package btrplace.evaluation;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.SplitAmong;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.IOException;
import java.util.*;


/**
 * User: TU HUYNH DANG
 * Date: 5/2/13
 * Time: 2:41 PM
 */
public class TestGoogleTraceDataA {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String filename = "/user/hdang/home/Downloads/google_trace/dataA/";
    private static Model intermediateModel;

    public void testTraceReaderDataA1_1() throws IOException, SolverException {

        TraceReader tr = new TraceReader(filename + "model_a1_1.txt",
                filename + "assignment_a1_1.txt");
        tr.readModel();
        tr.readAssignment();
        log.info(tr.summary());

        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
        Assert.assertEquals(tr.getNumber_of_nodes(), 4);
        Assert.assertEquals(tr.getNumber_of_services(), 79);
        Assert.assertEquals(tr.getNumber_of_vm(), 100);
    }

    public void testTraceReaderDataA1_2() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_2.txt",
                filename + "assignment_a1_2.txt");
        tr.readModel();
        tr.readAssignment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 4);
        Assert.assertEquals(tr.getNumber_of_nodes(), 100);
        Assert.assertEquals(tr.getNumber_of_services(), 980);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 0);
    }

    public void testTraceReaderDataA1_3() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_3.txt",
                filename + "assignment_a1_3.txt");
        tr.readModel();
        tr.readAssignment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 3);
        Assert.assertEquals(tr.getNumber_of_nodes(), 100);
        Assert.assertEquals(tr.getNumber_of_services(), 216);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 0);
    }

    public void testTraceReaderDataA1_4() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_4.txt",
                filename + "assignment_a1_4.txt");
        tr.readModel();
        tr.readAssignment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 3);
        Assert.assertEquals(tr.getNumber_of_nodes(), 50);
        Assert.assertEquals(tr.getNumber_of_services(), 142);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
    }

    public void testTraceReaderDataA1_5() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_5.txt",
                filename + "assignment_a1_5.txt");
        tr.readModel();
        tr.readAssignment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 4);
        Assert.assertEquals(tr.getNumber_of_nodes(), 12);
        Assert.assertEquals(tr.getNumber_of_services(), 981);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
    }

    public void testTraceReaderDataA1_1WithGather() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_1.txt",
                filename + "assignment_a1_1.txt");
        tr.readModel();
        tr.readAssignment();
        log.info(tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();

        for (Integer key : tr.getAllServices().keySet()) {
            Set<UUID> uuids = tr.getAllServices().get(key);
            System.out.println(uuids);
        }
        for (Integer key : tr.getAllServices().keySet()) {
            Set<UUID> vmSet = tr.getAllServices().get(key);
            Gather gather = new Gather(vmSet);
            constraints.add(gather);
        }

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, constraints);
        cra.setVerbosity(0);
        System.out.println(plan.toString());
        //System.out.println(plan.getResult().getMapping().toString());

        Model result = plan.getResult();
        for (SatConstraint sat : constraints) {
            Gather gather = (Gather) sat;
            Assert.assertTrue(gather.isSatisfied(result));
        }
        Assert.assertEquals(model.equals(result), false);

        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
        Assert.assertEquals(tr.getNumber_of_nodes(), 4);
        Assert.assertEquals(tr.getNumber_of_services(), 79);
        Assert.assertEquals(tr.getNumber_of_vm(), 100);
    }

    public void testTraceReaderDataA1_3WithOffline() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_3.txt",
                filename + "assignment_a1_3.txt");
        tr.readModel();
        tr.readAssignment();
        log.info("\n" + tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();

        Set<UUID> uuids = tr.getAllServices().get(1);
        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(new HashSet<UUID>(uuids));
//        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(new UUID(1, 1), new UUID(1, 2), new UUID(1, 3)));
        Set<Set<UUID>> nodes = new HashSet<Set<UUID>>();
        nodes.add(new HashSet<UUID>(tr.getNeighborMap().get(1)));

        SplitAmong sa = new SplitAmong(vmSet, nodes);
        constraints.add(sa);

        for (SatConstraint sat : constraints) {
            log.info(sat.toString());
        }

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        ReconfigurationPlan plan = cra.solve(model, constraints);

        intermediateModel = plan.getResult();
        Assert.assertEquals(model.equals(intermediateModel), false);
        log.info("\nNumber of actions: " + plan.getSize());
        log.info("\n" + plan.toString());

        Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(new UUID(1, 1))));
        constraints.add(offline);
        plan = cra.solve(model, constraints);
        sa.setContinuous(true);
        for (SatConstraint sat : constraints) {
            log.info(sat.toString());
        }

        intermediateModel = plan.getResult();
        Assert.assertEquals(plan.getOrigin().equals(plan.getResult()), false);
        log.info("\nNumber of actions: " + plan.getSize());
        log.info("\n" + plan.toString());
    }

    /**
     * This test the discrete restriction of splitAmong constraint
     * The RP satisfies discrete restriction but it doesn't satisfy continuous restriction
     *
     * @throws IOException
     * @throws SolverException
     */
    public void testTraceReaderDataA1_3WithOffline2() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_3.txt",
                filename + "assignment_a1_3.txt");
        tr.readModel();
        tr.readAssignment();
        log.info("\n" + tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();

        Set<UUID> vmset1 = tr.getAllServices().get(1);
        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(vmset1);
//        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(new UUID(1, 1), new UUID(1, 2), new UUID(1, 3)));
        Set<Set<UUID>> nodes = new HashSet<Set<UUID>>();
        nodes.add(tr.getNeighborMap().get(1));
        nodes.add(tr.getNeighborMap().get(2));

        SplitAmong sa = new SplitAmong(vmSet, nodes);
        constraints.add(sa);

        for (SatConstraint sat : constraints) {
            log.info(sat.toString());
        }

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        ReconfigurationPlan plan = cra.solve(model, constraints);

        intermediateModel = plan.getResult();
        Assert.assertEquals(model.equals(intermediateModel), false);
        log.info("\nNumber of actions: " + plan.getSize());
        log.info("\n" + plan.toString());

        Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(new UUID(1, 1))));
        constraints.add(offline);
        plan = cra.solve(model, constraints);

        for (SatConstraint sat : constraints) {
            log.info(sat.toString());
        }

        intermediateModel = plan.getResult();
        Assert.assertEquals(plan.getOrigin().equals(plan.getResult()), false);
        log.info("\nNumber of actions: " + plan.getSize());
        log.info("\n" + plan.toString());
        Assert.assertTrue(sa.isSatisfied(plan.getResult()));
        Assert.assertTrue(sa.isSatisfied(plan));
    }

    /**
     * This test the discrete restriction of splitAmong constraint
     * The RP satisfies discrete restriction and continuous restriction
     *
     * @throws IOException
     * @throws SolverException
     */
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

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();

        Set<UUID> vmset1 = tr.getAllServices().get(1);
        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(vmset1);
//        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(new UUID(1, 1), new UUID(1, 2), new UUID(1, 3)));
        Set<Set<UUID>> nodes = new HashSet<Set<UUID>>();
        nodes.add(tr.getNeighborMap().get(1));
        nodes.add(tr.getNeighborMap().get(2));

        SplitAmong sa = new SplitAmong(vmSet, nodes);
        constraints.add(sa);

        for (SatConstraint sat : constraints) {
            log.info(sat.toString());
        }

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, constraints);

        intermediateModel = plan.getResult();
        Assert.assertEquals(model.equals(intermediateModel), false);
        Assert.assertTrue(sa.isSatisfied(plan.getResult()));
        log.info("\nNumber of actions: " + plan.getSize());
        log.info("\n" + plan.toString());

        Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(new UUID(1, 1))));
        constraints.add(offline);
        sa.setContinuous(true);

        plan = cra.solve(intermediateModel, constraints);
        for (SatConstraint sat : constraints) {
            log.info(sat.toString());
        }

        Assert.assertEquals(plan.getOrigin().equals(plan.getResult()), false);
        log.info("\nNumber of actions: " + plan.getSize());
        log.info("\n" + plan.toString());
        Assert.assertTrue(sa.isSatisfied(plan));
    }
}
