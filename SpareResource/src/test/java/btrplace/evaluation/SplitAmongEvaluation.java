package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.SplitAmong;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/13/13
 * Time: 1:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SplitAmongEvaluation {
    private static final Logger log = LoggerFactory.getLogger(TestGoogleTraceDataA.class.getPackage().getName());
    private final String filename = "/user/hdang/home/Downloads/google_trace/dataA/";
    private static Model intermediateModel;

    @Test
    public void testTraceReaderDataA1_3WithOffline3() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_3.txt",
                filename + "assignment_a1_3.txt");
        tr.readModel();
        tr.readAssignment();
        log.info("\n" + tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        Set<SatConstraint> dis_cstrs = new HashSet<SatConstraint>();
        Set<SatConstraint> cont_cstrs = new HashSet<SatConstraint>();

        Set<UUID> vmset1 = tr.getAllServices().get(1);
        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(vmset1);
        Set<Set<UUID>> nodes = new HashSet<Set<UUID>>();
        nodes.add(tr.getNeighborMap().get(1));
        nodes.add(tr.getNeighborMap().get(2));

        SplitAmong sa = new SplitAmong(vmSet, nodes);
        SplitAmong saC = new SplitAmong(vmSet, nodes, true);

        dis_cstrs.add(sa);
        cont_cstrs.add(saC);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, dis_cstrs);
        Assert.assertEquals(saC.isSatisfied(plan.getResult()), SatConstraint.Sat.SATISFIED);

        Evaluation evaluation = new Evaluation(plan.getResult(), dis_cstrs, cont_cstrs);
        evaluation.evaluate();

    }
}
