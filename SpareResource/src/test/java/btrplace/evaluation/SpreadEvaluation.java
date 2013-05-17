package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/15/13
 * Time: 2:41 PM
 */
public class SpreadEvaluation implements PremadeElements {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void spreadBasicEvaluation() {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5, vm6).build();

        ShareableResource cpu = new ShareableResource("cpu", 1);
        cpu.set(n1, 4);
        cpu.set(n2, 4);
        cpu.set(n3, 2);
        cpu.set(n4, 4);
        cpu.set(vm2, 2);

        ShareableResource mem = new ShareableResource("mem", 1);
        mem.set(n1, 4);
        mem.set(n2, 4);
        mem.set(n3, 4);
        mem.set(n4, 2);
        mem.set(vm4, 2);
        mem.set(vm6, 2);

        Model model = new DefaultModel(map);
        model.attach(cpu);
        model.attach(mem);
        log.info(model.toString());


        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        Set<SatConstraint> ctrsC = new HashSet<SatConstraint>();
        Set<UUID> apache = new HashSet<UUID>(Arrays.asList(vm1, vm3, vm5));
        Set<UUID> tomcat = new HashSet<UUID>(Arrays.asList(vm2, vm4, vm6));

        ctrs.add(new Spread(apache, false));
        ctrs.add(new Spread(tomcat, false));

        ctrsC.add(new Spread(apache));
        ctrsC.add(new Spread(tomcat));

        Offline off = new Offline(new HashSet<UUID>() {{
            add(n2);
        }});
        ctrs.add(off);
        ctrsC.add(off);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        try {
            ReconfigurationPlan dp = cra.solve(model, ctrs);
            if (!satisfy(dp, ctrsC)) {
                ReconfigurationPlan cp = cra.solve(model, ctrsC);
                if (!satisfy(cp, ctrsC)) {
                    log.info("Not found continuous plan");
                } else log.info(cp.toString());
            }
        } catch (SolverException e) {
            e.printStackTrace();
        }
    }

    private boolean satisfy(ReconfigurationPlan dp, Set<SatConstraint> constraints) {

        for (SatConstraint sc : constraints) {
            if (sc.isSatisfied(dp)) {
                log.info("Satisfy: " + sc);
            } else {
                log.info("Not Satisfy: " + sc);
                return false;
            }
        }
        return true;
    }

    @Test
    public void spreadTest1() {
        TestModelGenerator tm = new TestModelGenerator(10, 40);
        Model m = tm.generateModel();
        Set<UUID> apache = tm.getRandomVMs(6);
        Set<UUID> tomcat = tm.getRandomVMs(3);
        Set<UUID> mysql = tm.getRandomVMs(2);

        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        Set<SatConstraint> ctrsC = new HashSet<SatConstraint>();
        ctrs.add(new Spread(apache, false));
        ctrs.add(new Spread(tomcat, false));
        ctrs.add(new Spread(mysql, false));
        ctrsC.add(new Spread(apache));
        ctrsC.add(new Spread(tomcat));
        ctrsC.add(new Spread(mysql));
        log.info(m.toString());
        log.info(ctrs.toString());
        Evaluation ev = new Evaluation(m, ctrs, ctrsC);
        ev.evaluate();
    }

    @Test
    public void examSpreadConstraint() {
        TestModelGenerator tm = new TestModelGenerator(2, 2);
        Model m = tm.generateModel();
        Set<UUID> mysql = tm.getRandomVMs(2);

        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        Spread sp1 = new Spread(mysql, false);
        ctrs.add(sp1);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        try {
            ReconfigurationPlan plan = cra.solve(m, ctrs);
            if (sp1.isSatisfied(plan)) {
                log.info(m.toString());
                log.info(sp1.toString());
                log.info(plan.toString());
            }
        } catch (SolverException e) {
            log.error(e.toString());

        }
    }
}
