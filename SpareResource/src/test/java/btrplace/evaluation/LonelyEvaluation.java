package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Lonely;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 11:29 AM
 */
public class LonelyEvaluation {

    @Test(timeOut = 10000)
    public void test1() throws SolverException {
        ModelGenerator gen = new ModelGenerator();
        Model model = gen.generateModel(20, 30);

        Lonely constraint = new Lonely(gen.getRandomVMs(4));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, Collections.<SatConstraint>singleton(constraint));
        Model result = plan.getResult();
        constraint.setContinuous(true);
        IncreasingLoad incLoad = new IncreasingLoad(result, constraint);
        incLoad.run();
    }
}
