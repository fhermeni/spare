package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/23/13
 * Time: 9:59 AM
 */
public class SolveDriver {

    public static void main(String[] args) {
        Solve s = new Solve();
        s.readArguments(args);
        Model model = s.getModel();
        Set<SatConstraint> constraints = s.getConstraints();
        Model fixed_model = EvaluationTools.prepareModel(model, constraints);
        IncreasingLoad incLoad = new IncreasingLoad(fixed_model, constraints);
        ReconfigurationPlan plan = incLoad.run();
        System.out.println(plan);
        s.recordPlan(plan);
    }
}
