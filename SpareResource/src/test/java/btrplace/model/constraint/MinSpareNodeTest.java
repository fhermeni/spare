package btrplace.model.constraint;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link MinSpareNode}.
 *
 * @author Tu Huynh Dang
 */

public class MinSpareNodeTest implements PremadeElements {

    @Test
    public void discreteMinSpareNodeTest() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);

        Model mo = new DefaultModel(map);
        ShareableResource rc = new ShareableResource("mem", 1);

        rc.set(vm2, 2);
        rc.set(n1, 4);
        rc.set(n2, 2);
        rc.set(n3, 2);

        mo.attach(rc);
        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));

        MinSpareNode msn = new MinSpareNode(nodes, 1);

        Assert.assertEquals(msn.isSatisfied(mo), Sat.UNSATISFIED);

        map.addSleepingVM(vm2, n1);
        map.addSleepingVM(vm3, n1);

        Assert.assertEquals(msn.isSatisfied(mo), Sat.SATISFIED);

    }

    @Test
    public void continuousMinSpareNodeTest() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);

        Model mo = new DefaultModel(map);
        ShareableResource rc = new ShareableResource("mem", 1);

        rc.set(vm2, 2);
        rc.set(n1, 4);
        rc.set(n2, 2);
        rc.set(n3, 2);

        mo.attach(rc);
        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));

        MinSpareNode msn = new MinSpareNode(nodes, 1);
        Overbook oc = new Overbook(map.getAllNodes(), "mem", 1);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        Assert.assertEquals(oc.isSatisfied(plan), Sat.SATISFIED);

        plan.add(new MigrateVM(vm1, n1, n3, 5, 9));
        Assert.assertEquals(msn.isSatisfied(plan), Sat.UNSATISFIED);
        Assert.assertEquals(oc.isSatisfied(plan), Sat.SATISFIED);

        plan.add(new MigrateVM(vm3, n2, n3, 0, 5));
        Assert.assertEquals(msn.isSatisfied(plan), Sat.SATISFIED);
        Assert.assertEquals(oc.isSatisfied(plan), Sat.SATISFIED);

    }

}
