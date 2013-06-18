package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMaxSpareNodeTest {


    @Test(timeOut = 10000, groups = {"discrete"})
    public void discreteMaxSpareNodeTest1() throws SolverException {
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
                .run(n2, vm1, vm2, vm3, vm4).build();

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Set<Node> nodes = map.getAllNodes();
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
        Assert.assertTrue(msn.isSatisfied(plan.getResult()));
    }

    @Test(enabled = false, groups = {"brokenTest"})
    public void discreteMaxSpareNodeTest6() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        Node n5 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm5 = model.newVM();
        VM vm6 = model.newVM();
        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5)
                .run(n1, vm5, vm6)
                .run(n2, vm1, vm2).build();
        MappingUtils.fill(map, model.getMapping());


        Set<Node> nodes = map.getAllNodes();
        Online online = new Online(new HashSet<Node>(Arrays.asList(n4, n5)));
        MaxSpareNode msn = new MaxSpareNode(nodes, 0);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        constraints.add(online);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        Assert.assertTrue(msn.isSatisfied(plan.getResult()));
    }


    @Test(timeOut = 30000)
    public void testMaxSNContinuousSimplest() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        Mapping map = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm2)
                .run(n2, vm3).build();

        ShareableResource resources = new ShareableResource("vcpu", 4, 1);
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Set<Node> nodes = map.getAllNodes();
        MaxSpareNode msn = new MaxSpareNode(nodes, 0, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(new Killed(Collections.<VM>singleton(vm3)));
        constraints.add(msn);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(msn.isSatisfied(plan));
    }

    @Test(timeOut = 30000)
    public void testMaxSNContinuousSimple() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm4)
                .run(n2, vm3)
                .run(n3).build();

        ShareableResource resources = new ShareableResource("vcpu", 2, 1);
        resources.setCapacity(n1, 4);
        resources.setCapacity(n2, 8);
        resources.setConsumption(vm4, 2);
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Set<Node> nodes = map.getAllNodes();
        Ban ban = new Ban(Collections.singleton(vm3), Collections.singleton(n2));
        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(ban);
        constraints.add(msn);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(msn.isSatisfied(plan));
    }

    @Test
    public void testMaxSNContinuous3() throws SolverException {
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
        VM vm6 = model.newVM();
        Mapping map = new MappingBuilder().on(n1, n2, n3).off(n4, n5).ready(vm2, vm3)
                .run(n1, vm1, vm6)
                .run(n2, vm4).build();

        ShareableResource resources = new ShareableResource("vcpu", 2, 1);
        resources.setCapacity(n3, 1);
        resources.setCapacity(n4, 4);
        resources.setCapacity(n5, 4);

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2, n3, n4));

        MaxSpareNode msn = new MaxSpareNode(nodes, 1, true);
        Online online = new Online(new HashSet<Node>(Arrays.asList(n4, n5)));
        Running r = new Running(new HashSet<VM>(Arrays.asList(vm2, vm3)));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(r);
        constraints.add(msn);
        constraints.add(online);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(msn.isSatisfied(plan));
    }
}
