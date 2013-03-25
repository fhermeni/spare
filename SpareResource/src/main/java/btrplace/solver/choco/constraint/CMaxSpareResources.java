package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxSpareResources;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.view.CShareableResource;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CMaxSpareResources implements ChocoSatConstraint {

    private final MaxSpareResources cstr;

    /**
     * Make a new constraint.
     *
     * @param msr is maxSpareResources constraint to rely on
     */
    public CMaxSpareResources(MaxSpareResources msr) {
        super();
        this.cstr = msr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return m.getMapping().getRunningVMs();
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {

        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE
                + cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "No resource associated to identifier '"
                    + cstr.getResource() + "'");
        }

        CPSolver solver = rp.getSolver();
        if (cstr.isContinuous()) {
            // The constraint must be already satisfied
            if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
                rp.getLogger()
                        .error("The constraint '{}' must be already satisfied to provide a continuous restriction",
                                cstr);
                return false;
            } else {
                Mapping mapping = rp.getSourceModel().getMapping();
                int[] alias = new int[cstr.getInvolvedNodes().size()];
                int i = 0;
                int capas = 0;
                for (UUID n : cstr.getInvolvedNodes()) {
                    alias[i++] = rp.getNode(n);
                    if (mapping.getOnlineNodes().contains(n)) {
                        capas += rcm.getSourceResource().get(n);
                    }
                }

                TIntArrayList cUse = new TIntArrayList();
                List<IntDomainVar> dSlices = new ArrayList<IntDomainVar>();
                List<IntDomainVar> heights = new ArrayList<IntDomainVar>();
                List<TaskVar> taskVars = new ArrayList<TaskVar>();

                IntDomainVar uppBound = rp.getEnd();
                for (UUID vmId : rp.getVMs()) {
                    VMActionModel a = rp.getVMAction(vmId);
                    Slice c = a.getCSlice();
                    Slice d = a.getDSlice();
                    if (c != null) {
                        int val = rcm.getSourceResource().get(vmId);
                        cUse.add(val);
                    }
                    if (d != null) {
                        dSlices.add(rcm.getVMsAllocation(rp.getVM(vmId)));
                        heights.add(rcm.getVMsAllocation(rp.getVM(vmId)));
                        taskVars.add(solver.createTaskVar("dTask_" + vmId, d.getStart(), d.getEnd(), d.getDuration()));
                    }
                }
                rp.getAliasedCumulativesBuilder().add(capas, cUse.toArray(),
                        dSlices.toArray(new IntDomainVar[dSlices.size()]), alias);

                int csum = 0;
                for (UUID vmId : rp.getVMs()) {
                    int currentNode = rp.getCurrentVMLocation(rp.getVM(vmId));
                    if (cstr.getInvolvedNodes().contains(rp.getNode(currentNode))) {
                        csum += cUse.get(rp.getVM(vmId));
                    }
                }
                IntDomainVar capacity = solver.makeConstantIntVar(capas - csum);
                IntDomainVar consumption = solver.createBoundIntVar("consumption", capas - cstr.getAmount(), capas);

                Cumulative cumulative = new Cumulative(solver, "Cumulative", taskVars.toArray(new TaskVar[taskVars.size()]),
                        heights.toArray(new IntDomainVar[heights.size()]), consumption, capacity, uppBound);
                solver.post(cumulative);

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

        CPSolver s = solver;

        // sum all capacity of involved nodes
        IntExp sumcap = s.scalar(caps, nodes_state.toArray(new IntDomainVar[nodes_state.size()]));

        // sum all resource consumption of involved nodes
        IntExp sumcon = CPSolver.sum(vs.toArray(new IntDomainVar[vs.size()]));

        // number of spare resources is the difference between capacity and
        // usage
        IntExp spare = CPSolver.minus(sumcap, sumcon);

        s.post(s.leq(spare, cstr.getAmount()));

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
            return MaxSpareResources.class;
        }

        @Override
        public CMaxSpareResources build(SatConstraint cstr) {
            return new CMaxSpareResources((MaxSpareResources) cstr);
        }
    }
}
