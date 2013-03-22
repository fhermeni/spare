package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MinSpareResources;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class CMinSpareResourcesTest implements PremadeElements {

    @Test
    public void testCMinSpareResourcesDiscrete() throws SolverException {

        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n3);

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        c.setContinuous(false);

        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void test2CMinSpareResourcesDiscrete() throws SolverException {

        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n3);

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu",
                1);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        c.setContinuous(false);
        c2.setContinuous(false);

        l.add(c2);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testdiscreteCMinSpareResourcesWithOverbookRatio2() throws SolverException {

        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n3);

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 3);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu",
                1);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 2);

        c.setContinuous(false);
        c2.setContinuous(false);

        l.add(c2);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        // System.out.println(plan.toString());
        // System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testGetMisPlacedVMs() {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        // m.addRunningVM(vm5, n3);

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

    @Test
    public void testInject() throws SolverException {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n3);

        btrplace.model.view.ShareableResource rc = new ShareableResource("vcpu", 5);
        rc.set(vm1, 2);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        rc.set(vm4, 1);
        rc.set(vm5, 2);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu",
                1);

        c.setContinuous(false);
        c2.setContinuous(false);

        l.add(c);
        l.add(c2);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        System.out.println(plan);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);

        System.out.println(plan.getResult().getMapping().toString());

    }

    @Test
    public void testContinuousCMinSpareResources() throws SolverException {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n3);
        m.addRunningVM(vm6, n3);
        m.addReadyVM(vm7);

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

        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        MinSpareResources c = new MinSpareResources(setn1, "vcpu", 3);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu",
                1);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        cr.setContinuous(true);
        c.setContinuous(true);
        c2.setContinuous(true);

        l.add(oc);
        l.add(cr);
        l.add(c);
        l.add(c2);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);

        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());

    }

    @Test
    public void testwithOverBook() throws SolverException {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n3);
        m.addRunningVM(vm6, n3);
        m.addReadyVM(vm7);

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

        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "vcpu",
                1);

        c2.setContinuous(false);

        l.add(c2);
        l.add(oc);
        l.add(cr);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void discreteminSpareResourceTest() throws SolverException {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOfflineNode(n3);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n1);
        m.addRunningVM(vm6, n2);
        m.addReadyVM(vm7);

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

        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        MinSpareResources c2 = new MinSpareResources(m.getAllNodes(), "vcpu", 1);

        Set<UUID> node12 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c3 = new MinSpareResources(node12, "vcpu", 2);

        l.add(c2);
        l.add(oc);
        l.add(cr);
        l.add(c3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void discreteminSpareResourceTest2() throws SolverException {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOfflineNode(n3);
        m.addOfflineNode(n4);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n1);
        m.addRunningVM(vm6, n2);
        m.addReadyVM(vm7);

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

        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        MinSpareResources c2 = new MinSpareResources(m.getAllNodes(), "vcpu", 6);

        Set<UUID> node12 = new HashSet<UUID>(Arrays.asList(n1, n2));
        MinSpareResources c3 = new MinSpareResources(node12, "vcpu", 3);

        l.add(c2);
        l.add(oc);
        l.add(cr);
        l.add(c3);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);
        System.out.println(plan.getResult().getMapping().toString());
    }

}
