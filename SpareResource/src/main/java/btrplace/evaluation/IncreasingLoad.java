package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 5/22/13
 * Time: 12:01 AM
 */
public class IncreasingLoad {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private Model model;
    private Set<SatConstraint> cont_cstr;


    public IncreasingLoad(Model m, Set<SatConstraint> c) {
        model = m;
        cont_cstr = c;
        log.info(c.toString());
    }

    public IncreasingLoad(Model m, SatConstraint c) {
        this(m, new HashSet<SatConstraint>(Arrays.asList(c)));
    }


    public void run() {
        /*if (EvaluationTools.satisfy(model, cont_cstr)) {
            model = fixDiscreteModel();
        }*/
        Model clone = model.clone();

        cont_cstr.addAll(preserveConstraints(model));

        ReconfigurationPlan planC = EvaluationTools.solve(cra, model, cont_cstr);
        ReconfigurationPlan planD = EvaluationTools.solve(cra, clone, EvaluationTools.toDiscrete(cont_cstr));
        String analyze = EvaluationTools.analyze(planD, planC);
        log.info(analyze);
    }

    /*private Model fixDiscreteModel() {
        try {
            ReconfigurationPlan p = cra.solve(model, dis_cstr);
            if (EvaluationTools.satisfy(p, dis_cstr)) {
                return p.getResult();
            } else
                throw new SolverException(model, "Cannot find the reconfiguration plan");
        } catch (SolverException e) {
            log.error(e.toString());
        }
        return null;
    }*/


    private Set<SatConstraint> preserveConstraints(Model model) {
        Set<SatConstraint> constraints = new HashSet<SatConstraint>();
        Set<UUID> vms = model.getMapping().getRunningVMs();
        Iterator<UUID> iter = vms.iterator();
        for (int i = 0; iter.hasNext() && i < vms.size() / 2; i++) {
            UUID vm = iter.next();
            constraints.add(new Preserve(Collections.singleton(vm), "cpu", 4));
        }
        return constraints;
    }

    public static void main(String[] args) {
        for (String env : args) {
            String value = System.getenv(env);
            if (value != null) {
                System.out.format("%s=%s%n",
                        env, value);
            } else {
                System.out.format("%s is"
                        + " not assigned.%n", env);
            }
        }
    }
}
