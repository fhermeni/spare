package btrplace.solver.choco.constraint;

import java.util.Set;
import java.util.UUID;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MinSpareNode;
import btrplace.model.constraint.MinSpareResources;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;

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
	
	public Set<UUID> getMisPlacedVMs(Model m) {
		return m.getMapping().getRunningVMs(cstr.getInvolvedNodes());
	}

	public boolean inject(ReconfigurationProblem arg0) throws SolverException {
		// TODO Auto-generated method stub
		return false;
	}
	
    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
 
        public Class<? extends SatConstraint> getKey() {
            return MinSpareResources.class;
        }

        public CMinSpareResources build(SatConstraint cstr) {
            return new CMinSpareResources((MinSpareResources) cstr);
        }
    }

}
