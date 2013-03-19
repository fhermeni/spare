package btrplace.solver.choco.constraint;

import java.util.Set;
import java.util.UUID;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MinSpareNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class CMinSpareNode implements ChocoSatConstraint {

	private final MinSpareNode cstr;

	/**
	 * Make a new constraint.
	 * 
	 * @param msn
	 *            is minSpareNode constraint to rely on
	 */
	public CMinSpareNode(MinSpareNode msn) {
		super();
		cstr = msn;
	}

	@Override
	public Set<UUID> getMisPlacedVMs(Model m) {
		return m.getMapping().getRunningVMs(cstr.getInvolvedNodes());
	}

	@Override
	public boolean inject(ReconfigurationProblem rp) throws SolverException {

		if (cstr.isContinuous()) {
			// The constraint must be already satisfied
			if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
				rp.getLogger()
						.error("The constraint '{}' must be already satisfied to provide a continuous restriction",
								cstr);
				return false;
			} else {

			}
		}

		IntDomainVar[] nodes_state = rp.getNbRunningVMs();
		IntDomainVar[] nodeVM = new IntDomainVar[cstr.getInvolvedNodes().size()];

		int i = 0;

		for (UUID n : cstr.getInvolvedNodes()) {
			nodeVM[i++] = nodes_state[rp.getNode(n)];
		}
		CPSolver solver = rp.getSolver();
		IntDomainVar idle = solver.createBoundIntVar("Nidles", 0, cstr.getInvolvedNodes().size());

		solver.post(solver.occurence(nodeVM, idle, 0));
		solver.post(solver.geq(idle, cstr.getAmount()));

		return true;
	}

	/**
	 * The builder associated to this constraint
	 */
	public static class Builder implements ChocoSatConstraintBuilder {

		@Override
		public Class<? extends SatConstraint> getKey() {
			return MinSpareNode.class;
		}

		@Override
		public CMinSpareNode build(SatConstraint cstr) {
			return new CMinSpareNode((MinSpareNode) cstr);
		}
	}

}
