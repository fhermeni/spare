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

import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.view.ShareableResource;

/**
 * Unit tests for {@link MinSpareResources}.
 * 
 * @author Tu Huynh Dang
 */

public class MinSpareResourcesTest extends ConstraintTestMaterial {

	@Test
	public void testMinSpareResources() {

	}

	@Test
	public void testIsSatisfiedModel() {

	}

	@Test
	public void testInstantiation() {
		Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
		MinSpareResources c = new MinSpareResources(s, "ucpu", 3);
		Assert.assertEquals(s, c.getInvolvedNodes());
		Assert.assertEquals("ucpu", c.getResource());
		Assert.assertEquals(3, c.getAmount());
		Assert.assertTrue(c.getInvolvedVMs().isEmpty());
		Assert.assertFalse(c.toString().contains("null"));
		Assert.assertFalse(c.isContinuous());
		Assert.assertTrue(c.setContinuous(true));
		Assert.assertTrue(c.isContinuous());

		c = new MinSpareResources(s, "ucpu", 3, true);
		Assert.assertTrue(c.isContinuous());

		// System.out.println(c);
	}

	@Test
	public void testDiscreteIsSatisfied() {
		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOnlineNode(n3);

		map.addRunningVM(vm1, n1);
		map.addRunningVM(vm2, n1);
		map.addRunningVM(vm3, n2);
		map.addRunningVM(vm4, n3);

		Model mo = new DefaultModel(map);
		ShareableResource rc = new ShareableResource("cpu", 1);

		rc.set(vm2, 2);
		rc.set(n1, 4);
		rc.set(n2, 2);
		rc.set(n3, 2);
		mo.attach(rc);
		Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3));

		MinSpareResources msr = new MinSpareResources(nodes, "cpu", 3);

		Assert.assertEquals(msr.isSatisfied(mo), Sat.SATISFIED);
		rc.set(vm1, 3);
		Assert.assertEquals(msr.isSatisfied(mo), Sat.UNSATISFIED);

		map.addSleepingVM(vm2, n1);
		map.addSleepingVM(vm3, n1);

		Assert.assertEquals(msr.isSatisfied(mo), Sat.SATISFIED);

	}

}
