package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMaxOnlinesTest {

    @Test
    public void discreteMaxOnlineTest() throws SolverException {
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
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
    }

    @Test
    public void discreteMaxOnlineTest2() throws SolverException {
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
        Assert.assertTrue(maxon.isSatisfied(plan.getResult()));
    }

    @Test
    public void testSimpleContinuousCase() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        model.getMapping().addOnlineNode(n1);
        model.getMapping().addOfflineNode(n2);
        MaxOnline maxOnline = new MaxOnline(model.getNodes(), 1, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxOnline);
        constraints.add(new Online(Collections.singleton(n2)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setTimeLimit(5);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        Assert.assertTrue(maxOnline.isSatisfied(plan));
    }

    @Test
    public void testContinuousRestrictionSimpleCase() throws SolverException {
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
        Assert.assertTrue(maxon.isSatisfied(plan));
    }

    @Test
    public void ComplexContinuousTest2() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode(1);
        Node n2 = model.newNode(2);
        Node n3 = model.newNode(3);
        Node n4 = model.newNode(4);
        Node n5 = model.newNode(5);
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

        MaxOnline maxOn = new MaxOnline(map.getAllNodes(), 4, true);
        MaxOnline maxOn2 = new MaxOnline(new HashSet<Node>(Arrays.asList(n2, n3, n4)), 2, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(maxOn);
        constraints.add(maxOn2);
        constraints.add(new Online(new HashSet<Node>(Arrays.asList(n4, n5))));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setMaxEnd(15);
        cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(maxOn.isSatisfied(plan));
        System.out.println(plan);
    }
}
