package btrplace.solver.choco.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.BootableNodeModel;
import btrplace.solver.choco.actionModel.NodeActionModel;
import btrplace.solver.choco.actionModel.ShutdownableNodeModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CMaxOnlines implements ChocoSatConstraint {

    private final MaxOnline constraint;

    /**
     * Make a new constraint
     *
     * @param maxOn is maxOnlines constraint to rely on
     */
    public CMaxOnlines(MaxOnline maxOn) {
        super();
        constraint = maxOn;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model model) {
        return model.getMapping().getRunningVMs(constraint.getInvolvedNodes());
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        CPSolver solver = rp.getSolver();
        if (constraint.isContinuous()) {
            // The constraint must be already satisfied
/*            if (!constraint.isSatisfied(rp.getSourceModel())) {
                rp.getLogger().error("The constraint '{}' must be already " +
                        "satisfied to provide a continuous restriction", constraint);
                return false;
            } else {*/
            Mapping map = rp.getSourceModel().getMapping();
            final int NUMBER_OF_TASK = constraint.getInvolvedNodes().size();
            int i = 0;
            int[] nodeIdx = new int[NUMBER_OF_TASK];
            for (Node n : constraint.getInvolvedNodes()) {
                nodeIdx[i++] = rp.getNode(n);
            }
            IntDomainVar capacity = solver.createIntegerConstant("capacity", constraint.getAmount());
            IntDomainVar consumption = solver.createBoundIntVar("consum", 0, constraint.getAmount());//minimum consumption
            // of the resource
            IntDomainVar uppBound = rp.getEnd();                    // All tasks must be scheduled before this time
            IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_TASK];   // The state of the node
            IntDomainVar[] starts = new IntDomainVar[NUMBER_OF_TASK];
            IntDomainVar[] ends = new IntDomainVar[NUMBER_OF_TASK];
            IntDomainVar[] Durations = new IntDomainVar[NUMBER_OF_TASK];  // Online duration
            TaskVar[] task_vars = new TaskVar[NUMBER_OF_TASK];  // Online duration is modeled as a task

            for (int idx = 0; idx < nodeIdx.length; idx++) {
                Node n = rp.getNode(nodeIdx[idx]);
                NodeActionModel nodeAction = rp.getNodeAction(n);


                if (map.getOfflineNodes().contains(n)) {             // Nodes can be turned on
                    BootableNodeModel b = (BootableNodeModel) nodeAction;
                    starts[idx] = b.getPoweringStart();
                    ends[idx] = b.getPoweringEnd();
                } else {                                             // Nodes can be turned off
                    ShutdownableNodeModel sd = (ShutdownableNodeModel) nodeAction;
                    starts[idx] = sd.getPoweringStart();
                    ends[idx] = sd.getPoweringEnd();
                }

                Durations[idx] = rp.makeUnboundedDuration("Dur(" + n + ")");
                solver.post(solver.leq(Durations[idx], rp.getEnd()));
                heights[idx] = solver.makeConstantIntVar(1);         // All tasks have to be scheduled
                task_vars[idx] = solver.createTaskVar("Task_" + n, starts[idx], ends[idx], Durations[idx]);
                solver.post(solver.eq(ends[idx], solver.plus(starts[idx], Durations[idx])));
            }
            Cumulative cumulative = new Cumulative(solver, "Cumulative", task_vars,
                    heights, consumption, capacity, uppBound);
            solver.post(cumulative);
        }
//        }
        // Constraint for discrete model
        List<IntDomainVar> nodes_state = new ArrayList<IntDomainVar>(constraint.getInvolvedNodes().size());

        for (Node ni : constraint.getInvolvedNodes()) {
            nodes_state.add(rp.getNodeAction(ni).getState());
        }

        IntExp sum_of_states = CPSolver.sum(nodes_state.toArray(new IntDomainVar[nodes_state.size()]));
        solver.post(solver.leq(sum_of_states, constraint.getAmount()));

        return true;
    }

    /**
     * The builder associated to this constraint
     *
     * @author Tu Huynh Dang
     */
    public static class Builder implements ChocoSatConstraintBuilder {

        @Override
        public ChocoSatConstraint build(SatConstraint cstr) {
            return new CMaxOnlines((MaxOnline) cstr);
        }

        @Override
        public Class<? extends SatConstraint> getKey() {
            return MaxOnline.class;
        }

    }

}
