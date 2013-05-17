package btrplace.solver.choco.constraint;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMaxOnlinesTest implements PremadeElements {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test(timeOut = 10000)
    public void discreteMaxonlinesTest() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3).build();
        Model model = new DefaultModel(map);
        Set<UUID> nodes = map.getAllNodes();
        MaxOnlines maxon = new MaxOnlines(nodes, 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        log.info(model.toString());
        log.info(constraints.toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
    }

    @Test(timeOut = 10000)
    public void discreteMaxonlinesTest2() throws SolverException {
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
        MaxOnlines maxon = new MaxOnlines(nodes, 2);
        Set<UUID> nodes2 = map.getAllNodes();
        nodes2.remove(n3);
        MaxOnlines maxon2 = new MaxOnlines(nodes2, 1);
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        constraints.add(overbook);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        log.info(model.toString());
        log.info(constraints.toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
        Assert.assertFalse(maxon.isSatisfied(plan));
    }

    @Test()
    public void continuousMaxOnlinesSimplestTest() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        Model model = new DefaultModel(map);
        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));
        MaxOnlines maxon = new MaxOnlines(nodes, 1);
        Online oncstr = new Online(new HashSet<UUID>(Arrays.asList(n2)));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        maxon.setContinuous(true);
        constraints.add(maxon);
        constraints.add(oncstr);
        constraints.add(new Offline(new HashSet<UUID>(Arrays.asList(n1))));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(5));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        ////////cra.setVerbosity(2);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(model.toString());
        log.info(constraints.toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));
    }


    @Test(timeOut = 10000)
    public void continuousMaxOnlinesSimpleTest() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);
        map.addOfflineNode(n4);
        Model model = new DefaultModel(map);
        Set<UUID> nodes = map.getAllNodes();
        MaxOnlines maxon = new MaxOnlines(nodes, 2);
        Online oncstr = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        maxon.setContinuous(true);
        constraints.add(maxon);
        constraints.add(oncstr);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(4));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(3));
//        ////////cra.setVerbosity(1);
        cra.setMaxEnd(20);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(model.toString());
        log.info(constraints.toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test(timeOut = 30000)
    public void continuousMaxOnlinesTest1() throws SolverException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 8);
        resources.set(n2, 8);
        resources.set(n3, 4);
        resources.set(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm5, vm6)
                .run(n2, vm2, vm3, vm4)
                .run(n3, vm3).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        Set<UUID> nodes = map.getAllNodes();
        MaxOnlines maxon = new MaxOnlines(nodes, 2);
        Online oncstr = new Online(new HashSet<UUID>(Arrays.asList(n3)));
        Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        maxon.setContinuous(true);
        constraints.add(maxon);
        constraints.add(oncstr);
        constraints.add(overbook);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(5));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
//        ////////cra.setVerbosity(1);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        cra.setMaxEnd(10);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test(timeOut = 10000)
    public void failWithContinuousRestrictionSimpleCase() throws SolverException {
        ShareableResource resources = new ShareableResource("cpu", 1);
        resources.set(n1, 8);
        resources.set(n2, 4);
        resources.set(n3, 4);
        resources.set(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n3).off(n2)
                .run(n1, vm1, vm4)
                .run(n3, vm3).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        MaxOnlines maxon = new MaxOnlines(map.getAllNodes(), 2);
        Overbook overbook = new Overbook(map.getAllNodes(), "cpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(overbook);
        constraints.add(new Online(new HashSet<UUID>(Arrays.asList(n2))));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test(timeOut = 60000)
    public void successWithContinuousRestrictionSimpleCase() throws SolverException {
        ShareableResource resources = new ShareableResource("cpu", 1);
        resources.set(n1, 8);
        resources.set(n2, 4);
        resources.set(n3, 4);
        resources.set(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n3).off(n2)
                .run(n1, vm1, vm4)
                .run(n3, vm3).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        MaxOnlines maxon = new MaxOnlines(map.getAllNodes(), 2, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "cpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(overbook);
        constraints.add(new Online(new HashSet<UUID>(Arrays.asList(n2))));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test(timeOut = 10000)
    public void ComplexContinuousTest1() throws SolverException {
        ShareableResource resources = new ShareableResource("cpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(n4, 2);
        resources.set(n5, 2);
        resources.set(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        MaxOnlines maxon = new MaxOnlines(map.getAllNodes(), 4);
        MaxOnlines maxon2 = new MaxOnlines(new HashSet<UUID>(Arrays.asList(n2, n3, n4)), 2);
        Overbook overbook = new Overbook(map.getAllNodes(), "cpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        constraints.add(overbook);
        constraints.add(new Online(new HashSet<UUID>(Arrays.asList(n4, n5))));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test
    public void ComplexContinuousTest2() throws SolverException {
        ShareableResource resources = new ShareableResource("cpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(n4, 2);
        resources.set(n5, 2);
        resources.set(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        MaxOnlines maxon = new MaxOnlines(map.getAllNodes(), 4, true);
        MaxOnlines maxon2 = new MaxOnlines(new HashSet<UUID>(Arrays.asList(n2, n3, n4)), 2, true);
        Overbook overbook = new Overbook(map.getAllNodes(), "cpu", 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        constraints.add(overbook);
        constraints.add(new Online(new HashSet<UUID>(Arrays.asList(n4, n5))));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(5));
        cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
        cra.setMaxEnd(15);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));

    }
}
