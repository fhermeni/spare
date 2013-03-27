package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.Killed;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.KillVM;
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMaxSpareNodeTest implements PremadeElements {
    @Test
    public void discreteMaxSpareNodeTest1() throws SolverException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n2, vm1, vm2, vm3, vm4).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        MaxSpareNode msn = new MaxSpareNode(nodes, 1);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void discreteMaxSpareNodeTest2() throws SolverException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n2, vm1, vm2, vm3, vm4).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();

        MaxSpareNode msn = new MaxSpareNode(nodes, 0);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testMaxSNContinuousSimple() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm4).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Killed killvm = new Killed(new HashSet<UUID>(Arrays.asList(vm4)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(killvm);
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testMaxSNContinuous1() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm2)
                .run(n2, vm4).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Killed killvm = new Killed(new HashSet<UUID>(Arrays.asList(vm4)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Online online = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(killvm);
        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(online);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testPlanwithConcurrentActions() {
        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3).build();
        DefaultModel model = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        KillVM killVM = new KillVM(vm3, n2, 0, 2);
        MigrateVM migrateVM = new MigrateVM(vm1, n1, n2, 2, 4);
        BootNode bootNode = new BootNode(n3, 0, 3);
        plan.add(migrateVM);
        plan.add(killVM);
        plan.add(bootNode);
        Assert.assertTrue(plan.isApplyable());
        System.out.println(plan);

        MaxSpareNode msn = new MaxSpareNode(map.getAllNodes(), 1, true);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);

    }
}
