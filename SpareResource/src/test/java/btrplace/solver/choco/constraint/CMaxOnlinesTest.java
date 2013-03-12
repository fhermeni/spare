package btrplace.solver.choco.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.testng.annotations.Test;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.SatConstraint.Sat;
import btrplace.model.constraint.MaxOnlines;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.test.PremadeElements;

public class CMaxOnlinesTest implements PremadeElements {

   @Test
  public void DiscreteMaxonlinesTest() throws SolverException {
	  
	    Mapping map = new DefaultMapping();
	    map.addOnlineNode(n1);
	    map.addOnlineNode(n2);
	    map.addOnlineNode(n3);
	    
	    map.addRunningVM(vm1, n1);
	    map.addRunningVM(vm2, n2);
	    map.addRunningVM(vm3, n3);
	    
	    Model model = new DefaultModel(map);
	    
	    Set<UUID> nodes = map.getAllNodes();
	    
		MaxOnlines maxon = new MaxOnlines(nodes , 1);
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
   public void DiscreteMaxonlinesTest2() throws SolverException {
	   	
	   ShareableResource resources = new ShareableResource("vcpu", 1);
	    resources.set(n1, 4);
	    resources.set(n2, 8);
	   	resources.set(n3, 2);
	   	resources.set(vm4,2);
	   	
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
 		MaxOnlines maxon2 = new MaxOnlines(nodes2 , 1);
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
}
