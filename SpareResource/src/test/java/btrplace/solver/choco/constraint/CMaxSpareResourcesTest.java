package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.Killed;
import btrplace.model.constraint.MaxSpareResources;
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

public class CMaxSpareResourcesTest {

    @Test
    public void testCMaxSpareResourcesDiscrete4() throws SolverException {
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

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 3, 1);
        rc.setConsumption(vm5, 3);
        MappingUtils.fill(map, model.getMapping());
        model.attach(rc);

        Set<Node> setn1 = new HashSet<Node>(Arrays.asList(n1, n2));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 5);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(model, l);
        Assert.assertNotNull(plan);
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test(enabled = false)
    public void testCMaxSpareResourcesContinuous() throws SolverException {
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
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5).build();

        ShareableResource rc = new ShareableResource("vcpu", 4, 2);
        rc.setConsumption(vm1, 1);
        rc.setConsumption(vm3, 1);
        MappingUtils.fill(map, model.getMapping());
        model.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<Node> setn1 = new HashSet<Node>(Arrays.asList(n1, n2));

        Killed cKilled = new Killed(Collections.singleton(vm1));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 2);
        c.setContinuous(true);
        l.add(cKilled);
        l.add(c);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(model, l);

        Assert.assertNotNull(plan);
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }
}
