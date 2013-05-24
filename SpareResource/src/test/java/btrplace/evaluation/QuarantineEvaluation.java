package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.model.InstanceConverter;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.Quarantine;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.annotations.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 11:24 AM
 */
public class QuarantineEvaluation {
    @Test(timeOut = 10000)
    public void test1() throws SolverException, IOException, JSONConverterException {
        ModelGenerator gen = new ModelGenerator();
        Model model = gen.generateModel(20, 30);

        Quarantine constraint = new Quarantine(gen.getRandomNodes(2));
        Quarantine constraint2 = new Quarantine(gen.getRandomNodes(2));

        Instance instance = new Instance(model, new ArrayList<SatConstraint>(Arrays.asList(constraint, constraint2)));
        InstanceConverter converter = new InstanceConverter();
        FileWriter fw = new FileWriter("QuarantineInstance.json");
        fw.write(converter.toJSONString(instance));
        fw.close();

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, Collections.<SatConstraint>singleton(constraint));
        Model result = plan.getResult();
        constraint.setContinuous(true);
        IncreasingLoad incLoad = new IncreasingLoad(result, constraint);
        incLoad.run();
    }
}
