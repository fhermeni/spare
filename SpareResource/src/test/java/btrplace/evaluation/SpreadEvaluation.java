package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Spread;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
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
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TestGoogleTraceDataA.class.getPackage().getName());

    @Test
    public void spreadBasicEvaluation() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5, vm6).build();

        ShareableResource cpu = new ShareableResource("cpu", 1);
        cpu.set(n1, 4);
        cpu.set(n2, 4);
        cpu.set(n3, 2);
        cpu.set(n4, 4);
        cpu.set(vm1, 1);
        cpu.set(vm2, 2);
        cpu.set(vm3, 1);
        cpu.set(vm4, 1);
        cpu.set(vm5, 1);
        cpu.set(vm6, 1);

        ShareableResource mem = new ShareableResource("mem", 1);
        mem.set(n1, 4);
        mem.set(n2, 4);
        mem.set(n3, 4);
        mem.set(n4, 2);
        mem.set(vm1, 1);
        mem.set(vm2, 1);
        mem.set(vm3, 1);
        mem.set(vm4, 2);
        mem.set(vm5, 1);
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
        Overbook obc = new Overbook(map.getAllNodes(), "cpu", 1);
        Overbook obm = new Overbook(map.getAllNodes(), "mem", 1);
        ctrs.add(obc);
        ctrs.add(obm);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan discrete_plan = cra.solve(model, ctrs);
        if (discrete_plan == null) {
            log.info("Discrete plan is null");
        }
        if (satisfiedDiscrete(discrete_plan, ctrs)) {
            log.info("DPlan satisfies DRestriction");
        }
        if (satisfiedContinuous(discrete_plan, ctrsC)) {
            log.info("Dplan satisfies CRestriction");
            log.info("\n" + discrete_plan.toString());
        } else {
            ReconfigurationPlan cont_plan = cra.solve(model, ctrsC);
            if (satisfiedContinuous(cont_plan, ctrsC)) {
                log.info("Cplan satisfies CRestriction");
                log.info(cont_plan.toString());
            }
        }

    }

    private boolean satisfiedDiscrete(ReconfigurationPlan plan, Set<SatConstraint> dis_cstr) {
        for (SatConstraint c : dis_cstr) {
            if (c.isSatisfied(plan.getResult()) != SatConstraint.Sat.SATISFIED) {
                return false;
            }
        }
        return true;
    }

    private boolean satisfiedContinuous(ReconfigurationPlan plan, Set<SatConstraint> cont_cstr) {
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
        try {
            ev.evaluate();
        } catch (SolverException e) {
            log.error(e.toString());
        }
    }

}
