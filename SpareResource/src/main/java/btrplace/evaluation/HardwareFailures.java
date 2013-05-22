package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/7/13
 * Time: 2:36 PM
 */
public class HardwareFailures {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private Model model;
    private Set<SatConstraint> dis_cstr;
    private Set<SatConstraint> cont_cstr;
    private static FileWriter inf;
    private static FileWriter fw_dp;
    private static FileWriter fw_cp;

    public HardwareFailures(Model m, Set<SatConstraint> d, Set<SatConstraint> c) {
        model = m;
        dis_cstr = d;
        cont_cstr = c;
        try {
            inf = new FileWriter("instance.json");
            fw_dp = new FileWriter("dplan.json");
            fw_cp = new FileWriter("cplan.json");
        } catch (IOException e) {
            log.error(e.toString());
        }

    }


    public void evaluate() {
        try {
            model = fixDiscreteModel();
            log.info("Successfully fix origin model");
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
                        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
                        fw_dp.write(rpc.toJSON(dis_plan).toJSONString());
                        fw_dp.close();

                        cont_cstr.addAll(offs);
                        inf.close();

                        ReconfigurationPlan cont_plan = cra.solve(clone, cont_cstr);
                        fw_cp.write(rpc.toJSON(cont_plan).toJSONString());
                        fw_cp.close();
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
                    break;
                }
            }
        } catch (SolverException e) {
            log.error(e.toString());
        } catch (JSONConverterException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
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
            log.error(e.toString());
        }
        return null;
    }

    private boolean satisfiedDiscrete(ReconfigurationPlan plan) {
        for (SatConstraint c : dis_cstr) {
            if (!c.isSatisfied(plan.getResult())) {
                return false;
            }
        }
        return true;
    }

    private boolean satisfiedContinuous(ReconfigurationPlan plan) {
        for (SatConstraint c : cont_cstr) {
            if (c.isContinuous()) {
                if (!c.isSatisfied(plan)) {
                    return false;
                }
            } else {
                if (!c.isSatisfied(plan.getResult())) {
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
