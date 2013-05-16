package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MinSpareResources;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMinSpareResourcesTest implements PremadeElements {

    private static final Logger log = LoggerFactory.getLogger("TEST");

    @Test(timeOut = 10000)
    public void testCMinSpareResourcesDiscrete() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    @Test(timeOut = 10000)
    public void test2CMinSpareResourcesDiscrete() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu", 1);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c2);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
    }

    public void testdiscreteCMinSpareResourcesWithOverbookRatio2() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 3);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu", 1);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 2);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c2);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
    }

    @Test(timeOut = 10000)
    public void testGetMisPlacedVMs() {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 5);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        MinSpareResources c = new MinSpareResources(m.getAllNodes(), "vcpu", 3);
        CMinSpareResources cc = new CMinSpareResources(c);

        Assert.assertFalse(cc.getMisPlacedVMs(mo).isEmpty());
        Assert.assertTrue(!cc.getMisPlacedVMs(mo).contains(vm5));
    }

    @Test(timeOut = 10000)
    public void testInject() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu", 1);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c);
        l.add(c2);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));
        log.info(plan.getResult().getMapping().toString());
    }

    @Test(timeOut = 10000)
    public void testContinuousCMinSpareResources() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5, vm6).ready(vm7).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        rc.set(vm6, 2);
        rc.set(vm7, 2);
        rc.set(n3, 5);
        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3, true);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu", 1, true);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1, true);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(oc);
        l.add(cr);
        l.add(c);
        l.add(c2);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertNotNull(plan);
        log.info(plan.toString());
        log.info(plan.getResult().getMapping().toString());
        Assert.assertTrue(c.isSatisfied(plan.getResult()));

    }

    @Test(timeOut = 10000)
    public void testwithOverBook() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm3)
                .run(n2, vm2, vm4)
                .run(n3, vm5, vm6).ready(vm7).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        rc.set(vm6, 2);
        rc.set(vm7, 2);

        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu", 1);


        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c2);
        l.add(oc);
        l.add(cr);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        log.info(plan.getResult().getMapping().toString());
    }

    @Test(timeOut = 10000)
    public void discreteminSpareResourceTest() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2).off(n3)
                .run(n1, vm1, vm3, vm5)
                .run(n2, vm2, vm4, vm6).ready(vm7).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        rc.set(vm6, 2);
        rc.set(vm7, 2);

        Model mo = new DefaultModel(m);
        mo.attach(rc);

        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        MinSpareResources c2 = new MinSpareResources(m.getAllNodes(), "vcpu", 1);
        Set<UUID> node12 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c3 = new MinSpareResources(node12, "vcpu", 2);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c2);
        l.add(oc);
        l.add(cr);
        l.add(c3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.getResult().getMapping().toString());
    }

    @Test(timeOut = 10000)
    public void discreteminSpareResourceTest2() throws SolverException {

        Mapping m = new MappingBuilder().on(n1, n2).off(n3, n4)
                .run(n1, vm1, vm3, vm5)
                .run(n2, vm2, vm4, vm6).ready(vm7).build();

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        rc.set(vm6, 2);
        rc.set(vm7, 2);

        Model mo = new DefaultModel(m);
        mo.attach(rc);


        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        MinSpareResources c2 = new MinSpareResources(m.getAllNodes(), "vcpu", 6);
        Set<UUID> node12 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c3 = new MinSpareResources(node12, "vcpu", 3);

        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(c2);
        l.add(oc);
        l.add(cr);
        l.add(c3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        log.info(plan.getResult().getMapping().toString());
    }

}
