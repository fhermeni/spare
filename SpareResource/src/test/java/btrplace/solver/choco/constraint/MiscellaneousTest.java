package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.KillVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.actionModel.ShutdownableNodeModel;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 4/10/13
 * Time: 9:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class MiscellaneousTest implements PremadeElements {
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
        System.out.println(plan);

        MaxSpareNode msn = new MaxSpareNode(map.getAllNodes(), 0, true);
        Assert.assertEquals(killVM.getStart(), 0);
        Assert.assertEquals(killVM.getEnd(), 2);
        Assert.assertEquals(migrateVM.getStart(), 0);
        Assert.assertEquals(migrateVM.getEnd(), 2);
        Assert.assertEquals(msn.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
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
        System.out.println(plan);
        System.out.println(plan.getResult());
        Assert.assertEquals(msn.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        Assert.fail("No fail");
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
        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(plan.getResult());
        Assert.assertEquals(msn.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        Assert.fail("No fail");
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

        ReconfigurationPlan p = rp.solve(0, false);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(sd.getHostingStart().getVal(), 0);
        Assert.assertEquals(sd.getStart().getVal(), 3);
        Assert.assertEquals(sd.getEnd().getVal(), 6);
        Assert.assertEquals(sd.getHostingEnd().getVal(), 3);
    }
}
