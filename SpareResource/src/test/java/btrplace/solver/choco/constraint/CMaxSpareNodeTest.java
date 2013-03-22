package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CMaxSpareNodeTest implements PremadeElements {
    @Test
    public void discreteMaxSpareNodeTest1() throws SolverException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 4);
        resources.set(n2, 8);
        resources.set(n3, 2);
        resources.set(vm4, 2);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n2);
        map.addRunningVM(vm4, n2);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);

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

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm4, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);

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
}
