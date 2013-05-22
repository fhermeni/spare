package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.annotations.Test;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 5/21/13
 * Time: 11:36 PM
 */
public class PreservedEvaluation {

    @Test(timeOut = 10000)
    public void test1() {

        ModelGenerator tmg = new ModelGenerator();
        Model model = tmg.generateModel(100, 400);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Set<SatConstraint> constraints = preserveConstraints(model);
        cra.doOptimize(true);
        cra.setTimeLimit(5);

        EvaluationTools.solve(cra, model, constraints);
    }

    @Test(timeOut = 10000)
    public void TestWebAppAmong() {
        ModelGenerator moGen = new ModelGenerator();
        Model model = moGen.generateModel(100, 300);


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

    @Test(timeOut = 10000)
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
