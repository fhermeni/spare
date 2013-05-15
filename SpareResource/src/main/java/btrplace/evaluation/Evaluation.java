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

    public Evaluation(Model m, Set<SatConstraint> d, Set<SatConstraint> c) {
        model = m;
        dis_cstr = d;
        cont_cstr = c;
    }


    public void evaluate() throws SolverException {
        log.info("Evaluate:");
        model = fixDiscreteModel();
        log.info("Successfully fix origin model");
        log.info(model.toString());
        Model clone = model.clone();

        Random rand = new Random();
        int node_size = model.getMapping().getAllNodes().size();
        Set<Offline> offs = new HashSet<Offline>();
        Set<Integer> offIds = new HashSet<Integer>(node_size);
        int randomId;
        for (int i = 0; i < node_size; i++) {

            do {
                randomId = rand.nextInt(node_size);
            }
            while (offIds.contains(randomId));
            offIds.add(randomId);

            log.info("Event: Shutdown node: " + randomId);

            UUID n = new UUID(1, randomId);
            Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(n)));
            offs.add(offline);

            dis_cstr.add(offline);

            ReconfigurationPlan dis_plan = cra.solve(model, dis_cstr);
            if (dis_plan != null) {
                if (!satisfiedContinuous(dis_plan)) {
                    cont_cstr.addAll(offs);
                    ReconfigurationPlan cont_plan = cra.solve(clone, cont_cstr);
                    if (cont_plan != null) {
                        log.info("Found continuous plan");
                        analyze(dis_plan, cont_plan);
                    } else log.info("Not found continuous plan");
                    break;
                } else {
                    log.info("Discrete plan satisfies Continuous");
                }
            } else {
                log.info("Not found discrete plan");
            }
        }
    }

    private Model fixDiscreteModel() {
        try {
            ReconfigurationPlan p = cra.solve(model, dis_cstr);
            if (satisfiedDiscrete(p)) {
                return p.getResult();
            } else
                throw new SolverException(model, "Cannot find the reconfiguration plan");
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private boolean satisfiedDiscrete(ReconfigurationPlan plan) {
        for (SatConstraint c : dis_cstr) {
            if (c.isSatisfied(plan.getResult()) != SatConstraint.Sat.SATISFIED) {
                return false;
            }
        }
        return true;
    }

    private boolean satisfiedContinuous(ReconfigurationPlan plan) {
        for (SatConstraint c : cont_cstr) {
            if (c.isContinuous()) {
                if (c.isSatisfied(plan) != SatConstraint.Sat.SATISFIED) {
                    return false;
                }
            } else {
                if (c.isSatisfied(plan.getResult()) != SatConstraint.Sat.SATISFIED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void analyze(ReconfigurationPlan d, ReconfigurationPlan c) {
        log.info("Analyze:");
        log.info("Duration: {} {}", d.getDuration(), c.getDuration());
        log.info("N. Action: {} {}", d.getSize(), c.getSize());
        log.info("N. delay Acts: {} {}", getNumberOfDelayedAction(d), getNumberOfDelayedAction(c));
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
