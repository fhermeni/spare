package btrplace.model.constraint;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.view.ShareableResource;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MaxSpareNodeTest implements PremadeElements {
    @Test
    public void discreteMaxSpareNodeTest() {
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

        MaxSpareNode msn = new MaxSpareNode(nodes, 1);

        Assert.assertEquals(msn.isSatisfied(mo), Sat.SATISFIED);

        map.addSleepingVM(vm2, n1);
        map.addSleepingVM(vm3, n1);
        Assert.assertEquals(msn.isSatisfied(mo), Sat.SATISFIED);

        map.addSleepingVM(vm4, n3);
        Assert.assertEquals(msn.isSatisfied(mo), Sat.UNSATISFIED);
    }
}
