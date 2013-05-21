package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.model.*;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Preserve;
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
 * Created with IntelliJ IDEA.
 * User: Tu Huynh Dang
 * Date: 5/22/13
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class IncreasingLoad {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private Model model;
    private Set<SatConstraint> dis_cstr;
    private Set<SatConstraint> cont_cstr;
    private static FileWriter inf;
    private static FileWriter fw_dp;
    private static FileWriter fw_cp;

    public IncreasingLoad(Model m, Set<SatConstraint> d, Set<SatConstraint> c) {
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
        log.info("Evaluate:");
        model = fixDiscreteModel();
        log.info("Successfully fix origin model");
        log.info(model.toString());
        Model clone = model.clone();

        dis_cstr.addAll(preserveConstraints(model));
        cont_cstr.addAll(preserveConstraints(clone));
        EvaluationTools.solve(cra, model, dis_cstr);
        EvaluationTools.solve(cra, model, cont_cstr);


    }

    private Model fixDiscreteModel() {
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
    }


    private Set<SatConstraint> preserveConstraints(Model model) {
        Set<SatConstraint> constraints = new HashSet<SatConstraint>();
        Set<UUID> vms = model.getMapping().getRunningVMs();
        Iterator<UUID> iter = vms.iterator();
        for (int i = 0; iter.hasNext() && i < vms.size()/2; i++) {
            UUID vm = iter.next();
            constraints.add(new Preserve(Collections.singleton(vm), "cpu", 4));
        }
        return constraints;
    }


}
