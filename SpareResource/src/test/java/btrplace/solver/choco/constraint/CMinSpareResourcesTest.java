package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.MinSpareResources;
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

public class CMinSpareResourcesTest {

    @Test()
    public void testCMinSpareResourcesDiscrete() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        VM vm5 = model.newVM();
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5, 2);
        rc.setConsumption(vm2, 4);
        rc.setConsumption(vm3, 3);
        rc.setConsumption(vm4, 1);
        MappingUtils.fill(map, model.getMapping());
        model.attach(rc);

        Set<Node> setn1 = new HashSet<Node>(Arrays.asList(n1, n2));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(model, l);
        Assert.assertNotNull(plan);
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test()
    public void testContinuousCMinSpareResources() throws SolverException {
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
        VM vm7 = model.newVM();
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5, vm6).ready(vm7).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5, 2);
        rc.setConsumption(vm3, 1);
        rc.setConsumption(vm4, 1);
        model.attach(rc);
        MappingUtils.fill(map, model.getMapping());
        Set<Node> setn1 = new HashSet<Node>(Arrays.asList(n1, n2));
        Running cr = new Running(Collections.singleton(vm7));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3, true);
        MinSpareResources c2 = new MinSpareResources(Collections.singleton(n2), "vcpu", 1, true);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(cr);
        l.add(c);
        l.add(c2);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(model, l);

        Assert.assertNotNull(plan);
        Assert.assertTrue(c.isSatisfied(plan.getResult()));

    }
}
