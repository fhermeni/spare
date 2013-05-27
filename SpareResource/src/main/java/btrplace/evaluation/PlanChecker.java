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

        boolean vflag = false;
        boolean cont = false;
        String plan_file = "";
        String constraints = "";

        int i = 0, j;
        String arg;
        char flag;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];

            // use this type of check for "wordy" arguments
            if (arg.equals("-verbose") || arg.equals("-v")) {
                System.out.println("verbose mode on");
                vflag = true;
            }

            // use this type of check for a series of flag arguments
            else {
                for (j = 1; j < arg.length(); j++) {
                    flag = arg.charAt(j);
                    switch (flag) {
                        case 'd':
                            cont = false;
                            if (vflag) System.out.println("Continuous:" + cont);
                            break;
                        case 'c':
                            cont = true;
                            if (vflag) System.out.println("Continuous:" + cont);
                            break;
                        default:
                            System.err.println("Solve: illegal option " + flag);
                            break;
                    }
                }
            }
        }

        if (i == args.length) {
            System.err.println("Usage: Solve [-cd] constraints plan");
        } else {
            plan_file = args[args.length - 1];
            constraints = args[args.length - 2];
        }


        ReconfigurationPlanConverter planConverter = new ReconfigurationPlanConverter();
        SatConstraintsConverter satConstraintsConverter = new SatConstraintsConverter();
        try {
            ReconfigurationPlan plan = planConverter.fromJSON(new File(plan_file));
            List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(constraints));
            Set<SatConstraint> dConstr = new HashSet<SatConstraint>(satConstraint);
            if (!cont) {
                dConstr = EvaluationTools.toDiscrete(new HashSet<SatConstraint>(satConstraint));
            }
            SatConstraint disSatisfied = check(plan, dConstr);
            if (disSatisfied == null) {
                System.out.println(String.format("The plan:\n%s\n satisfies all the constraints:", plan));
                for (SatConstraint s : satConstraint) {
                    System.out.println(s);
                }
            } else {
                System.out.println(String.format("The plan:\n%s\nNOT satisfy the constraint:\n%s", plan, disSatisfied));
            }


        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        }
    }


    static public SatConstraint check(ReconfigurationPlan plan, Set<SatConstraint> co) {
        if (plan == null) {
            System.err.println("No plan");
            System.exit(-1);
        }

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
