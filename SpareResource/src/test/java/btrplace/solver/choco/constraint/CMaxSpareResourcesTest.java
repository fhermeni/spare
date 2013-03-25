package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MaxSpareResources;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Sleeping;
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

public class CMaxSpareResourcesTest implements PremadeElements {

    @Test
    public void testCMaxSpareResourcesDiscrete1() throws SolverException {

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
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 3);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));

        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 2);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        c.setContinuous(false);

        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.getOrigin().getMapping().toString());
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testCMaxSpareResourcesDiscrete2() throws SolverException {

        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addOnlineNode(n4);
        m.addOnlineNode(n5);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n1);
        m.addRunningVM(vm3, n1);
        m.addRunningVM(vm4, n2);
        m.addRunningVM(vm5, n2);
        m.addRunningVM(vm6, n2);
        m.addRunningVM(vm7, n3);
        m.addRunningVM(vm8, n3);
        m.addRunningVM(vm9, n4);
        m.addRunningVM(vm10, n5);

        ShareableResource rc = new ShareableResource("vcpu", 2);
        rc.set(n1, 8);
        rc.set(n2, 8);
        rc.set(n3, 8);
        rc.set(n4, 4);
        rc.set(n5, 4);

        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> setn2 = new HashSet<UUID>(Arrays.asList(n4, n5));

        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 3);
        MaxSpareResources c2 = new MaxSpareResources(setn2, "vcpu", 3);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        c.setContinuous(false);
        c2.setContinuous(false);

        l.add(c);
        l.add(oc);
        l.add(c2);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.getOrigin().getMapping().toString());
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
    public void testCMaxSpareResourcesDiscrete3() throws SolverException {

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
        rc.set(vm2, 2);
        rc.set(vm3, 1);
        rc.set(vm4, 1);
        rc.set(vm5, 3);
        Model mo = new DefaultModel(m);
        mo.attach(rc);
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2, n3));

        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 5);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.getOrigin().getMapping().toString());
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
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
        List<SatConstraint> l = new ArrayList<SatConstraint>();

        Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1, n2, n3));

        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 5);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);

        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.getOrigin().getMapping().toString());
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }

    @Test
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

        Sleeping sleeping = new Sleeping(new HashSet<UUID>(Arrays.asList(vm3)));
        MaxSpareResources c = new MaxSpareResources(setn1, "vcpu", 2);
        Overbook oc = new Overbook(m.getAllNodes(), "vcpu", 1);
        c.setContinuous(true);
        l.add(sleeping);
        l.add(c);
        l.add(oc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMaxSpareResources.Builder());
//        cra.setVerbosity(2);
        cra.setMaxEnd(20);
        ReconfigurationPlan plan = cra.solve(mo, l);

        Assert.assertNotNull(plan);
        Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
        System.out.println(plan.getOrigin().getMapping().toString());
        System.out.println(plan.toString());
        System.out.println(plan.getResult().getMapping().toString());
    }
}
