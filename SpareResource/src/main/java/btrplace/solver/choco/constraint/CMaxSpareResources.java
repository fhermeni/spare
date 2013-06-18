package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.MaxSpareResources;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.view.CShareableResource;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public Set<VM> getMisPlacedVMs(Model m) {
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
        // get future state of involved nodes
        List<IntDomainVar> nodes_state = new ArrayList<IntDomainVar>(cstr.getInvolvedNodes().size());
        for (Node ni : cstr.getInvolvedNodes()) {
            nodes_state.add(rp.getNodeAction(ni).getState());
        }

        // get future resource usages of each involved nodes
        List<IntDomainVar> vs = new ArrayList<IntDomainVar>();

        // caps is capacity of all involved nodes
        int[] caps = new int[nodes_state.size()];
        int i = 0;
        for (Node u : cstr.getInvolvedNodes()) {
            vs.add(rcm.getVirtualUsage()[rp.getNode(u)]);
            caps[i++] = rcm.getSourceResource().getCapacity(u);
        }
        // sum all capacity of involved nodes
        IntExp capacity_discrete = solver.scalar(caps, nodes_state.toArray(new IntDomainVar[nodes_state.size()]));

        // sum all resource consumption of involved nodes
        IntExp total_consumption = CPSolver.sum(vs.toArray(new IntDomainVar[vs.size()]));


        //Continuous restriction: The constraint must be already satisfied
        if (cstr.isContinuous()) if (!cstr.isSatisfied(rp.getSourceModel())) {
            System.err.printf("The constraint '{}' must be already satisfied to provide a continuous restriction",
                    cstr);
            return false;
        } else {
            List<TaskVar> taskVars = new ArrayList<TaskVar>();
            List<IntDomainVar> heights = new ArrayList<IntDomainVar>();
//            IntDomainVar capa_cont = solver.createBoundIntVar("capacity", 0, Integer.MAX_VALUE);
//            solver.post(solver.eq(capa_cont, capacity_discrete));
            int sum_of_caps = 0;
            for (int j : caps) sum_of_caps += j;
            IntDomainVar capa_cont = solver.makeConstantIntVar(sum_of_caps);
            IntDomainVar min_consumption = solver.createBoundIntVar("min_consumption", 0, Integer.MAX_VALUE);
            solver.post(solver.eq(min_consumption, solver.minus(capa_cont, cstr.getAmount())));

            for (VM vmId : rp.getVMs()) {
                VMActionModel a = rp.getVMAction(vmId);
                Slice c = a.getCSlice();
                Slice d = a.getDSlice();
                if (c != null) {
                    heights.add(rcm.getVMsAllocation(rp.getVM(vmId)));   // It manipulates the ShareResource as well
                    taskVars.add(solver.createTaskVar("cTask_" + vmId, c.getStart(), c.getEnd(), c.getDuration()));
                }
                if (d != null) {
                    heights.add(rcm.getVMsAllocation(rp.getVM(vmId))); // It manipulates the ShareResource as well
                    taskVars.add(solver.createTaskVar("dTask_" + vmId, d.getStart(), d.getEnd(), d.getDuration()));
                }
            }

          /*  for (UUID ni : cstr.getInvolvedNodes()) {
                NodeActionModel na = rp.getNodeAction(ni);
                heights.add(solver.makeConstantIntVar(caps[rp.getNode(ni)]));
                taskVars.add(solver.createTaskVar("NodeAct_" + ni, na.getStart(), na.getEnd(), na.getDuration()));
            }*/

            Cumulative maxsr = new Cumulative(solver, "maxsr", taskVars.toArray(new TaskVar[taskVars.size()]),
                    heights.toArray(new IntDomainVar[heights.size()]), min_consumption, capa_cont, rp.getEnd());
            solver.post(maxsr);
        }

        // Discrete restriction: number of spare resources is the difference between capacity and usage
        IntExp spare = CPSolver.minus(capacity_discrete, total_consumption);
        solver.post(solver.leq(spare, cstr.getAmount()));

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
