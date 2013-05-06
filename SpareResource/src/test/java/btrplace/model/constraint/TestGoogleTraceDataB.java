package btrplace.model.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/3/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestGoogleTraceDataB {

    private static final Logger log = LoggerFactory.getLogger(TestGoogleTraceDataB.class.getPackage().getName());
    private final String filepath = "/user/hdang/home/Downloads/google_trace/dataB/";

    @Test
    public void testDataB_1() throws IOException, SolverException {

        String file = "b_1.txt";
        TraceReader tr = new TraceReader(filepath + "model_" + file,
                filepath + "assignment_" + file);
        tr.readModel();
        tr.readAssigment();
        log.info("\n" + tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();
        int i = 0;
        for (Integer key : tr.getAllServices().keySet()) {
            ArrayList<UUID> uuids = tr.getAllServices().get(key);
            Set<UUID> vmSet = new HashSet<UUID>(uuids);
            Spread spread = new Spread(vmSet, true);
            constraints.add(spread);
            if (i++ > 5) break;

        }

        HashSet<UUID> vms1 = new HashSet<UUID>(Arrays.asList(new UUID(0, 1), new UUID(0, 10)));
        Lonely lonely = new Lonely(vms1);
        constraints.add(lonely);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setVerbosity(0);

        ReconfigurationPlan plan = cra.solve(model, constraints);

        for (SatConstraint sat : constraints) {
            if (sat.isContinuous()) {
                Assert.assertEquals(sat.isSatisfied(plan), SatConstraint.Sat.SATISFIED);
            } else {
                Assert.assertEquals(sat.isSatisfied(plan.getResult()), SatConstraint.Sat.SATISFIED);
            }
        }
        Model result = plan.getResult();
        Assert.assertEquals(model.equals(result), false);

        log.info(plan.toString());

    }
}
