package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.KillVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.actionModel.NodeActionModel;
import btrplace.solver.choco.actionModel.ShutdownableNodeModel;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.objective.minMTTR.MinMTTR;
import btrplace.solver.choco.view.CShareableResource;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.constraints.integer.ElementV;
import choco.cp.solver.constraints.integer.MaxOfAList;
import choco.cp.solver.constraints.integer.MinOfAList;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static btrplace.solver.choco.chocoUtil.ChocoUtils.postIfOnlyIf;

/**
 * User: TU HUYNH DANG
 * Date: 4/10/13
 * Time: 9:07 AM
 */
public class MiscellaneousTest implements PremadeElements {
    private static final Logger log = LoggerFactory.getLogger("TEST");

    @Test
    public void testPlanwithConcurrentActions() {
        Mapping map = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm2)
                .run(n2, vm3).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 4);

        Model model = new DefaultModel(map);
        model.attach(resources);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
//        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        KillVM killVM = new KillVM(vm3, n2, 0, 2);
        MigrateVM migrateVM = new MigrateVM(vm1, n1, n2, 0, 2);
//        BootNode bootNode = new BootNode(n3, 2, 3);
        plan.add(migrateVM);
        plan.add(killVM);
//        plan.add(bootNode);
        Assert.assertTrue(plan.isApplyable());
        log.info(plan.toString());

        MaxSpareNode msn = new MaxSpareNode(map.getAllNodes(), 0, true);
        Assert.assertEquals(killVM.getStart(), 0);
        Assert.assertEquals(killVM.getEnd(), 2);
        Assert.assertEquals(migrateVM.getStart(), 0);
        Assert.assertEquals(migrateVM.getEnd(), 2);
        Assert.assertTrue(msn.isSatisfied(plan));
    }


    @Test
    public void testPlanCompute() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm2)
                .run(n2, vm3).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 4);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        MaxSpareNode msn = new MaxSpareNode(nodes, 0, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
//        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(new Online(new HashSet<UUID>(Arrays.asList(n2))));
        constraints.add(new Ban(new HashSet<UUID>(Arrays.asList(vm1)), new HashSet<UUID>(Arrays.asList(n1))));
        constraints.add(new Ban(new HashSet<UUID>(Arrays.asList(vm3)), new HashSet<UUID>(Arrays.asList(n2))));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
//        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().toString());
        Assert.assertTrue(msn.isSatisfied(plan));
    }

    @Test
    public void testISIE() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm2).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 4);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(new Online(new HashSet<UUID>(Arrays.asList(n2))));
        constraints.add(new Ban(new HashSet<UUID>(Arrays.asList(vm1)), new HashSet<UUID>(Arrays.asList(n1))));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
//        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().toString());
        Assert.assertTrue(msn.isSatisfied(plan));
    }

    @Test
    public void testShutdownableV27() throws SolverException, ContradictionException {
        Mapping map = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm2)
                .run(n2, vm3).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 4);

        Model model = new DefaultModel(map);
        model.attach(resources);

        DurationEvaluators dev = new DurationEvaluators();
        dev.register(ShutdownNode.class, new ConstantDuration(3));
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model).setDurationEvaluatators(dev)
                .labelVariables().build();


        ShutdownableNodeModel sd = (ShutdownableNodeModel) rp.getNodeAction(n2);
        sd.getState().setVal(0);
        sd.getStart().setVal(3);

        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        Assert.assertEquals(sd.getHostingStart().getVal(), 0);
        Assert.assertEquals(sd.getStart().getVal(), 3);
        Assert.assertEquals(sd.getEnd().getVal(), 6);
        Assert.assertEquals(sd.getHostingEnd().getVal(), 3);
    }

    @Test
    public void testV29Regression() throws SolverException, ContradictionException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 2);
        resources.set(n3, 2);
        resources.set(vm3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2).ready(vm3).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .setNextVMsStates(new HashSet<UUID>(), map.getAllVMs(), new HashSet<UUID>(), new HashSet<UUID>())     // VM3 turns to run
                .labelVariables().build();
        CPSolver s = rp.getSolver();


        ///------------Overbook constraint in v27-------------//
        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + "vcpu");
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to get the resource mapping '" + "vcpu");
        }

        IntDomainVar[] rawCapa = rcm.getPhysicalUsage();
        IntDomainVar[] realCapa = rcm.getVirtualUsage();
        for (UUID u : map.getAllNodes()) {
            int nIdx = rp.getNode(u);
            s.post(s.eq(realCapa[nIdx], rawCapa[nIdx]));
            try {
                realCapa[nIdx].setSup(rawCapa[nIdx].getSup());
            } catch (ContradictionException ex) {
                rp.getLogger().error("Unable to restrict the real '{}' capacity of {} to {}: ", u, rawCapa[nIdx].getSup(), ex.getMessage());

            }
        }
        ///------------End of Overbook constraint in v27-------------//

        //------------MinSpareNode({n1,n2,n3}, 1, continuous=true)-------------//
        int NUMBER_OF_NODE = 3; // 3 nodes are considered
        int MAX_TIME = 20;
        s.post(s.leq(rp.getEnd(), MAX_TIME));

        IntDomainVar capacity = s.createIntegerConstant("capacity", 1);
        IntDomainVar consumption = s.createBoundIntVar("consum", 1, NUMBER_OF_NODE);//minimum consumption of the resource
