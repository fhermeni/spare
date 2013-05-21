package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/17/13
 * Time: 11:02 AM
 */
public class AmongEvaluation extends Datacenter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test        // Gist: https://gist.github.com/tudang/5598942
    public void amongTest1() {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4).build();

        Model model = new DefaultModel(map);
        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm4));
        Set<UUID> vms2 = new HashSet<UUID>(Arrays.asList(vm3, vm4));
        Set<UUID> ns1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> ns2 = new HashSet<UUID>(Arrays.asList(n3));
        Set<Set<UUID>> nodeSets = new HashSet<Set<UUID>>();
        nodeSets.add(ns1);
        nodeSets.add(ns2);
        log.info(model.toString());
        Among among = new Among(vms, nodeSets);
        Collection<SatConstraint> ctrs = new HashSet<SatConstraint>();
        ctrs.add(among);
        ctrs.add(new Spread(vms2));
        ctrs.add(new SingleRunningCapacity(map.getAllNodes(), 2));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        try {
            ReconfigurationPlan plan = cra.solve(model, ctrs);
            Assert.assertNotNull(plan);
            if (among.isSatisfied(plan)) {
                log.info(model.getMapping().toString());
                log.info(among.toString());
                log.info(plan.toString());
            }

        } catch (SolverException e) {
            log.error(e.toString());
        }
    }

    @Test
    public void TestWebAppAmong() {
        Datacenter center = new Datacenter();
        WebApplication wapp = new WebApplication();
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5, n6, n8, n9, n11, n12).off(n2, n7, n10)
                .ready(vm1, vm2, vm3, vm4, vm5, vm6, vm7, vm8, vm9, vm10).build();
        Model model = new DefaultModel(map);

        Set<Set<UUID>> nodeSets = new HashSet<Set<UUID>>();
        nodeSets.add(center.getRack2());
        nodeSets.add(center.getRack3());

        Collection<SatConstraint> constr = new HashSet<SatConstraint>();
        Running run = new Running(wapp.getAllReplicas());
        constr.add(run);
        constr.add(new Among(wapp.getMysql(), nodeSets));
        for (Set<UUID> set : wapp.getAllTiers()) {
            constr.add(new Spread(set));
        }

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        try {
            ReconfigurationPlan plan = cra.solve(model, constr);
            Assert.assertNotNull(plan);
            Assert.assertTrue(EvaluationTools.satisfy(plan, constr));

            log.info(plan.toString());
            log.info(plan.getResult().toString());
        } catch (SolverException e) {
            log.error(e.toString());
        }
    }
}
