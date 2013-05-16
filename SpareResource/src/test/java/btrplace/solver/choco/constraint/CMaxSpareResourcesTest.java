package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Killed;
import btrplace.model.constraint.MaxSpareResources;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
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

public class CMaxSpareResourcesTest implements PremadeElements {

    private static final Logger log = LoggerFactory.getLogger("TEST");

    @Test
    public void testCMaxSpareResourcesDiscrete1() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 3);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 2);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.getOrigin().getMapping().toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test
    public void testCMaxSpareResourcesDiscrete2() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4, vm5, vm6)
                .run(n3, vm7, vm8)
                .run(n4, vm9)
                .run(n5, vm10).build();

        ShareableResource rc = new ShareableResource("vcpu", 2);
        rc.set(n1, 8);
        rc.set(n2, 8);
        rc.set(n3, 8);
        rc.set(n4, 4);
        rc.set(n5, 4);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> setn2 = new HashSet<UUID>(Arrays.asList(n4, n5));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 3);
        MaxSpareResources c2 = new MaxSpareResources(setn2, "vcpu", 3);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        l.add(oc);
        l.add(c2);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.getOrigin().getMapping().toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test
    public void testCMaxSpareResourcesDiscrete3() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 3);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2, n3));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 5);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.getOrigin().getMapping().toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test
    public void testCMaxSpareResourcesDiscrete4() throws SolverException {
        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 4);
        rc.set(vm1, 1);
        rc.set(vm2, 1);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 3);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 5);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test(enabled = false)
    public void testCMaxSpareResourcesContinuousSimple() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm3).run(n2, vm2).build();

        ShareableResource rc = new ShareableResource("vcpu", 1);
        rc.set(n1, 3);
        rc.set(n2, 2);

        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        Killed cKilled = new Killed(new HashSet<UUID>(Arrays.asList(vm1)));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 2);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        c.setContinuous(true);
        l.add(cKilled);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(2));
        //cra.setVerbosity(2);
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertNotNull(plan);
        Assert.assertEquals(rc.get(vm3), 1);
        log.info(plan.getOrigin().getMapping().toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test(enabled = false)
    public void testCMaxSpareResourcesContinuous() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5).build();

        ShareableResource rc = new ShareableResource("vcpu", 1);
        rc.set(n1, 4);
        rc.set(n2, 4);
        rc.set(n3, 4);
        rc.set(vm2, 2);
        rc.set(vm4, 2);
        rc.set(vm5, 2);

        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        Killed cKilled = new Killed(new HashSet<UUID>(Arrays.asList(vm1)));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 2);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        c.setContinuous(true);
        l.add(cKilled);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertNotNull(plan);
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
        log.info(plan.getOrigin().getMapping().toString());
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
    }
}
