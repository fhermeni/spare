package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMaxOnlinesTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test(timeOut = 30000)
    public void discreteMaxonlinesTest() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2)
                .run(n3, vm3).build();
        MappingUtils.fill(map, model.getMapping());
        Set<Node> nodes = map.getAllNodes();
        MaxOnline maxon = new MaxOnline(nodes, 1);
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

    @Test(timeOut = 30000)
    public void discreteMaxonlinesTest2() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("vcpu", 1, 1);
        resources.setCapacity(n1, 4);
        resources.setCapacity(n2, 8);
        resources.setCapacity(n3, 2);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();
        model.attach(resources);
        MappingUtils.fill(map, model.getMapping());
        Set<Node> nodes = map.getAllNodes();
        MaxOnline maxon = new MaxOnline(nodes, 2);
        Set<Node> nodes2 = new HashSet<Node>(Arrays.asList(n1, n2));

        MaxOnline maxon2 = new MaxOnline(nodes2, 1);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        log.info(model.toString());
        log.info(constraints.toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
    }

    @Test()
    public void continuousMaxOnlinesSimplestTest() throws SolverException {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2));
        MaxOnline maxon = new MaxOnline(nodes, 1);
        Online oncstr = new Online(new HashSet<Node>(Arrays.asList(n2)));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        maxon.setContinuous(true);
        constraints.add(maxon);
        constraints.add(oncstr);
        constraints.add(new Offline(new HashSet<Node>(Arrays.asList(n1))));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
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


    @Test(timeOut = 30000)
    public void continuousMaxOnlinesSimpleTest() throws SolverException {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);
        map.addOfflineNode(n4);
        Set<Node> nodes = map.getAllNodes();
        MaxOnline maxon = new MaxOnline(nodes, 2);
        Online oncstr = new Online(new HashSet<Node>(Arrays.asList(n3)));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        maxon.setContinuous(true);
        constraints.add(maxon);
        constraints.add(oncstr);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
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
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        VM vm5 = model.newVM();
        VM vm6 = model.newVM();
        ShareableResource resources = new ShareableResource("vcpu", 8, 1);
        resources.setCapacity(n3, 4);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm5, vm6)
                .run(n2, vm2, vm3, vm4)
                .run(n3, vm3).build();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);
        Set<Node> nodes = map.getAllNodes();
        MaxOnline maxon = new MaxOnline(nodes, 2);
        Online oncstr = new Online(Collections.singleton(n3));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        maxon.setContinuous(true);
        constraints.add(maxon);
        constraints.add(oncstr);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        cra.setMaxEnd(30);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test(timeOut = 30000)
    public void failWithContinuousRestrictionSimpleCase() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("cpu", 4, 1);
        resources.setCapacity(n1, 8);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n3).off(n2)
                .run(n1, vm1, vm4)
                .run(n3, vm3).build();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);
        MaxOnline maxon = new MaxOnline(map.getAllNodes(), 2);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(new Online(Collections.singleton(n2)));
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
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("cpu", 4, 1);
        resources.setCapacity(n1, 8);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n3).off(n2)
                .run(n1, vm1, vm4)
                .run(n3, vm3).build();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);
        MaxOnline maxon = new MaxOnline(map.getAllNodes(), 2, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(new Online(Collections.singleton(n2)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test(timeOut = 30000)
    public void ComplexContinuousTest1() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        Node n5 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("cpu", 2, 1);
        resources.setCapacity(n1, 4);
        resources.setCapacity(n2, 8);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);
        MaxOnline maxon = new MaxOnline(map.getAllNodes(), 4);
        MaxOnline maxon2 = new MaxOnline(new HashSet<Node>(Arrays.asList(n2, n3, n4)), 2);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        constraints.add(new Online(new HashSet<Node>(Arrays.asList(n4, n5))));
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
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        Node n5 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("cpu", 2, 1);
        resources.setCapacity(n1, 4);
        resources.setCapacity(n2, 8);
        resources.setConsumption(vm4, 2);
        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2)
                .run(n3, vm3).build();
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        log.info(map.toString());
        MaxOnline maxon = new MaxOnline(map.getAllNodes(), 4, true);
        MaxOnline maxon2 = new MaxOnline(new HashSet<Node>(Arrays.asList(n2, n3, n4)), 2, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxon);
        constraints.add(maxon2);
        constraints.add(new Online(new HashSet<Node>(Arrays.asList(n4, n5))));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setMaxEnd(15);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(maxon.isSatisfied(plan));

    }
}
