package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.constraints.integer.ElementV;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.Set;
import java.util.UUID;

import static btrplace.solver.choco.chocoUtil.ChocoUtils.postIfOnlyIf;
import static btrplace.solver.choco.chocoUtil.ChocoUtils.postImplies;

public class CMaxSpareNode implements ChocoSatConstraint {

    private final MaxSpareNode cstr;
    private final int MAX_TIME = 120;

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

        //TODO: Check with the case too many nodes with too little VM
        CPSolver solver = rp.getSolver();
        int NUMBER_OF_NODE = cstr.getInvolvedNodes().size();
        if (cstr.isContinuous()) {
            // The constraint must be already satisfied
            if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
                rp.getLogger()
                        .error("The constraint '{}' must be already satisfied to provide a continuous restriction",
                                cstr);
                return false;
            } else {            // Start of Continuous Model
                IntDomainVar capacity = solver.createIntegerConstant("capacity", cstr.getAmount());
                IntDomainVar consumption = solver.createBoundIntVar("consum", 0, cstr.getAmount());//minimum consumption of the resource
                IntDomainVar uppBound = rp.getEnd(); // All tasks must be scheduled before this time
                IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_NODE]; // The state of the node
                IntDomainVar[] durations = new IntDomainVar[NUMBER_OF_NODE]; // Online duration
                TaskVar[] taskvars = new TaskVar[NUMBER_OF_NODE]; // Online duration is modeled as a task

                // The start moment of node being idle
                IntDomainVar[] idle_starts = new IntDomainVar[NUMBER_OF_NODE];

                // The end moment of node being idle
                IntDomainVar[] idle_ends = new IntDomainVar[NUMBER_OF_NODE];
                int i = 0;
                for (UUID n : cstr.getInvolvedNodes()) {
                    NodeActionModel na = rp.getNodeAction(n);
                    idle_starts[i] = solver.createBoundIntVar("IS(" + n + ")", 0, MAX_TIME);
                    idle_ends[i] = solver.createBoundIntVar("IE(" + n + ")", 0, MAX_TIME);

                    Set<UUID> vms = rp.getSourceModel().getMapping().getRunningVMs(n);
                    if (!vms.isEmpty()) {
                        for (UUID vm : vms) {
                            VMActionModel vma = rp.getVMAction(vm);
                            Slice c = vma.getCSlice();
                            solver.post(solver.geq(idle_starts[i], c.getEnd()));
                        }
                    } else {
                        solver.post(solver.eq(idle_starts[i], na.getHostingStart()));
                    }


                    for (VMActionModel vma : rp.getVMActions()) {
                        Slice dSlice = vma.getDSlice();
                        if (dSlice == null) continue;
                        IntDomainVar eq = solver.createBooleanVar("eq");
                        postIfOnlyIf(solver, eq, solver.eq(dSlice.getHoster(), rp.getNode(n)));
                        postImplies(solver, eq, solver.leq(idle_ends[i], dSlice.getStart()));
                    }

                    durations[i] = rp.makeDuration("Dur(" + n + ")");
                    heights[i] = solver.makeConstantIntVar(1); // All tasks have to be scheduled
                    taskvars[i] = solver.createTaskVar("Task_" + n, idle_starts[i], idle_ends[i], durations[i]);
                    i++;
                }

                Cumulative cumulative = new Cumulative(solver, "Cumulative", taskvars, heights,
                        consumption, capacity, uppBound);
                solver.post(cumulative);
            }   // END OF CONTINUOUS
        }

        IntDomainVar[] hostVM = rp.getNbRunningVMs();
        IntDomainVar[] vmsOnNodes = new IntDomainVar[NUMBER_OF_NODE]; // Each element is the number of VMs on each node
        IntDomainVar[] idles = new IntDomainVar[NUMBER_OF_NODE];
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (UUID n : cstr.getInvolvedNodes()) {
            vmsOnNodes[i] = solver.createBoundIntVar("nVMs" + n, -1, maxVMs);
            IntDomainVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is 1, otherwise, it equals the number of VMs on that node
            IntDomainVar[] c = new IntDomainVar[]{solver.makeConstantIntVar(-1), hostVM[rp.getNode(n)],
                    state, vmsOnNodes[i]};
            solver.post(new ElementV(c, 0, solver.getEnvironment()));
            // IF number of VMs on a node is 0 -> idle
            idles[i] = solver.createBooleanVar("idle" + n);
            postIfOnlyIf(solver, idles[i], solver.eq(vmsOnNodes[i], 0));
            i++;
        }
        IntExp Sidle = solver.sum(idles);
        solver.post(solver.leq(Sidle, cstr.getAmount()));

        /* // Other way for discrete  (more constraints)
        IntDomainVar[] hostVM = rp.getNbRunningVMs();
        IntDomainVar[] idles = new IntDomainVar[NUMBER_OF_NODE];
        int i = 0;
        for (UUID n : cstr.getInvolvedNodes()) {
            IntDomainVar noVM = solver.createBooleanVar("NoVM" + n);
            idles[i] = solver.createBooleanVar("idle" + n);
            IntDomainVar online = rp.getNodeAction(n).getState();
            IntDomainVar card = hostVM[rp.getNode(n)];
            postIfOnlyIf(solver, noVM, solver.eq(card, 0));
            postIfOnlyIf(solver, idles[i], BooleanFactory.and(online, noVM));
            i++;
        }
        IntExp Sidle = solver.sum(idles);
        solver.post(solver.leq(Sidle, cstr.getAmount()));
        */
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
