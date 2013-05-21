package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.annotations.Test;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 5/21/13
 * Time: 11:36 PM
 */
public class PreservedEvaluation extends Datacenter {

    @Test
    public void test1() {

        TestModelGenerator tmg = new TestModelGenerator(100, 400);
        Model model = tmg.generateModel();
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Set<SatConstraint> constraints = preserveConstraints(model);
        cra.doOptimize(true);
        cra.setTimeLimit(5);

        EvaluationTools.solve(cra, model, constraints);
    }

    @Test
    public void TestWebAppAmong() {
        TestModelGenerator moGen = new TestModelGenerator(100, 300);
        Model model = moGen.generateModel();


        Set<Set<UUID>> nodeSets = new HashSet<Set<UUID>>();
        nodeSets.add(moGen.getRandomNodes(10));
        nodeSets.add(moGen.getRandomNodes(10));

        Set<SatConstraint> dcons = new HashSet<SatConstraint>();
        Set<SatConstraint> ccons = new HashSet<SatConstraint>();
        dcons.add(new Among(moGen.getRandomVMs(10), nodeSets));
        ccons.add(new Among(moGen.getRandomVMs(10), nodeSets, true));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        EvaluationTools.solve(cra, model, dcons);
        Model clone_model = model.clone();

        ccons.addAll(preserveConstraints(clone_model));
        EvaluationTools.solve(cra, clone_model, ccons);
    }

    private Set<SatConstraint> preserveConstraints(Model model) {
        Set<SatConstraint> constraints = new HashSet<SatConstraint>();


        Set<UUID> vms = model.getMapping().getRunningVMs();
        Iterator<UUID> iter = vms.iterator();
        for (int i = 0; iter.hasNext() && i < 30; i++) {
            UUID vm = iter.next();
            constraints.add(new Preserve(Collections.singleton(vm), "cpu", 4));
        }
        return constraints;
    }

}
