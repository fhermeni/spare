package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.MinSpareNode;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Running;
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

public class CMinSpareNodeTest {

    @Test(timeOut = 30000)
    public void discreteMinSpareNodeTest4() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        Node n5 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm4 = model.newVM();
        ShareableResource resources = new ShareableResource("vcpu", 1, 1);
        resources.setCapacity(n1, 3);
        resources.setCapacity(n3, 2);
        resources.setConsumption(vm4, 2);

        Mapping map = new MappingBuilder().on(n1, n2).off(n3, n4, n5)
                .run(n1, vm1, vm4)
                .run(n2, vm2).build();

        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);
        MinSpareNode msn = new MinSpareNode(map.getAllNodes(), 2);
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(msn);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(msn.isSatisfied(plan.getResult()));
    }


    @Test(timeOut = 30000)
    public void testContinuousComplex3() throws SolverException {
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

        ShareableResource resources = new ShareableResource("vcpu", 1, 1);
        resources.setCapacity(n1, 2);
        resources.setCapacity(n2, 2);
        resources.setCapacity(n4, 4);
        resources.setCapacity(n5, 4);
        MappingUtils.fill(map, model.getMapping());
        model.attach(resources);

        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2, n3, n4));
        Set<Node> nodes2 = new HashSet<Node>(Arrays.asList(n1, n3, n4));
        MinSpareNode msn = new MinSpareNode(nodes, 1, true);
        MinSpareNode minSpareNode = new MinSpareNode(nodes2, 1, true);
        Online online = new Online(new HashSet<Node>(Arrays.asList(n4, n5)));
        Running r = new Running(new HashSet<VM>(Arrays.asList(vm2, vm3)));
        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        constraints.add(r);
        constraints.add(msn);
        constraints.add(online);
        constraints.add(minSpareNode);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareNode.Builder());
        cra.getSatConstraintMapper().register(new CMinSpareNode.Builder());
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, constraints);
        Assert.assertNotNull(plan);
        Assert.assertTrue(msn.isSatisfied(plan));
        Assert.assertTrue(minSpareNode.isSatisfied(plan));
    }
}
