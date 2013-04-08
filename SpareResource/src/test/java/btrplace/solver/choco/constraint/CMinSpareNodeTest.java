package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MinSpareNode;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMinSpareNodeTest implements PremadeElements {
    @Test
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

    @Test
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

    @Test
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

    @Test
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
        cra.setVerbosity(1);
        ReconfigurationPlan plan = cra.solve(model, constraints);

        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan.getResult()), Sat.SATISFIED);
    }

    @Test
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
        cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
    }
}
