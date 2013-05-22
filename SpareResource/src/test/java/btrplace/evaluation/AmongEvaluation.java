package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.SingleRunningCapacity;
import btrplace.model.constraint.Spread;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
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
public class AmongEvaluation implements PremadeElements {

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
}
