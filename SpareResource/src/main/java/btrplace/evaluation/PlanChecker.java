package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/23/13
 * Time: 12:38 PM
 */
public class PlanChecker {

    public static void main(String[] args) {

        ReconfigurationPlanConverter planConverter = new ReconfigurationPlanConverter();
        SatConstraintsConverter satConstraintsConverter = new SatConstraintsConverter();
        try {
            ReconfigurationPlan plan = planConverter.fromJSON(new File(args[0]));
            List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(args[1]));

            SatConstraint disatisfied = check(plan, new HashSet<SatConstraint>(satConstraint));
            if (disatisfied == null) {
                System.out.println(String.format("The plan:\n%s\n satisfies all the constraints:", plan));
                for (SatConstraint s : satConstraint) {
                    System.out.println(s);
                }
            } else {
                System.out.println(String.format("The plan:\n%s\nNOT satisfy the constraint:\n%s", plan, disatisfied));
            }


        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        }
    }


    static public SatConstraint check(ReconfigurationPlan plan, Set<SatConstraint> co) {
        for (SatConstraint c : co) {
            if (c.isContinuous()) {
                if (!c.isSatisfied(plan)) {
                    System.out.println("Doesn't satisfy " + c);
                    return c;
                }
            } else {
                if (!c.isSatisfied(plan.getResult())) {
                    System.out.println("Doesn't satisfy " + c);
                    return c;
                }
            }
        }
        return null;
    }
}
