/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint;

import btrplace.model.*;
import btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link MinSpareResources}.
 *
 * @author Tu Huynh Dang
 */

public class MinSpareResourcesTest {

    @Test
    public void testDiscreteIsSatisfied() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);

        ShareableResource rc = new ShareableResource("cpu", 1, 1);

        rc.setConsumption(vm2, 2);
        rc.setCapacity(n1, 4);
        rc.setCapacity(n2, 2);
        rc.setCapacity(n3, 2);
        model.attach(rc);
        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2, n3));

        MinSpareResources msr = new MinSpareResources(nodes, "cpu", 3);

        Assert.assertTrue(msr.isSatisfied(model));
        rc.setConsumption(vm1, 3);
        Assert.assertFalse(msr.isSatisfied(model));

        map.addSleepingVM(vm2, n1);
        map.addSleepingVM(vm3, n1);

        Assert.assertTrue(msr.isSatisfied(model));

    }

}
