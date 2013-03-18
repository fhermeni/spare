package btrplace.solver.choco.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxOnlines;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.NodeActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class CMaxOnlines implements ChocoSatConstraint {

	private final MaxOnlines cstr;

	/**
	 * Make a new constraint
	 * 
	 * @param maxon
	 *            is maxOnlines constraint to rely on
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
		CPSolver solver = rp.getSolver();

		if (cstr.isContinuous()) {
			// The constraint must be already satisfied
			if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
				rp.getLogger()
						.error("The constraint '{}' must be already satisfied to provide a continuous restriction",
								cstr);
				return false;
			} else {
				Mapping map = rp.getSourceModel().getMapping();
				int i = 0;
				int[] nodeIdx = new int[cstr.getInvolvedNodes().size()];
				for (UUID n : cstr.getInvolvedNodes()) {
					nodeIdx[i++] = rp.getNode(n);
				}
				
				List<TaskVar> stateTasks = new ArrayList<TaskVar>();
				IntDomainVar consumption = solver.createIntVar("consumption", IntDomainVar.BOUNDS,	0, cstr.getAmount());
				IntDomainVar cstrAmount = solver.createIntVar("capacity", IntDomainVar.BOUNDS,	cstr.getAmount(), cstr.getAmount());
				IntDomainVar upperbound = solver.createIntVar("upperbound", IntDomainVar.BOUNDS, 0,	cstr.getAmount());
				IntDomainVar[] heights = new IntDomainVar[cstr.getInvolvedNodes().size()];
				
				try {
				for (int idx : nodeIdx) {

					UUID n = rp.getNode(idx);
					NodeActionModel nodeAction = rp.getNodeAction(n);
					
					IntDomainVar onlineDuration = rp.makeDuration("onlineDuration(" + idx + "");
					
					IntDomainVar startTime = solver.createIntVar("online_Start("+n+")", IntDomainVar.BOUNDS, 0, nodeAction.getStart().getSup());
					
					if(map.getOfflineNodes().contains(n)) {
						System.err.println(n + " start at 10");
						nodeAction.getStart().setVal(10);
					}
					else {
						nodeAction.getStart().setVal(5);
						System.err.println(n + " start at 5");	
					}
					stateTasks.add(solver.createTaskVar("online_time(" + n + ")", startTime, 
								nodeAction.getHostingEnd(), onlineDuration));	
					
					heights[idx] = solver.createIntegerConstant("height(" + n + ")", 1);
					
					//solver.createIntVar(ni +" isOnline", IntDomainVar.BOUNDS, 0, 1);
					//BooleanChanneling bc = new BooleanChanneling(nodeAction.getState(), heights[idx], 1);
					//solver.post(bc);
				}
					
								
				
				Cumulative cumulative = new Cumulative(solver, "MaxOnline",
						stateTasks.toArray(new TaskVar[stateTasks.size()]), 
						heights, consumption,
						cstrAmount, upperbound);
				//solver.post(cumulative);
				
				} catch (ContradictionException e) {
					e.printStackTrace();
				}	
				
			}
				
		}

		List<IntDomainVar> nodes_state = new ArrayList<IntDomainVar>(cstr.getInvolvedNodes().size());

		for (UUID ni : cstr.getInvolvedNodes()) {
			nodes_state.add(rp.getNodeAction(ni).getState());
		}

		IntExp sum_of_states = CPSolver
				.sum(nodes_state.toArray(new IntDomainVar[nodes_state.size()]));
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
			return MaxOnlines.class;
		}

	}

}
