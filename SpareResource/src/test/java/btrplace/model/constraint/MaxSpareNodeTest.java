package btrplace.model.constraint;

import btrplace.model.*;
import btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MaxSpareNodeTest {
    @Test
    public void discreteMaxSpareNodeTest() {
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

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);

        ShareableResource rc = new ShareableResource("mem", 1, 1);

        rc.setConsumption(vm2, 2);
        rc.setCapacity(n1, 4);
        rc.setCapacity(n2, 2);
        rc.setCapacity(n3, 2);

        model.attach(rc);
        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2, n3, n4));

        MaxSpareNode msn = new MaxSpareNode(nodes, 1);

        Assert.assertTrue(msn.isSatisfied(model));

        map.addSleepingVM(vm2, n1);
        map.addSleepingVM(vm3, n1);
        Assert.assertTrue(msn.isSatisfied(model));

        map.addSleepingVM(vm4, n3);
        Assert.assertFalse(msn.isSatisfied(model));
    }
}
