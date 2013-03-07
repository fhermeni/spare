package btrplace.solver.choco.constraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MinSpareResources;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

public class CMinSpareResourcesTest extends ConstraintTestMaterial {
		
	@Test
	public void testCMinSpareResources() throws SolverException {
			
		Mapping m = new DefaultMapping();

		m.addOnlineNode(n1);
		m.addOnlineNode(n2);
		m.addOnlineNode(n3);

		m.addRunningVM(vm1, n1);
		m.addRunningVM(vm2, n2);
		m.addRunningVM(vm3, n1);
		m.addRunningVM(vm4, n2);
		m.addRunningVM(vm5, n3);

		btrplace.model.view.ShareableResource rc = new ShareableResource("cpu",	5);
		rc.set(vm1, 2);
		rc.set(vm2, 4);
		rc.set(vm3, 3);
		rc.set(vm4, 1);
		rc.set(vm5, 2);
		Model mo = new DefaultModel(m);
		mo.attach(rc);
		List<SatConstraint> l = new ArrayList<SatConstraint>();
		
		Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1,n2));
		
		MinSpareResources c = new MinSpareResources(setn1, "cpu", 3);
		
		c.setContinuous(false);
		
		l.add(c);
		
		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
		ReconfigurationPlan plan = cra.solve(mo, l);

		Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
		System.out.println(plan.getResult().getMapping().toString());
	}

	@Test
	public void testGetMisPlacedVMs() {
		Mapping m = new DefaultMapping();

		m.addOnlineNode(n1);
		m.addOnlineNode(n2);
		m.addOnlineNode(n3);

		m.addRunningVM(vm1, n1);
		m.addRunningVM(vm2, n2);
		m.addRunningVM(vm3, n1);
		m.addRunningVM(vm4, n2);
		// m.addRunningVM(vm5, n3);

		btrplace.model.view.ShareableResource rc = new ShareableResource("cpu",5);
		rc.set(vm1, 2);
		rc.set(vm2, 4);
		rc.set(vm3, 3);
		rc.set(vm4, 1);
		rc.set(vm5, 5);
		Model mo = new DefaultModel(m);
		mo.attach(rc);
		MinSpareResources c = new MinSpareResources(m.getAllNodes(), "cpu", 3);
		CMinSpareResources cc = new CMinSpareResources(c);

		Assert.assertFalse(cc.getMisPlacedVMs(mo).isEmpty());

		Assert.assertTrue(!cc.getMisPlacedVMs(mo).contains(vm5));
	}

	@Test
	public void testInject() throws SolverException {
		Mapping m = new DefaultMapping();

		m.addOnlineNode(n1);
		m.addOnlineNode(n2);
		m.addOnlineNode(n3);

		m.addRunningVM(vm1, n1);
		m.addRunningVM(vm2, n2);
		m.addRunningVM(vm3, n1);
		m.addRunningVM(vm4, n2);
		m.addRunningVM(vm5, n3);

		btrplace.model.view.ShareableResource rc = new ShareableResource("cpu",	5);
		rc.set(vm1, 2);
		rc.set(vm2, 4);
		rc.set(vm3, 3);
		rc.set(vm4, 1);
		rc.set(vm5, 2);
		Model mo = new DefaultModel(m);
		mo.attach(rc);
		List<SatConstraint> l = new ArrayList<SatConstraint>();
		
		Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1,n2));
		
			
		MinSpareResources c = new MinSpareResources(setn1, "cpu", 3);
		MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "cpu",1);
		
		c.setContinuous(false);
		c2.setContinuous(false);
		
		
		l.add(c);
		l.add(c2);
		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
		ReconfigurationPlan plan = cra.solve(mo, l);

		//Assert.assertNotNull(plan);
		System.out.println(plan);

		Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
		
	
		System.out.println(plan.getResult().getMapping().toString());
		
	}
	
	@Test
	public void testContinuousCMinSpareResources() throws SolverException {
		Mapping m = new DefaultMapping();

		m.addOnlineNode(n1);
		m.addOnlineNode(n2);
		m.addOnlineNode(n3);

		m.addRunningVM(vm1, n1);
		m.addRunningVM(vm3, n1);
		m.addRunningVM(vm2, n2);
		m.addRunningVM(vm4, n2);
		m.addRunningVM(vm5, n3);
		m.addRunningVM(vm6, n3);
		m.addReadyVM(vm7);

		btrplace.model.view.ShareableResource rc = new ShareableResource("cpu",	5);
		rc.set(vm1, 2);
		rc.set(vm2, 2);
		rc.set(vm3, 1);
		rc.set(vm4, 1);
		rc.set(vm5, 2);
		rc.set(vm6, 2);
		rc.set(vm7, 2);
		rc.set(n3,5);
		Model mo = new DefaultModel(m);
		mo.attach(rc);
		
		List<SatConstraint> l = new ArrayList<SatConstraint>();
		
		Set<UUID> setn1 = new HashSet<UUID>(Arrays.asList(n1,n2));
		
		Running cr = new Running(new HashSet<UUID>(Arrays.asList(vm7)));
		MinSpareResources c = new MinSpareResources(setn1,"cpu", 3);
		MinSpareResources c2 = new MinSpareResources(new HashSet<UUID>(Arrays.asList(n2)), "cpu",1);
		Overbook oc = new Overbook(m.getAllNodes(), "cpu", 1);
		cr.setContinuous(true);
		c.setContinuous(true);
		c2.setContinuous(true);
		
		l.add(oc);
		l.add(cr);
		l.add(c);
		l.add(c2);
		
		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());
		ReconfigurationPlan plan = cra.solve(mo, l);
		


		Assert.assertEquals(c.isSatisfied(plan.getResult()), Sat.SATISFIED);
		
	
		System.out.println(plan.getResult().getMapping().toString());
		
		
		
	}

}
