package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.CumulatedResourceCapacity;
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
 * Time: 11:32 AM
 */
public class CRCEvaluation {
    @Test
    public void test1() throws SolverException {
        ModelGenerator gen = new ModelGenerator();
        Model model = gen.generateModel(20, 30);
        CumulatedResourceCapacity capacity = new CumulatedResourceCapacity(gen.getRandomNodes(5), "cpu", 10);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, Collections.<SatConstraint>singleton(capacity));
        Model result = plan.getResult();
        capacity.setContinuous(true);
        IncreasingLoad incLoad = new IncreasingLoad(result, capacity);
        incLoad.run();
    }
}
