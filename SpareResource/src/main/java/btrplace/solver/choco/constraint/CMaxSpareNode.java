package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;
import java.util.UUID;

public class CMaxSpareNode implements ChocoSatConstraint {

    private final MaxSpareNode cstr;

    /**
     * Make a new constraint.
     *
     * @param msn is maxSpareNode constraint to rely on
     */
    public CMaxSpareNode(MaxSpareNode msn) {
        super();
        cstr = msn;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return m.getMapping().getRunningVMs();
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
        solver.post(solver.leq(idle, cstr.getAmount()));

        return true;
    }

    /**
     * The builder associated to this constraint
     */
    public static class Builder implements ChocoSatConstraintBuilder {

        @Override
        public Class<? extends SatConstraint> getKey() {
            return MaxSpareNode.class;
        }

        @Override
        public CMaxSpareNode build(SatConstraint cstr) {
            return new CMaxSpareNode((MaxSpareNode) cstr);
        }
    }

}
