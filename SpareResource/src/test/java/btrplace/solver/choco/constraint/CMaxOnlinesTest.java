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
import btrplace.model.constraint.MaxOnlines;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.DurationEvaluators;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.BootableNodeModel;
import btrplace.solver.choco.actionModel.ShutdownableNodeModel;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import choco.kernel.solver.ContradictionException;

public class CMaxOnlinesTest implements PremadeElements {
	@Test
	public void discreteMaxonlinesTest() throws SolverException {

		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOnlineNode(n3);

		map.addRunningVM(vm1, n1);
		map.addRunningVM(vm2, n2);
		map.addRunningVM(vm3, n3);

		Model model = new DefaultModel(map);

		Set<UUID> nodes = map.getAllNodes();

		MaxOnlines maxon = new MaxOnlines(nodes, 1);
		List<SatConstraint> constraints = new ArrayList<SatConstraint>();
		constraints.add(maxon);

		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());

		ReconfigurationPlan plan = cra.solve(model, constraints);

		Assert.assertEquals(maxon.isSatisfied(plan.getResult()), Sat.SATISFIED);

		System.out.println(plan.toString());
		System.out.println(plan.getResult().getMapping().toString());
	}

	@Test
	public void discreteMaxonlinesTest2() throws SolverException {

		ShareableResource resources = new ShareableResource("vcpu", 1);
		resources.set(n1, 4);
		resources.set(n2, 8);
		resources.set(n3, 2);
		resources.set(vm4, 2);

		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOnlineNode(n3);

		map.addRunningVM(vm1, n1);
		map.addRunningVM(vm4, n1);
		map.addRunningVM(vm2, n2);
		map.addRunningVM(vm3, n3);

		Model model = new DefaultModel(map);
		model.attach(resources);

		Set<UUID> nodes = map.getAllNodes();

		MaxOnlines maxon = new MaxOnlines(nodes, 2);

		Set<UUID> nodes2 = map.getAllNodes();
		nodes2.remove(n3);
		MaxOnlines maxon2 = new MaxOnlines(nodes2, 1);
		Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

		List<SatConstraint> constraints = new ArrayList<SatConstraint>();
		constraints.add(maxon);
		constraints.add(maxon2);
		constraints.add(overbook);

		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());

		ReconfigurationPlan plan = cra.solve(model, constraints);

		Assert.assertEquals(maxon.isSatisfied(plan.getResult()), Sat.SATISFIED);

		System.out.println(plan.toString());
		System.out.println(plan.getResult().getMapping().toString());
	}


	public void continuousMaxOnlinesTest1() throws SolverException {
		ShareableResource resources = new ShareableResource("vcpu", 1);
		resources.set(n1, 8);
		resources.set(n2, 8);
		resources.set(n3, 4);
		resources.set(vm4, 2);

		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOfflineNode(n3);

		map.addRunningVM(vm1, n1);
		map.addRunningVM(vm4, n1);
		map.addRunningVM(vm2, n2);
		map.addRunningVM(vm3, n2);

		Model model = new DefaultModel(map);
		model.attach(resources);

		Set<UUID> nodes = map.getAllNodes();

		MaxOnlines maxon = new MaxOnlines(nodes, 2);

		Online oncstr = new Online(new HashSet<UUID>(Arrays.asList(n3)));

		Overbook overbook = new Overbook(map.getAllNodes(), "vcpu", 1);

		List<SatConstraint> constraints = new ArrayList<SatConstraint>();

		maxon.setContinuous(true);

		constraints.add(maxon);
		constraints.add(oncstr);
		constraints.add(overbook);

		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(5));
		cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));
		cra.setVerbosity(2);
		cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());

		ReconfigurationPlan plan = cra.solve(model, constraints);
		Assert.assertNotNull(plan);
		System.out.println(plan.toString());
		System.out.println(plan.getResult().getMapping().toString());
	}


	public void continuousMaxOnlinesSimplestTest() throws SolverException {

		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOfflineNode(n2);

		Model model = new DefaultModel(map);

		Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2));

		MaxOnlines maxon = new MaxOnlines(nodes, 1);

		Online oncstr = new Online(new HashSet<UUID>(Arrays.asList(n2)));

		List<SatConstraint> constraints = new ArrayList<SatConstraint>();

		maxon.setContinuous(true);

		constraints.add(maxon);
		constraints.add(oncstr);
		constraints.add(new Offline(new HashSet<UUID>(Arrays.asList(n1))));
		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(5));
		cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));

		cra.setVerbosity(2);
		cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());

		ReconfigurationPlan plan = cra.solve(model, constraints);
		Assert.assertNotNull(plan);
		System.out.println(plan.toString());
		System.out.println(plan.getResult().getMapping().toString());
	}
	

	public void continuousMaxOnlinesSimpleTest() throws SolverException {

		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOfflineNode(n3);

		Model model = new DefaultModel(map);

		Set<UUID> nodes = map.getAllNodes();

		MaxOnlines maxon = new MaxOnlines(nodes, 2);

		Online oncstr = new Online(new HashSet<UUID>(Arrays.asList(n3)));

		List<SatConstraint> constraints = new ArrayList<SatConstraint>();

		maxon.setContinuous(true);

		constraints.add(maxon);
		constraints.add(oncstr);

		ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
		cra.getDurationEvaluators().register(ShutdownNode.class, new ConstantDuration(5));
		cra.getDurationEvaluators().register(BootNode.class, new ConstantDuration(4));

		cra.setVerbosity(2);
		cra.getSatConstraintMapper().register(new CMaxOnlines.Builder());

		ReconfigurationPlan plan = cra.solve(model, constraints);
		Assert.assertNotNull(plan);
		System.out.println(plan.toString());
		System.out.println(plan.getResult().getMapping().toString());
	}
	
		@Test
		public void testNodeHostingEnd() throws SolverException, ContradictionException {

		Mapping map = new DefaultMapping();
		map.addOnlineNode(n1);
		map.addOnlineNode(n2);
		map.addOfflineNode(n3);

		Model model = new DefaultModel(map);

		DurationEvaluators dev = new DurationEvaluators();
		dev.register(ShutdownNode.class, new ConstantDuration(5));
		dev.register(BootNode.class, new ConstantDuration(10));
		ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
					.setDurationEvaluatators(dev)
					.labelVariables()
					.build();
		
		ShutdownableNodeModel shd = (ShutdownableNodeModel) rp.getNodeAction(n1);
		shd.getState().setVal(1);
		
		ShutdownableNodeModel shd2 = (ShutdownableNodeModel) rp.getNodeAction(n2);
		shd2.getState().setVal(0);
		shd2.getStart().setVal(1);
		
		BootableNodeModel bn = (BootableNodeModel) rp.getNodeAction(n3);
		bn.getState().setVal(1);
		bn.getStart().setVal(6);
		
		ReconfigurationPlan p = rp.solve(0, false);
	    Assert.assertNotNull(p);
	    System.out.println(p);
	    Assert.assertEquals(shd.getDuration().getVal(), 0);
	    Assert.assertEquals(shd.getStart().getVal(), 0);
	    Assert.assertEquals(shd.getEnd().getVal(), 0);
	    Assert.assertEquals(shd.getHostingStart().getVal(), 0);
	    Assert.assertEquals(shd.getHostingEnd().getVal(), 16);
	    
	    Assert.assertEquals(shd2.getDuration().getVal(), 5);
	    Assert.assertEquals(shd2.getStart().getVal(), 1);
	    Assert.assertEquals(shd2.getEnd().getVal(), 6);
	    Assert.assertEquals(shd2.getHostingStart().getVal(), 0);
	    Assert.assertEquals(shd2.getHostingEnd().getVal(), 1);
	    
	    Assert.assertEquals(bn.getStart().getVal(), 6);
	    Assert.assertEquals(bn.getDuration().getVal(), 10);
	    Assert.assertEquals(bn.getEnd().getVal(), 16);
	    Assert.assertEquals(bn.getHostingStart().getVal(), 16);
	    Assert.assertEquals(bn.getHostingEnd().getVal(), 16);


	    Assert.assertEquals(p.getSize(), 2);
	    Model res = p.getResult();
	    Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n1));
	    Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n3));
	    Assert.assertTrue(res.getMapping().getOfflineNodes().contains(n2));
	}
	
	@Test
	public void testForcedOnline() throws SolverException, ContradictionException {
	    Mapping map = new DefaultMapping();
	    map.addOnlineNode(n1);
	    map.addOfflineNode(n2);
	    Model mo = new DefaultModel(map);
	    DurationEvaluators dev = new DurationEvaluators();
	    dev.register(ShutdownNode.class, new ConstantDuration(5));
	    dev.register(BootNode.class, new ConstantDuration(10));
	    ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
	    		.setDurationEvaluatators(dev)
	            .labelVariables()
	            .build();
	    ShutdownableNodeModel ma = (ShutdownableNodeModel) rp.getNodeAction(n1);
	    ma.getState().setVal(1);   //stay online

	    //To make the result plan 10 seconds long
	    BootableNodeModel ma2 = (BootableNodeModel) rp.getNodeAction(n2);
	    ma2.getState().setVal(1); //go online

	    ReconfigurationPlan p = rp.solve(0, false);
	    Assert.assertNotNull(p);
	    System.out.println(p);
	    Assert.assertEquals(ma.getDuration().getVal(), 0);
	    Assert.assertEquals(ma.getStart().getVal(), 0);
	    Assert.assertEquals(ma.getEnd().getVal(), 0);
	    Assert.assertEquals(ma.getHostingStart().getVal(), 0);
	    Assert.assertEquals(ma.getHostingEnd().getVal(), 10);


	    Assert.assertEquals(p.getSize(), 1);
	    Model res = p.getResult();
	    Assert.assertTrue(res.getMapping().getOnlineNodes().contains(n1));
	}
}
