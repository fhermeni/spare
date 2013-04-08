package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MinSpareNode;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
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

public class CMinSpareNode implements ChocoSatConstraint {

    private final MinSpareNode cstr;
    private static final int MAX_TIME = 120;

    /**
     * Make a new constraint.
     *
     * @param msn is minSpareNode constraint to rely on
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
        CPSolver solver = rp.getSolver();
        int NUMBER_OF_NODE = cstr.getInvolvedNodes().size();
        if (cstr.isContinuous()) {
            // The constraint must be already satisfied
            if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
                rp.getLogger()
                        .error("The constraint '{}' must be already satisfied to provide a continuous restriction",
                                cstr);
                return false;
            } else {
                // The constraint must be already satisfied
                if (!cstr.isSatisfied(rp.getSourceModel()).equals(SatConstraint.Sat.SATISFIED)) {
                    rp.getLogger()
                            .error("The constraint '{}' must be already satisfied to provide a continuous restriction",
                                    cstr);
                    return false;
                } else {            // Start of Continuous Model
                    IntDomainVar capacity = solver.createIntegerConstant("capacity", cstr.getAmount());
                    //minimum consumption of the resource
                    IntDomainVar consumption = solver.createBoundIntVar("consum", cstr.getAmount(), NUMBER_OF_NODE);
                    IntDomainVar uppBound = rp.getEnd(); // All tasks must be scheduled before this time
                    IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_NODE]; // The state of the node
                    IntDomainVar[] durations = new IntDomainVar[NUMBER_OF_NODE]; // Online duration
                    TaskVar[] taskvars = new TaskVar[NUMBER_OF_NODE]; // Online duration is modeled as a task

                    // The start moment of node being idle
                    IntDomainVar[] idle_starts = new IntDomainVar[NUMBER_OF_NODE];

                    // The end moment of node being idle
                    IntDomainVar[] idle_ends = new IntDomainVar[NUMBER_OF_NODE];

                    for (UUID n : rp.getSourceModel().getMapping().getAllNodes()/*cstr.getInvolvedNodes()*/) {
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
                }
            }
        }


        // Extract all the state of the involved nodes (all nodes in this case)
        IntDomainVar[] states = new IntDomainVar[NUMBER_OF_NODE];
        int j = 0;
        for (UUID n : cstr.getInvolvedNodes()) {
            states[j++] = rp.getNodeAction(n).getState();
        }
        IntDomainVar[] VMsOnAllNodes = rp.getNbRunningVMs();
        // Each element is the number of VMs on each node
        IntDomainVar[] vmsOnInvolvedNodes = new IntDomainVar[NUMBER_OF_NODE];
        IntDomainVar[] idles = new IntDomainVar[NUMBER_OF_NODE];
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (UUID n : cstr.getInvolvedNodes()) {
            vmsOnInvolvedNodes[i] = solver.createBoundIntVar("nVMs" + n, 0, maxVMs);
            IntDomainVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is -1, otherwise, it equals the number of VMs on that node
            IntDomainVar[] c = new IntDomainVar[]{solver.makeConstantIntVar(1), VMsOnAllNodes[rp.getNode(n)],
                    state, vmsOnInvolvedNodes[i]};
            solver.post(new ElementV(c, 0, solver.getEnvironment()));
            // IF number of VMs on a node is 0 -> Idle
            idles[i] = solver.createBooleanVar("idle" + n);
            postIfOnlyIf(solver, idles[i], solver.eq(vmsOnInvolvedNodes[i], 0));
            i++;
        }
        IntExp Sidle = solver.sum(idles);
        solver.post(solver.geq(Sidle, cstr.getAmount()));
        /*IntDomainVar[] hostVM = rp.getNbRunningVMs();
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
        solver.post(solver.geq(Sidle, cstr.getAmount()));*/

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
