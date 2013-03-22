package btrplace.solver.choco.constraint;

import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.StrategyFactory;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.constraints.integer.channeling.BooleanChanneling;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;


public class CumulativeTest {
    static final int NUMBER_OF_TASK = 5;
    static final int CAPACITY = 3;
    static int[] durations = {3, 3, 1, 2, 0};
    static int HORIZON = 3;


    public static void main(String[] args) throws ContradictionException {

        TestCumulativeChocoSolver();
    }

    public static void TestCumulativeChocoSolver() throws ContradictionException {

        Solver solver = new CPSolver();
        String cstr_name = "cumulative";
        IntDomainVar capacity = solver.createIntegerConstant("capacity", CAPACITY);
        IntDomainVar consumption = solver.createBoundIntVar("consum", 1, 3);
        IntDomainVar uppBound = solver.createBoundIntVar("uppBound", HORIZON, HORIZON);
        IntDomainVar objective = solver.createBoundIntVar("obj", 0, NUMBER_OF_TASK);
        solver.setObjective(objective);

        IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_TASK];
        IntDomainVar[] starts = new IntDomainVar[NUMBER_OF_TASK];
        IntDomainVar[] ends = new IntDomainVar[NUMBER_OF_TASK];
        IntDomainVar[] Durations = new IntDomainVar[NUMBER_OF_TASK];
        IntDomainVar[] usages = new IntDomainVar[NUMBER_OF_TASK];
        TaskVar[] taskvars = new TaskVar[NUMBER_OF_TASK];


        for (int i = 0; i < NUMBER_OF_TASK; i++) {
            starts[i] = solver.createBoundIntVar("St(" + i + ")", 0, HORIZON);
            ends[i] = solver.createBoundIntVar("End(" + i + ")", 0, HORIZON);
            Durations[i] = solver.createBoundIntVar("Dur(" + i + ")", durations[i], durations[i]);
            heights[i] = solver.createBoundIntVar("Hgt(" + i + ")", 0, 1);
            usages[i] = solver.createBooleanVar("U" + i);
            taskvars[i] = solver.createTaskVar("Task_" + i, starts[i], ends[i], Durations[i]);
            solver.post(solver.eq(ends[i], solver.plus(starts[i], Durations[i])));
            solver.post(new BooleanChanneling(usages[i], heights[i], 1));
        }
        // maximize number of nodes online
        solver.post(solver.eq(solver.sum(usages), objective));

        // node id=2 turn off, on at the beginning time
        starts[2].setVal(0);


        Cumulative cumulative = new Cumulative(solver, cstr_name, taskvars, heights, consumption, capacity,
                uppBound);

        solver.post(cumulative);

        StrategyFactory.setNoStopAtFirstSolution(solver);
        StrategyFactory.setDoOptimize(solver, true); //maximize
        solver.clearGoals();
        solver.addGoal(BranchingFactory.lexicographic(solver, usages, new MaxVal()));

        solver.addGoal(BranchingFactory.minDomMinVal(solver, starts));
        solver.generateSearchStrategy();
        solver.launch();

        ChocoLogging.setVerbosity(Verbosity.SEARCH);
        solver.solve();
        if (solver.isFeasible()) {
            System.out.println(solver.pretty());
        }

    }
}