//                IntDomainVar uppBound = rp.getEnd(); // All tasks must be scheduled before this time
        IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_NODE]; // The state of the node
        IntDomainVar[] durations = new IntDomainVar[NUMBER_OF_NODE]; // Online duration
        TaskVar[] taskvars = new TaskVar[NUMBER_OF_NODE]; // Online duration is modeled as a task

        // The start moment of node being idle
        IntDomainVar[] idle_starts = new IntDomainVar[NUMBER_OF_NODE];

        // The end moment of node being idle
        IntDomainVar[] idle_ends = new IntDomainVar[NUMBER_OF_NODE];
        int i = 0;
        for (UUID n : map.getAllNodes()) {
            NodeActionModel na = rp.getNodeAction(n);
            idle_starts[i] = s.createBoundIntVar("IS(" + n + ")", 0, MAX_TIME);
            idle_ends[i] = s.createBoundIntVar("IE(" + n + ")", 0, MAX_TIME);
            ArrayList<IntDomainVar> CElist = new ArrayList<IntDomainVar>();
            CElist.add(0, idle_starts[i]);
            Set<UUID> vms = rp.getSourceModel().getMapping().getRunningVMs(n);
            if (!vms.isEmpty()) {
                for (UUID vm : vms) {
                    VMActionModel vma = rp.getVMAction(vm);
                    Slice c = vma.getCSlice();
                    CElist.add(c.getEnd());
                }
                s.post(new MaxOfAList(s.getEnvironment(), CElist.toArray(new IntDomainVar[CElist.size()])));
            } else {
                s.post(s.eq(idle_starts[i], na.getHostingStart()));
            }
            ArrayList<IntDomainVar> dSlist = new ArrayList<IntDomainVar>();
            dSlist.add(idle_ends[i]);
            for (VMActionModel vma : rp.getVMActions()) {
                Slice dSlice = vma.getDSlice();
                if (dSlice == null) continue;
                IntDomainVar eq = s.createBooleanVar("eq");
                IntDomainVar tmpdEnd = s.createBoundIntVar("dS" + dSlice.getSubject(), 0, MAX_TIME);

                postIfOnlyIf(s, eq, s.eq(dSlice.getHoster(), rp.getNode(n)));
                s.post(new ElementV(new IntDomainVar[]{na.getHostingEnd(), dSlice.getStart(), eq, tmpdEnd}, 0, s.getEnvironment()));
                dSlist.add(tmpdEnd);
            }
            s.post(new MinOfAList(s.getEnvironment(), dSlist.toArray(new IntDomainVar[dSlist.size()])));

            durations[i] = rp.makeUnboundedDuration("Dur(" + n + ")");
            s.post(s.leq(durations[i], rp.getEnd()));
            heights[i] = s.makeConstantIntVar(1); // All tasks have to be scheduled
            taskvars[i] = s.createTaskVar("Task_" + n, idle_starts[i], idle_ends[i], durations[i]);
            i++;
        }
        IntDomainVar r = s.createBoundIntVar("NBusy", 0, NUMBER_OF_NODE);
        s.post(s.occurence(durations, r, 0));
        int v2 = NUMBER_OF_NODE - 1;
        s.post(s.leq(r, v2));
        s.post(s.eq(s.sum(durations), s.mult(1, rp.getEnd())));
        Cumulative cumulative = new Cumulative(s, "Cumulative", taskvars, heights,
                consumption, capacity, rp.getEnd());
        s.post(cumulative);


        // Extract all the state of the involved nodes (all nodes in this case)
        IntDomainVar[] states = new IntDomainVar[NUMBER_OF_NODE];
        int j = 0;
        for (UUID n : map.getAllNodes()) {
            states[j++] = rp.getNodeAction(n).getState();
        }
        IntDomainVar[] VMsOnAllNodes = rp.getNbRunningVMs();
// Each element is the number of VMs on each node
        IntDomainVar[] vmsOnInvolvedNodes = new IntDomainVar[NUMBER_OF_NODE];
        IntDomainVar[] idles = new IntDomainVar[NUMBER_OF_NODE];
        i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (UUID n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = s.createBoundIntVar("nVMs" + n, 0, maxVMs);
            IntDomainVar state = rp.getNodeAction(n).getState();
// If the node is offline -> the temporary variable is -1, otherwise, it equals the number of VMs on that node
            IntDomainVar[] c = new IntDomainVar[]{s.makeConstantIntVar(1), VMsOnAllNodes[rp.getNode(n)],
                    state, vmsOnInvolvedNodes[i]};
            s.post(new ElementV(c, 0, s.getEnvironment()));
// IF number of VMs on a node is 0 -> idle
            idles[i] = s.createBooleanVar("idle" + n);
            postIfOnlyIf(s, idles[i], s.eq(vmsOnInvolvedNodes[i], 0));
            i++;
        }
        IntExp Sidle = s.sum(idles);
        s.post(s.geq(Sidle, 1));
        //------------End of MinSpareNode-------------//

        System.err.println(s.pretty());
        System.err.flush();
        MinMTTR obj = new MinMTTR();
        obj.inject(rp);
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().toString());

    }
}
