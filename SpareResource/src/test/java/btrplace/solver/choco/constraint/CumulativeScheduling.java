package btrplace.solver.choco.constraint;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.StrategyFactory;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;

import static choco.Choco.*;
import static choco.visu.components.chart.ChocoChartFactory.createAndShowGUI;
import static choco.visu.components.chart.ChocoChartFactory.createCumulativeChart;

/**
 * Created with IntelliJ IDEA.
 * User: hdang
 * Date: 3/20/13
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class CumulativeScheduling {
    public static CPModel model = new CPModel();
    protected final static int N = 4;

    private final static int[] DURATIONS = new int[]{3, 3, 1, 2};
    private final static int HORIZON = 7;

    private final static int[] HEIGHTS = new int[]{1, 1, 1, 1};
    private final static int CAPACITY = 3;

    public static void main(String[] args) {

        TaskVariable[] tasks = makeTaskVarArray("T", 0, HORIZON, DURATIONS, Options.V_BOUND);
        IntegerVariable[] heights;
        IntegerVariable[] usages = makeBooleanVarArray("U", N) ;
        IntegerVariable objective = makeIntVar("obj", 0, N, Options.V_BOUND, Options.V_OBJECTIVE);

        model.addConstraint(eq(sum(usages), objective));

        Constraint cumulative;
        boolean useAlternativeResource = false;
        if(useAlternativeResource) {
            heights = constantArray(HEIGHTS);
        cumulative = cumulativeMax("alt-cumulative", tasks, heights, usages, constant(CAPACITY),
                Options.NO_OPTION);
    }else {
        heights = new IntegerVariable[N];
//post the channeling to know if the task uses the resource or not.

        for (int i = 0; i < N; i++) {
            heights[i] = makeIntVar("H_" + i, new int[]{0, HEIGHTS[i]});
            model.addConstraint(boolChanneling(usages[i], heights[i], HEIGHTS[i]));
        }
        cumulative =cumulativeMax("cumulative", tasks, heights, constant(CAPACITY), Options.NO_OPTION);
    }
    model.addConstraint(cumulative);

        CPSolver solver = new CPSolver();
        solver.read(model);
        StrategyFactory.setNoStopAtFirstSolution(solver);
        StrategyFactory.setDoOptimize(solver, true); //maximize
        solver.clearGoals();
        solver.addGoal(BranchingFactory.lexicographic(solver, solver.getVar(usages), new MaxVal()));
        IntDomainVar[] starts = VariableUtils.getStartVars(solver.getVar(tasks));
        solver.addGoal(BranchingFactory.minDomMinVal(solver, starts));
        solver.generateSearchStrategy();
        solver.launch();

        System.out.println(solver.pretty());


        String title = "MaxOnlines constraint";
        createAndShowGUI(title, createCumulativeChart(title, (CPSolver) solver, cumulative, true));


    }

}
