package btrplace.model.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
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

    @Test
    public void testTraceReaderWithSpread() throws IOException, SolverException {
        TraceReader tr = new TraceReader("/user/hdang/home/Downloads/google_trace/model_a1_1.txt",
                "/user/hdang/home/Downloads/google_trace/assignment_a1_1.txt");
        tr.readModel();
        tr.readAssigment();

        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
        Assert.assertEquals(tr.getNumber_of_nodes(), 4);
        Assert.assertEquals(tr.getNumber_of_services(), 79);
        Assert.assertEquals(tr.getNumber_of_vm(), 100);
    }

    @Test
    public void testTraceReader2WithSpread() throws IOException, SolverException {
        TraceReader tr = new TraceReader("/user/hdang/home/Downloads/google_trace/model_a1_2.txt",
                "/user/hdang/home/Downloads/google_trace/assignment_a1_2.txt");
        tr.readModel();
        tr.readAssigment();

        Assert.assertEquals(tr.getNumber_resources(), 4);
        Assert.assertEquals(tr.getNumber_of_nodes(), 100);
        Assert.assertEquals(tr.getNumber_of_services(), 980);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 0);
    }

    @Test
    public void testTraceReaderSet3() throws IOException, SolverException {
        TraceReader tr = new TraceReader("/user/hdang/home/Downloads/google_trace/model_a1_3.txt",
                "/user/hdang/home/Downloads/google_trace/assignment_a1_3.txt");
        tr.readModel();
        tr.readAssigment();

        Assert.assertEquals(tr.getNumber_resources(), 3);
        Assert.assertEquals(tr.getNumber_of_nodes(), 100);
        Assert.assertEquals(tr.getNumber_of_services(), 216);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 0);
    }

    @Test
    public void testTraceReaderSet4() throws IOException, SolverException {
        TraceReader tr = new TraceReader("/user/hdang/home/Downloads/google_trace/model_a1_4.txt",
                "/user/hdang/home/Downloads/google_trace/assignment_a1_4.txt");
        tr.readModel();
        tr.readAssigment();

        Assert.assertEquals(tr.getNumber_resources(), 3);
        Assert.assertEquals(tr.getNumber_of_nodes(), 50);
        Assert.assertEquals(tr.getNumber_of_services(), 142);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
    }

    @Test
    public void testTraceReaderSet5() throws IOException, SolverException {
        TraceReader tr = new TraceReader("/user/hdang/home/Downloads/google_trace/model_a1_5.txt",
                "/user/hdang/home/Downloads/google_trace/assignment_a1_5.txt");
        tr.readModel();
        tr.readAssigment();

        Assert.assertEquals(tr.getNumber_resources(), 4);
        Assert.assertEquals(tr.getNumber_of_nodes(), 12);
        Assert.assertEquals(tr.getNumber_of_services(), 981);
        Assert.assertEquals(tr.getNumber_of_vm(), 1000);
        Assert.assertEquals(tr.getNumber_of_balance_cost(), 1);
    }

    @Test
    public void testTraceReaderSet1WithGather() throws IOException, SolverException {
        TraceReader tr = new TraceReader("/user/hdang/home/Downloads/google_trace/model_a1_1.txt",
                "/user/hdang/home/Downloads/google_trace/assignment_a1_1.txt");
        tr.readModel();
        tr.readAssigment();

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

}
