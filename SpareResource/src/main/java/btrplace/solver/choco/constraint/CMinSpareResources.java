package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.constraint.MinSpareResources;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.view.CShareableResource;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CMinSpareResources implements ChocoSatConstraint {

    private final MinSpareResources cstr;

    /**
     * Make a new constraint.
     *
     * @param msr is minSpareResources constraint to rely on
     */
    public CMinSpareResources(MinSpareResources msr) {
        super();
        this.cstr = msr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return m.getMapping().getRunningVMs(cstr.getInvolvedNodes());
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE
                + cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "No resource associated to identifier '"
                    + cstr.getResource() + "'");
        }

        if (cstr.isContinuous()) {
            // The constraint must be already satisfied
            if (!cstr.isSatisfied(rp.getSourceModel())) {
                rp.getLogger()
                        .error("The constraint '{}' must be already satisfied to provide a continuous restriction",
                                cstr);
                return false;
            } else {
                int[] alias = new int[cstr.getInvolvedNodes().size()];
                int i = 0;
                int capas = 0;
                for (UUID n : cstr.getInvolvedNodes()) {
                    alias[i++] = rp.getNode(n);
                    capas += rcm.getSourceResource().get(n);
                }

                TIntArrayList cUse = new TIntArrayList();
                List<IntDomainVar> dUse = new ArrayList<IntDomainVar>();

                for (UUID vmId : rp.getVMs()) {
                    VMActionModel a = rp.getVMAction(vmId);
                    Slice c = a.getCSlice();
                    Slice d = a.getDSlice();
                    if (c != null) {
                        cUse.add(rcm.getSourceResource().get(vmId));
                    }
                    if (d != null) {
                        dUse.add(rcm.getVMsAllocation(rp.getVM(vmId)));
                    }
                }
                rp.getAliasedCumulativesBuilder().add(capas - cstr.getAmount(), cUse.toArray(),
                        dUse.toArray(new IntDomainVar[dUse.size()]), alias);

            }
        }
        // get future state of involved nodes
        List<IntDomainVar> nodes_state = new ArrayList<IntDomainVar>(cstr.getInvolvedNodes().size());
        for (UUID ni : cstr.getInvolvedNodes()) {
            nodes_state.add(rp.getNodeAction(ni).getState());
        }

        // get future resource usages of each involved nodes
        List<IntDomainVar> vs = new ArrayList<IntDomainVar>();

        // caps is capacity of all involved nodes
        int[] caps = new int[nodes_state.size()];
        int i = 0;
        for (UUID u : cstr.getInvolvedNodes()) {
            vs.add(rcm.getVirtualUsage()[rp.getNode(u)]);
            caps[i++] = rcm.getSourceResource().get(u);
        }

        CPSolver s = rp.getSolver();

        // sum all capacity of involved nodes
        IntExp sumcap = s.scalar(caps, nodes_state.toArray(new IntDomainVar[nodes_state.size()]));

        // sum all resource consumption of involved nodes
        IntExp sumcon = CPSolver.sum(vs.toArray(new IntDomainVar[vs.size()]));

        // number of spare resources is the difference between capacity and
        // usage
        IntExp spare = CPSolver.minus(sumcap, sumcon);

        s.post(s.geq(spare, cstr.getAmount()));

        return true;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to this constraint
     */
    public static class Builder implements ChocoSatConstraintBuilder {

        @Override
        public Class<? extends SatConstraint> getKey() {
            return MinSpareResources.class;
        }

        @Override
        public CMinSpareResources build(SatConstraint cstr) {
            return new CMinSpareResources((MinSpareResources) cstr);
        }
    }
}
