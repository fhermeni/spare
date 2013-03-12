package btrplace.solver.choco.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxOnlines;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class CMaxOnlines implements ChocoSatConstraint {
	
	

	private final MaxOnlines cstr;
	
	/**
	 * Make a new constraint
	 * 
	 * @param maxon
	 * 			is maxOnlines constraint to rely on
	 * 
	 */
	public CMaxOnlines(MaxOnlines maxon) {
		super();
		cstr = maxon;
	}

	@Override
	public Set<UUID> getMisPlacedVMs(Model model) {
		return model.getMapping().getRunningVMs(cstr.getInvolvedNodes());
	}

	@Override
	public boolean inject(ReconfigurationProblem rp) throws SolverException {
		
		List<IntDomainVar> nodes_state = new ArrayList<IntDomainVar>(cstr.getInvolvedNodes().size());
		
		for(UUID ni : cstr.getInvolvedNodes()) {
			nodes_state.add(rp.getNodeAction(ni).getState());
		}
		
		CPSolver solver = rp.getSolver();
		IntExp sum_of_states = CPSolver.sum(nodes_state.toArray(new IntDomainVar[nodes_state.size()]));
		solver.post(solver.leq(sum_of_states, cstr.getAmount()));
		
		return true;
	}
	
	/**
	 * The builder associated to this constraint 
	 * 
	 * @author Tu Huynh Dang
	 *
	 */
	public static class Builder implements ChocoSatConstraintBuilder {

		@Override
		public ChocoSatConstraint build(SatConstraint cstr) {
			return new CMaxOnlines((MaxOnlines) cstr);
		}

		@Override
		public Class<? extends SatConstraint> getKey() {
			return MaxOnlines.class ;
		}

	}

}
