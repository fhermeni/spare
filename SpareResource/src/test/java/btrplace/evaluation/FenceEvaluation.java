package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Fence;
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
 * Time: 11:20 AM
 */
public class FenceEvaluation {
    @Test(timeOut = 10000)
    public void test1() throws SolverException {
        ModelGenerator gen = new ModelGenerator();
        Model model = gen.generateModel(9, 30);

        Fence constraint = new Fence(gen.getRandomVMs(5), gen.getRandomNodes(5));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, Collections.<SatConstraint>singleton(constraint));
        Model result = plan.getResult();
        constraint.setContinuous(true);
        IncreasingLoad incLoad = new IncreasingLoad(result, constraint);
        incLoad.run();
    }
}
