package btrplace.model.constraint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.testng.Assert;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.view.ShareableResource;

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

		System.out.println(c);
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

	@Test
	public void testNodeDiscreteIsSatisfied() {
		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOnlineNode(n3);

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
		Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3));

		MinSpareResources msr = new MinSpareResources(nodes, "node", 1);

		Assert.assertEquals(msr.isSatisfied(mo), Sat.UNSATISFIED);

		map.addSleepingVM(vm2, n1);
		map.addSleepingVM(vm3, n1);

		Assert.assertEquals(msr.isSatisfied(mo), Sat.SATISFIED);

	}

}
