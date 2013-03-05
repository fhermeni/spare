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

/**
 * Unit tests for {@link MinSpareNode}.
 * 
 * @author Tu Huynh Dang
 */

public class MinSpareNodeTest extends ConstraintTestMaterial {

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

		MinSpareNode msn = new MinSpareNode(nodes, 1);

		Assert.assertEquals(msn.isSatisfied(mo), Sat.UNSATISFIED);

		map.addSleepingVM(vm2, n1);
		map.addSleepingVM(vm3, n1);

		Assert.assertEquals(msn.isSatisfied(mo), Sat.SATISFIED);

	}

}
