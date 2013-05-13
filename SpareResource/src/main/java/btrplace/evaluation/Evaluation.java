package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Offline;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/7/13
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Evaluation {
    private static final Logger log = LoggerFactory.getLogger(Evaluation.class.getPackage().getName());
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
        Model clone = model.clone();
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        for (int i = 0; i < 100; i++) {
            log.info("Number of node: " + (i + 1));
            Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(new UUID(1, i))));
            dis_cstr.add(offline);
            cont_cstr.add(offline);
            dis_plan = cra.solve(model, dis_cstr);
            if (!isSatisfiedContinuousRestriction(dis_plan)) {
                log.warn("The reconfiguration plan for discrete restriction doesn't satisfy the continuous restriction");
                log.info(dis_cstr.toString());
                cont_plan = cra.solve(clone, cont_cstr);
                if (isSatisfiedContinuousRestriction(cont_plan)) {
                    log.info("The continuous plan satisfies continuous restriction");
                    log.info("Discrete " + dis_plan.toString());
                    log.info("Continuous " + cont_plan.toString());
                }
                break;
            }
        }



/*
        if (isSatisfiedDiscreteRestriction(dis_plan)) {
            log.info("Number of actions: " + dis_plan.getSize());
            log.info(dis_plan.toString());
        }

        if (isSatisfiedContinuousRestriction(dis_plan)) {
            log.info("Discrete plan also satisfies continuous restriction");
        }



        if (isSatisfiedContinuousRestriction(cont_plan)) {
            log.info("Continuous plan satisfies continuous restriction");
            log.info("Number of actions: " + cont_plan.getSize());
            log.info(cont_plan.toString());
        }*/


    }

    public boolean isSatisfiedDiscreteRestriction(ReconfigurationPlan plan) {
        for (SatConstraint sa : dis_cstr) {
            if (sa.isSatisfied(plan.getResult()) != SatConstraint.Sat.SATISFIED) {
                return false;
            }
        }
        return true;
    }

    public boolean isSatisfiedContinuousRestriction(ReconfigurationPlan plan) {
        for (SatConstraint sa : cont_cstr) {
            if (sa.isSatisfied(plan) != SatConstraint.Sat.SATISFIED) {
                return false;
            }
        }
        return true;
    }

}
