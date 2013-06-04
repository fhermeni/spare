package btrplace.model.constraint;

import btrplace.model.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link MaxSpareResources}.
 *
 * @author Tu Huynh Dang
 */
public class MaxSpareResourcesTest {

    @Test
    public void testSatisfiedDiscrete() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        VM vm5 = model.newVM();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);
        ShareableResource rc = new ShareableResource("vcpu", 2, 1);
        model.attach(rc);
        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2, n4));
        MaxSpareResources msr = new MaxSpareResources(nodes, "vcpu", 0);
        Assert.assertFalse(msr.isSatisfied(model));
        map.addRunningVM(vm5, n2);
        Assert.assertTrue(msr.isSatisfied(model));
    }

    @Test
    public void isSatisfiedContinuousModelTest() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        VM vm5 = model.newVM();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);
        map.addRunningVM(vm5, n3);

        ShareableResource rc = new ShareableResource("vcpu", 2, 2);

        rc.setCapacity(n1, 5);
        rc.setCapacity(n2, 5);
        rc.setConsumption(vm4, 1);

        model.attach(rc);

        Set<Node> node1and3 = new HashSet<Node>(Arrays.asList(n1, n3));

        MaxSpareResources msr = new MaxSpareResources(node1and3, "vcpu", 2);
        Overbook oc = new Overbook(map.getAllNodes(), "vcpu", 1);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        Assert.assertTrue(msr.isSatisfied(plan));
        Assert.assertTrue(oc.isSatisfied(plan));

        plan.add(new MigrateVM(vm1, n1, n2, 6, 10));
        Assert.assertFalse(msr.isSatisfied(plan));

        plan.add(new MigrateVM(vm4, n2, n1, 0, 5));
        System.out.println(plan.getOrigin().getMapping().toString());
        System.out.println(plan.getResult().getMapping().toString());
        System.out.println(plan.toString());

        Assert.assertTrue(msr.isSatisfied(plan));
        Assert.assertTrue(oc.isSatisfied(plan));

    }

    @Test(enabled = false)
    public void testMaxSpareResourcesContinuousSimple() throws SolverException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();

        Mapping mapping = new MappingBuilder().on(n1, n2)
                .run(n1, vm1, vm3).run(n2, vm2).build();

        ShareableResource rc = new ShareableResource("vcpu", 1, 1);
        rc.setCapacity(n1, 3);
        rc.setCapacity(n2, 2);

        MappingUtils.fill(mapping, model.getMapping());
        model.attach(rc);
        MaxSpareResources msr = new MaxSpareResources(mapping.getAllNodes(), "vcpu", 2, true);
        Overbook oc = new Overbook(mapping.getAllNodes(), "vcpu", 1);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);
        Assert.assertTrue(msr.isSatisfied(plan));
        Assert.assertTrue(oc.isSatisfied(plan));

        plan.add(new ShutdownNode(n2, 2, 4));
        Assert.assertFalse(msr.isSatisfied(plan));

        plan.add(new MigrateVM(vm2, n2, n1, 0, 2));
        plan.add(new ShutdownVM(vm1, n1, 4, 5));
        System.out.println(plan);
        Assert.assertTrue(msr.isSatisfied(plan));

    }

}
