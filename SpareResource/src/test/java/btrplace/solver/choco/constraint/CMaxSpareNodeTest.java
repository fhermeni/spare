package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.KillVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
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
    @Test(groups = {"discrete"})
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
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(groups = {"discrete"})
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
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(groups = {"discrete"})
    public void discreteMaxSpareNodeTest3() throws SolverException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 2);

        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm2).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        Online online = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        MaxSpareNode msn = new MaxSpareNode(map.getAllNodes(), 0);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(online);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setVerbosity(1);
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
//        cra.setMaxEnd(10);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(groups = {"discrete"})
    public void discreteMaxSpareNodeTest4() throws SolverException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm5, vm6)
                .run(n2, vm1, vm2).build();


        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));
        Online online = new Online(new HashSet<UUID>(Arrays.asList(n4, n5)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 0);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(online);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(groups = {"discrete"})
    public void discreteMaxSpareNodeTest5() throws SolverException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm5, vm6).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        Online online = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        MaxSpareNode msn = new MaxSpareNode(map.getAllNodes(), 0);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(online);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.setMaxEnd(30);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(groups = {"discrete"})
    public void discreteMaxSpareNodeTest6() throws SolverException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm5, vm6)
                .run(n2, vm1, vm2).build();


        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4, n5));
        Online online = new Online(new HashSet<UUID>(Arrays.asList(n4, n5)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 0);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(online);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(groups = {"discrete"})
    public void discreteMaxSpareNodeTestMassive() throws SolverException {

        UUID n6 = UUID.randomUUID();
        UUID n7 = UUID.randomUUID();
        UUID n8 = UUID.randomUUID();
        UUID n9 = UUID.randomUUID();
        UUID n10 = UUID.randomUUID();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3, n6, n7, n8).off(n4, n5, n9, n10)
                .run(n1, vm5, vm6, vm3, vm7)
                .run(n2, vm1, vm2, vm4, vm10)
                .run(n3, vm8, vm9).build();


        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
//        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4, n5));
        Online online = new Online(new HashSet<UUID>(Arrays.asList(n4, n5, n9, n10)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 2);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(online);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.repair(false);
        cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(dependsOnGroups = {"discrete"})
    public void testMaxSNContinuousSimplest() throws SolverException {
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
        constraints.add(new Killed(new HashSet<UUID>(Arrays.asList(vm3))));
        constraints.add(msn);
        constraints.add(overbook);

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
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
    }

    @Test(dependsOnMethods = {"testMaxSNContinuousSimplest"})
    public void testMaxSNContinuousSimplest2() throws SolverException {
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
        constraints.add(new Ban(new HashSet<UUID>(Arrays.asList(vm3)), new HashSet<UUID>(Arrays.asList(n2))));
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
//        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(plan.getResult());
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
    }

    @Test(dependsOnMethods = {"testMaxSNContinuousSimplest2"})
    public void testMaxSNContinuousSimple() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm3)
                .run(n3).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Ban ban = new Ban(new HashSet<UUID>(Arrays.asList(vm3)), new HashSet<UUID>(Arrays.asList(n2)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(ban);
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
/*        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));*/
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
//        cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(plan.getResult());
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
//        Assert.fail();
    }

    @Test(dependsOnMethods = {"testMaxSNContinuousSimple"})
    public void testMaxSNContinuous1() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 2);
        resources.set(n3, 2);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();

        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Online online = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(online);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(3));
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(2));
//        cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
    }

    @Test(dependsOnMethods = {"testMaxSNContinuousSimple"})
    public void testMaxSNContinuous2() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2).off(n3).ready(vm2, vm3)
                .run(n1, vm1).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 2);
        resources.set(n3, 2);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();

        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Online online = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        Running r = new Running(new HashSet<UUID>(Arrays.asList(vm2, vm3)));
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(r);
        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(online);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(2));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
//        cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
    }
}
