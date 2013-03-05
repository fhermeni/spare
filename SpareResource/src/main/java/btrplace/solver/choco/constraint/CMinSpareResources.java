package btrplace.solver.choco.constraint;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Model;
import btrplace.model.constraint.MinSpareResources;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ReconfigurationProblem;

public class CMinSpareResources implements ChocoSatConstraint {

	private MinSpareResources msr;

	/**
	 * Make a new constraint.
	 * 
	 * @param msr
	 *            is minSpareResources constraint to rely on
	 */
	public CMinSpareResources(MinSpareResources msr) {
		super();
		this.msr = msr;
	}

	public Set<UUID> getMisPlacedVMs(Model m) {
		return m.getMapping().getRunningVMs(msr.getInvolvedNodes());
	}

	public boolean inject(ReconfigurationProblem arg0) throws SolverException {
		// TODO Auto-generated method stub
		Collection<UUID> nodes = msr.getInvolvedNodes();

		return false;
	}

}
