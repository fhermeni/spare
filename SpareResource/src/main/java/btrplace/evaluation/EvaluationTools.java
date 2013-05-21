package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;

import java.util.Collection;
import java.util.Set;

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

    static public int getNumberOfDelayedAction(ReconfigurationPlan plan) {
        int i = 0;
        for (Action a : plan) {
            if (plan.getDirectDependencies(a).size() > 0) {
                i++;
            }
        }
        return i;
    }

    static public String analyze(ReconfigurationPlan d, ReconfigurationPlan c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze:\n");
        sb.append("Duration: {} {}\n", d.getDuration(), c.getDuration());
        sb.append("N. Action: {} {}\n", d.getSize(), c.getSize());
        sb.append("N. delay Acts: {} {}\n", EvaluationTools.getNumberOfDelayedAction(d),
                EvaluationTools.getNumberOfDelayedAction(c));
        return sb.toString();
    }

    static public void solve(ChocoReconfigurationAlgorithm cra, Model model, Set<SatConstraint> constraints) {
        try {
            ReconfigurationPlan p = cra.solve(model, constraints);
            if (p != null) {
                System.out.println("--- Solving using repair : " + cra.repair());
                System.out.println(cra.getSolvingStatistics());
            }
        } catch (SolverException e) {
            System.err.println("--- Solving using repair : " + cra.repair() + "; Error: " + e.getMessage());
        }
    }
}
