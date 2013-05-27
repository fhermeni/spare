package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
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
import btrplace.solver.choco.constraint.CMinSpareResources;
import btrplace.test.PremadeElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/15/13
 * Time: 2:41 PM
 */
public class SpreadEvaluation implements PremadeElements {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test(timeOut = 10000)  // Gist: https://gist.github.com/tudang/5599099
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
        Model clone = model.clone();

        Assert.assertEquals(model, clone);
        Set<SatConstraint> cList = new HashSet<SatConstraint>();
        Set<SatConstraint> dList = new HashSet<SatConstraint>();
        Set<UUID> vms1 = new HashSet<UUID>(Arrays.asList(vm1, vm3, vm5));
        Set<UUID> vms2 = new HashSet<UUID>(Arrays.asList(vm2, vm4, vm6));

        cList.add(new Spread(vms1));
        dList.add(new Spread(vms1, false));
        cList.add(new Spread(vms2));
        dList.add(new Spread(vms2, false));
        cList.add(new Offline(Collections.singleton(n2)));
        dList.add(new Offline(Collections.singleton(n2)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new CMinSpareResources.Builder());

        ReconfigurationPlan planD = EvaluationTools.solve(cra, clone, dList);
        ReconfigurationPlan planC = EvaluationTools.solve(cra, model, cList);
        String analyze = EvaluationTools.analyze(planD, planC);
        System.out.println(analyze);

        ReconfigurationPlanConverter planConverter = new ReconfigurationPlanConverter();
        SatConstraintsConverter constraintsConverter = new SatConstraintsConverter();
        try {
            ModelConverter modelConverter = new ModelConverter();
            modelConverter.toJSON(model, new File("SampleSpreadModel.json"));
            planConverter.toJSON(planD, new File("planD.json"));
            planConverter.toJSON(planC, new File("planC.json"));
            constraintsConverter.toJSON(dList, new File("discrete.json"));
            constraintsConverter.toJSON(cList, new File("continuous.json"));

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        Assert.assertEquals(model, clone);
    }

    @Test(timeOut = 10000)
    public void spreadTest1() {
        ModelGenerator tm = new ModelGenerator();
        Model m = tm.generateModel(10, 40);
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
        log.info(ctrs.toString());
    }

    @Test(timeOut = 10000)
    public void examSpreadConstraint() {
        ModelGenerator tm = new ModelGenerator();
        Model m = tm.generateModel(2, 2);
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

    @Test
    public void newTestHF() {
        ModelGenerator tm = new ModelGenerator();
        Model model = tm.generateModel(10, 25);
        Spread spread = new Spread(tm.getRandomVMs(5), false);
        Set<SatConstraint> constraints = new HashSet<SatConstraint>();
        constraints.add(spread);

        Model readyModel = EvaluationTools.prepareModel(model, constraints);
        HardwareFailures ic = new HardwareFailures(readyModel, constraints);
        ReconfigurationPlan plan = ic.run();
        spread.setContinuous(true);

        if (plan != null) {
            if (PlanChecker.check(plan, constraints) == null) {
                log.info("Discrete plan satisfies continuous restriction");
            }
        } else {
            log.info("No plan");
        }
    }

    @Test
    public void newTestIL() {
        ModelGenerator tm = new ModelGenerator();
        Model model = tm.generateModel(10, 25);
        Spread spread = new Spread(tm.getRandomVMs(8), false);
        Spread spread2 = new Spread(tm.getRandomVMs(6), false);
        Set<SatConstraint> constraints = new HashSet<SatConstraint>();
        constraints.add(spread);
        constraints.add(spread2);

        Model readyModel = EvaluationTools.prepareModel(model, constraints);
        IncreasingLoad ic = new IncreasingLoad(readyModel, constraints);
        ReconfigurationPlan plan = ic.run();
        spread.setContinuous(true);
        spread2.setContinuous(true);

        if (PlanChecker.check(plan, constraints) != null) {
            log.info("Discrete plan does NOT satisfy continuous restriction");
            ic = new IncreasingLoad(readyModel, constraints);
            ReconfigurationPlan contPlan = ic.run();
            if (PlanChecker.check(contPlan, constraints) == null) {
                log.info("Continuous plan satisfy continuous restriction");
                log.info(EvaluationTools.analyze(plan, contPlan));
            }

        }


    }
}
