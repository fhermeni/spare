package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.io.IOException;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/23/13
 * Time: 9:59 AM
 */
public class SolveDriver {

    public static void main(String[] args) throws IOException, JSONConverterException {
        Solve s = new Solve();
        s.readArguments(args);
        Model model = s.getModel();
        Set<SatConstraint> constraints = s.getConstraints();

        IncreasingLoad incLoad = new IncreasingLoad(model, constraints);
        ReconfigurationPlan plan = incLoad.run();
        System.out.println(plan);
        s.recordPlan(plan);
    }
}
