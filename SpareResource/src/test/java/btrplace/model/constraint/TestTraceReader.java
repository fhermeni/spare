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
 * Date: 5/2/13
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestTraceReader {
    private static final Logger log = LoggerFactory.getLogger(TestTraceReader.class.getPackage().getName());
    private final String filename = "/user/hdang/home/Downloads/google_trace/dataA/";

    @Test
    public void testTraceReaderDataA1_1() throws IOException, SolverException {

        TraceReader tr = new TraceReader(filename + "model_a1_1.txt",
                filename + "assignment_a1_1.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());

        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
        Assert.assertEquals(tr.getNumber_of_nodes(), 4);
        Assert.assertEquals(tr.getNumber_of_services(), 79);
        Assert.assertEquals(tr.getNumber_of_vm(), 100);
    }

    @Test
    public void testTraceReaderDataA1_2() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_2.txt",
                filename + "assignment_a1_2.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 4);
        Assert.assertEquals(tr.getNumber_of_nodes(), 100);
        Assert.assertEquals(tr.getNumber_of_services(), 980);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 0);
    }

    @Test
    public void testTraceReaderDataA1_3() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_3.txt",
                filename + "assignment_a1_3.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 3);
        Assert.assertEquals(tr.getNumber_of_nodes(), 100);
        Assert.assertEquals(tr.getNumber_of_services(), 216);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 0);
    }

    @Test
    public void testTraceReaderDataA1_4() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_4.txt",
                filename + "assignment_a1_4.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 3);
        Assert.assertEquals(tr.getNumber_of_nodes(), 50);
        Assert.assertEquals(tr.getNumber_of_services(), 142);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
    }

    @Test
    public void testTraceReaderDataA1_5() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_5.txt",
                filename + "assignment_a1_5.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());
        Assert.assertEquals(tr.getNumber_resources(), 4);
        Assert.assertEquals(tr.getNumber_of_nodes(), 12);
        Assert.assertEquals(tr.getNumber_of_services(), 981);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
    }

    @Test
    public void testTraceReaderDataA1_1WithGather() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_1.txt",
                filename + "assignment_a1_1.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();

        for (Integer key : tr.getServices_vms().keySet()) {
            ArrayList<UUID> uuids = tr.getServices_vms().get(key);
            System.out.println(uuids);
        }
        for (Integer key : tr.getServices_vms().keySet()) {
            ArrayList<UUID> uuids = tr.getServices_vms().get(key);
            Set<UUID> vmSet = new HashSet<UUID>(uuids);
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
            Assert.assertEquals(gather.isSatisfied(result), SatConstraint.Sat.SATISFIED);
        }
        Assert.assertEquals(model.equals(result), false);

        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
        Assert.assertEquals(tr.getNumber_of_nodes(), 4);
        Assert.assertEquals(tr.getNumber_of_services(), 79);
        Assert.assertEquals(tr.getNumber_of_vm(), 100);
    }

    @Test
    public void testTraceReaderDataA1_3WithLonely() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_3.txt",
                filename + "assignment_a1_3.txt");
        tr.readModel();
        tr.readAssigment();
        log.info(tr.summary());
        Model model = new DefaultModel(tr.getMapping());
        for (ShareableResource sr : tr.getShareableResources()) {
            model.attach(sr);
        }

        List<SatConstraint> constraints = new ArrayList<SatConstraint>();

        for (Integer key : tr.getServices_vms().keySet()) {
            ArrayList<UUID> uuids = tr.getServices_vms().get(key);
            Set<UUID> vmSet = new HashSet<UUID>(uuids);
            Spread spread = new Spread(vmSet, true);
            constraints.add(spread);
        }

        HashSet<UUID> vms1 = new HashSet<UUID>(Arrays.asList(new UUID(0, 1)));
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
