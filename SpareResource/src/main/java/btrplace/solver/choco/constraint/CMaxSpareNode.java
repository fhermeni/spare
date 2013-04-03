package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.constraints.integer.ElementV;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.Set;
import java.util.UUID;

import static btrplace.solver.choco.chocoUtil.ChocoUtils.postImplies;

public class CMaxSpareNode implements ChocoSatConstraint {

    private final MaxSpareNode cstr;
    private final int MAX_TIME = 3600;

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
        int NUMBER_OF_TASK = cstr.getInvolvedNodes().size();
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
                IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_TASK]; // The state of the node
                IntDomainVar[] durations = new IntDomainVar[NUMBER_OF_TASK]; // Online duration
                TaskVar[] taskvars = new TaskVar[NUMBER_OF_TASK]; // Online duration is modeled as a task

                // The start moment of node being idle
                IntDomainVar[] idle_starts = new IntDomainVar[NUMBER_OF_TASK];

                // The end moment of node being idle
                IntDomainVar[] idle_ends = new IntDomainVar[NUMBER_OF_TASK];

                for (UUID n : cstr.getInvolvedNodes()) {
                    int i = rp.getNode(n);
                    Idle idleNode = new Idle(rp, n);
                    IntDomainVar taskStart = solver.createBoundIntVar("T.S(" + n + ")" + n, 0, MAX_TIME);
                    IntDomainVar taskEnd = solver.createBoundIntVar("T.E(" + n + ")" + n, 0, MAX_TIME);
                    IntDomainVar isIdle = solver.createBooleanVar("isIdle");
                    postImplies(solver, solver.lt(idleNode.getStart(), idleNode.getEnd()), solver.eq(isIdle, 1));


                    IntDomainVar[] a = new IntDomainVar[]{solver.makeConstantIntVar(0), idleNode.getStart(), isIdle, taskStart};
                    IntDomainVar[] b = new IntDomainVar[]{solver.makeConstantIntVar(0), idleNode.getEnd(), isIdle, taskEnd};
                    solver.post(new ElementV(a, 0, solver.getEnvironment()));
                    solver.post(new ElementV(b, 0, solver.getEnvironment()));

                    idle_starts[i] = taskStart;
                    idle_ends[i] = taskEnd;

//                    Test if it works when it does not touch the variables of DSlices
//                        idle_starts[i] = solver.makeConstantIntVar(1);
//                        idle_ends[i] = solver.makeConstantIntVar(1);


                    durations[i] = rp.makeDuration("Dur(" + n + ")");
                    heights[i] = solver.makeConstantIntVar(1); // All tasks have to be scheduled
                    taskvars[i] = solver.createTaskVar("Task_" + n, idle_starts[i], idle_ends[i], durations[i]);
                    solver.post(solver.eq(idle_ends[i], solver.plus(idle_starts[i], durations[i])));
                }
                Cumulative cumulative = new Cumulative(solver, "Cumulative", taskvars, heights,
                        consumption, capacity, uppBound);
                solver.post(cumulative);
            }   // END OF CONTINUOUS
        }
        //TODO: Fix the problem with (too many nodes with too little VMs)
        // Get number of VMs hosted on each node
        IntDomainVar[] VMsOnAllNodes = rp.getNbRunningVMs();

        // Filter the involved nodes
        IntDomainVar[] vmsOnInvolvedNodes = new IntDomainVar[NUMBER_OF_TASK];
        int i = 0;
        for (UUID n : cstr.getInvolvedNodes()) {
            vmsOnInvolvedNodes[i++] = VMsOnAllNodes[rp.getNode(n)];
        }

        // idle is equals the number of vmsOnInvolvedNodes with value 0. idle should be less than Amount for MaxSN
        IntDomainVar idle = solver.createBoundIntVar("Nidles", 0, NUMBER_OF_TASK);
        solver.post(solver.occurence(vmsOnInvolvedNodes, idle, 0));
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
