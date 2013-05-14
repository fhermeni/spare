package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Offline;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/7/13
 * Time: 2:36 PM
 */
public class Evaluation {
    private static final Logger log = LoggerFactory.getLogger(Evaluation.class.getPackage().getName());
    private static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private Model model;
    private Set<SatConstraint> dis_cstr;
    private Set<SatConstraint> cont_cstr;
    private ReconfigurationPlan dis_plan;
    private ReconfigurationPlan cont_plan;

    public Evaluation(Model m, Set<SatConstraint> d, Set<SatConstraint> c) {
        model = m;
        dis_cstr = d;
        cont_cstr = c;
    }


    public void evaluate() throws SolverException {
        log.info("Evaluate:");
        Model clone = model.clone();
        Random rand = new Random();
        int p = model.getMapping().getAllNodes().size() * 10 / 100;
        Set<Offline> offs = new HashSet<Offline>();
        for (int i = 1; i <= p; i++) {
            int randomId = rand.nextInt(100);
            log.info("Shutdown node: " + randomId);
            Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(new UUID(1, randomId))));
            offs.add(offline);
            dis_cstr.add(offline);
            dis_plan = cra.solve(model, dis_cstr);
            log.info(dis_plan.toString());
            if (!satisfied(dis_plan)) {
                cont_cstr.addAll(offs);
                cont_plan = cra.solve(clone, cont_cstr);
                analyze(dis_plan, cont_plan);
            }
        }
    }

    private boolean satisfied(ReconfigurationPlan plan) {
        for (SatConstraint c : cont_cstr) {
            if (c.isSatisfied(plan) != SatConstraint.Sat.SATISFIED) {
                return false;
            }
        }
        return true;
    }

    public void analyze(ReconfigurationPlan d, ReconfigurationPlan c) {
        log.info("Analyze:");
        log.info("{} {}", d.getDuration(), c.getDuration());
        log.info("{} {}", d.getSize(), c.getSize());
        log.info("{} {}", getNumberOfDelayedAction(d), getNumberOfDelayedAction(c));
    }

    public int getNumberOfDelayedAction(ReconfigurationPlan plan) {
        int i = 0;
        for (Action a : plan) {
            if (plan.getDirectDependencies(a).size() > 0) {
                i++;
            }
        }
        return i;
    }

}
