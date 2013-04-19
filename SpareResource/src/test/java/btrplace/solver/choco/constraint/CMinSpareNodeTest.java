package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MinSpareNode;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
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

public class CMinSpareNodeTest implements PremadeElements {
    @Test(timeOut = 10000)
    public void discreteMinSpareNodeTest1() throws SolverException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();

        Model model = new DefaultModel(map);
        model.attach(resources);
        Set<UUID> nodes = map.getAllNodes();
        MinSpareNode msn = new MinSpareNode(nodes, 1);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());

        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(timeOut = 10000)
    public void discreteMinSpareNodeTest2() throws SolverException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        MinSpareNode msn = new MinSpareNode(nodes, 2);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(timeOut = 10000)
    public void discreteMinSpareNodeTest3() throws SolverException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 2);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        MinSpareNode msn = new MinSpareNode(map.getAllNodes(), 1);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(timeOut = 10000)
    public void discreteMinSpareNodeTest4() throws SolverException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 3);
        resources.set(n2, 1);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2).off(n3, n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        MinSpareNode msn = new MinSpareNode(map.getAllNodes(), 2);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        //cra.setVerbosity(1);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test(timeOut = 10000)
    public void continuousMinSpareNodeTest1() throws SolverException {

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
        Set<UUID> nodes = map.getAllNodes();
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        Running run = new Running(new HashSet<UUID>(Arrays.asList(vm3)));

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(run);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        //cra.setVerbosity(2);
        cra.setMaxEnd(15);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
    }

    @Test(timeOut = 10000)
    public void testMinSNContinuousSimplest() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1)
                .ready(vm3).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Running running = new Running(new HashSet<UUID>(Arrays.asList(vm3)));
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(overbook);
        constraints.add(running);
        constraints.add(msn);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        //cra.setVerbosity(2);
        cra.setMaxEnd(15);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        System.out.println(plan);
        System.out.println(plan.getResult());
//        Assert.fail();
    }

    @Test(timeOut = 10000)
    public void testMinSNContinuousOnSimple() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm3).ready(vm5, vm6).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 1);
        resources.set(n3, 2);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Running running = new Running(new HashSet<UUID>(Arrays.asList(vm5, vm6)));
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(running);
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(3));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
//        //cra.setVerbosity(2);
        cra.setMaxEnd(15);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        System.out.println(plan);
        System.out.println(plan.getResult());
    }

    @Test(timeOut = 10000)
    public void testMinSNContinuosOnNormal() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4, vm2)
                .run(n2, vm3).ready(vm5, vm6, vm7).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 2);
        resources.set(n3, 4);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Running running = new Running(new HashSet<UUID>(Arrays.asList(vm5, vm6, vm7)));
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(running);
        constraints.add(msn);
        constraints.add(overbook);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(2));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        cra.setMaxEnd(10);
//        //cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        System.out.println(plan);
        System.out.println(plan.getResult());
    }

    @Test(timeOut = 10000)
    public void testMinSNContinuousComplex() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4)
                .run(n1, vm1)
                .run(n2, vm3)
                .ready(vm2).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 1);
        resources.set(n2, 1);
        resources.set(n3, 1);
        resources.set(n4, 1);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Running runvms = new Running(new HashSet<UUID>(Arrays.asList(vm2)));
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(runvms);
        constraints.add(msn);
        constraints.add(new Overbook(map.getAllNodes(), "vcpu", 1));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(2));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        //cra.setVerbosity(2);
        cra.setMaxEnd(15);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        System.out.println(plan);
        System.out.println(plan.getResult());
    }

    @Test(timeOut = 20000)
    public void testMinSNContinuousComplex2() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm1)
                .run(n2, vm3)
                .ready(vm2, vm4).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 1);
        resources.set(n2, 1);
        resources.set(n3, 1);
        resources.set(n4, 1);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = map.getAllNodes();
        Running runvms = new Running(new HashSet<UUID>(Arrays.asList(vm2, vm4)));
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(runvms);
        constraints.add(msn);
        constraints.add(new Overbook(map.getAllNodes(), "vcpu", 1));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
/*        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(MigrateVM.class, new ConstantDuration(2));
        cra.getDurationEvaluators().register(KillVM.class, new ConstantDuration(2));*/
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        //cra.setVerbosity(2);
        cra.setMaxEnd(15);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        System.out.println(plan);
        System.out.println(plan.getResult());
    }


    @Test(timeOut = 10000)
    public void testContinuousComplex3() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5).ready(vm2, vm3)
                .run(n1, vm1, vm6)
                .run(n2, vm4).build();

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 2);
        resources.set(n3, 1);
        resources.set(n4, 4);
        resources.set(n5, 4);

        Model model = new DefaultModel(map);
        model.attach(resources);

        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));
        Set<UUID> nodes2 = new HashSet<UUID>(Arrays.asList(n1, n3, n4));

        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        MinSpareNode minSpareNode = new MinSpareNode(nodes2, 1, true);

        Online online = new Online(new HashSet<UUID>(Arrays.asList(n4, n5)));
        Running r = new Running(new HashSet<UUID>(Arrays.asList(vm2, vm3)));
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(r);
        constraints.add(msn);
        constraints.add(overbook);
        constraints.add(online);
        constraints.add(minSpareNode);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        cra.setMaxEnd(20);
//        ////cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
        Assert.assertEquals(minSpareNode.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
    }
}
