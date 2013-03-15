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
import btrplace.solver.choco.NodeActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
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

				
				//fake State Tasks for three nodes. Order {n1, n3, n2}
				TaskVar tv1 = new TaskVar(solver, 0, "online1", solver.createIntegerConstant("st", 0), solver.createIntegerConstant("ed1", 3), solver.createIntegerConstant("duration1", 3));
				TaskVar tv2 = new TaskVar(solver, 0, "online2", solver.createIntegerConstant("st2", 0), solver.createIntegerConstant("ed2", 1), solver.createIntegerConstant("duration2", 1));
				TaskVar tv3 = new TaskVar(solver, 0, "online3", solver.createIntegerConstant("st3", 2), solver.createIntegerConstant("ed3", 3), solver.createIntegerConstant("duration3", 1));
				TaskVar[] stateTasks = {tv1, tv3, tv2};
				
				IntDomainVar consumption = solver.createIntVar("consumption", IntDomainVar.BOUNDS,	0, cstr.getAmount());
				IntDomainVar cstrAmount = solver.createIntVar("capacity", IntDomainVar.BOUNDS,	cstr.getAmount(), cstr.getAmount());
				IntDomainVar upperbound = solver.createIntVar("upperbound", IntDomainVar.BOUNDS, 0,	cstr.getAmount());
				IntDomainVar[] heights = new IntDomainVar[cstr.getInvolvedNodes().size()];
				
				int i = 0;
				int[] nodeIdx = new int[cstr.getInvolvedNodes().size()];
				for (UUID n : cstr.getInvolvedNodes()) {
					nodeIdx[i++] = rp.getNode(n);
				}

				for (int idx : nodeIdx) {

					UUID n = rp.getNode(idx);
					NodeActionModel nodeAction = rp.getNodeAction(n);
					
					

//					IntDomainVar onlineDuration = rp.makeDuration("onlineDuration(" + idx + "");
//					
//					solver.post(solver.eq(onlineDuration, CPSolver.minus(nodeAction.getHostingEnd(), 
//							                                              nodeAction.getHostingStart())));
					
//					stateTasks.add(solver.createTaskVar("online(" + n + ")", nodeAction.getHostingStart(), 
//							nodeAction.getHostingEnd(), onlineDuration));

					 heights[idx] = nodeAction.getState();
//					heights[idx] = solver.createIntegerConstant("State" + i, 1);
				}
				
				Cumulative cumulative = new Cumulative(solver, "MaxOnlines",
						stateTasks,//.toArray(new TaskVar[stateTasks.size()]), 
						heights, consumption,
						cstrAmount, upperbound);
				solver.post(cumulative);

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
