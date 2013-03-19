package btrplace.model.constraint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

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

/**
 * Unit tests for {@link MaxSpareResources}.
 * 
 * @author Tu Huynh Dang
 */
public class MaxSpareResourcesTest implements PremadeElements {

	@Test
	public void maxSpareResourcesSetUUIDStringint() {
		Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
		MaxSpareResources c = new MaxSpareResources(s, "vcpu", 3);
		Assert.assertEquals(s, c.getInvolvedNodes());
		Assert.assertEquals("vcpu", c.getResource());
		Assert.assertEquals(3, c.getAmount());
		Assert.assertTrue(c.getInvolvedVMs().isEmpty());
		Assert.assertFalse(c.toString().contains("null"));
		Assert.assertFalse(c.isContinuous());
		Assert.assertTrue(c.setContinuous(true));
		Assert.assertTrue(c.isContinuous());

	}

	@Test
	public void maxSpareResourcesCollectionUUIDStringintboolean() {
		Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
		MaxSpareResources c = new MaxSpareResources(s, "vcpu", 3, true);
		Assert.assertTrue(c.isContinuous());
	}

	@Test
	public void isSatisfiedDiscreteModelTest() {
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
		ShareableResource rc = new ShareableResource("vcpu", 2);

		rc.set(n1, 4);
		rc.set(n2, 4);

		mo.attach(rc);
		Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3));

		MaxSpareResources msr = new MaxSpareResources(nodes, "vcpu", 2);

		Assert.assertEquals(msr.isSatisfied(mo), Sat.SATISFIED);
		rc.set(vm1, 1);
		Assert.assertEquals(msr.isSatisfied(mo), Sat.UNSATISFIED);

		map.addRunningVM(vm5, n2);

		Assert.assertEquals(msr.isSatisfied(mo), Sat.SATISFIED);
	}

	@Test
	public void isSatisfiedContinuousModelTest() {
		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOnlineNode(n3);
		map.addOfflineNode(n4);
		map.addRunningVM(vm1, n1);
		map.addRunningVM(vm2, n1);
		map.addRunningVM(vm3, n2);
		map.addRunningVM(vm4, n2);
		map.addRunningVM(vm5, n3);

		Model mo = new DefaultModel(map);
		ShareableResource rc = new ShareableResource("vcpu", 2);

		rc.set(n1, 5);
		rc.set(n2, 5);
		rc.set(vm4, 1);

		mo.attach(rc);

		Set<UUID> node1and3 = new HashSet<UUID>(Arrays.asList(n1, n3));

		MaxSpareResources msr = new MaxSpareResources(node1and3, "vcpu", 2);
		Overbook oc = new Overbook(map.getAllNodes(), "vcpu", 1);

		ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
		Assert.assertEquals(msr.isSatisfied(plan), Sat.SATISFIED);
		Assert.assertEquals(oc.isSatisfied(plan), Sat.SATISFIED);

		plan.add(new MigrateVM(vm1, n1, n2, 6, 10));
		Assert.assertEquals(msr.isSatisfied(plan), Sat.UNSATISFIED);

		plan.add(new MigrateVM(vm4, n2, n1, 0, 5));
		System.out.println(plan.getOrigin().getMapping().toString());
		System.out.println(plan.getResult().getMapping().toString());
		System.out.println(plan.toString());

		Assert.assertEquals(msr.isSatisfied(plan), Sat.SATISFIED);
		Assert.assertEquals(oc.isSatisfied(plan), Sat.SATISFIED);

	}
}
