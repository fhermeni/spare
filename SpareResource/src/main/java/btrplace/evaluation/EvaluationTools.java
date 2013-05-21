package btrplace.evaluation;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationTools {


    static public boolean satisfy(ReconfigurationPlan plan, Collection<SatConstraint> constr) {
        for (SatConstraint c : constr) {
            if (c.isContinuous()) {
                if (!c.isSatisfied(plan)) return false;
            } else if (!c.isSatisfied(plan.getResult())) return false;
        }
        return true;
    }
}
