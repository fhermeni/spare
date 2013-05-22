package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Split;
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
import java.util.*;

/**
 * User: TU HUYNH DANG
 * Date: 5/13/13
 * Time: 1:15 PM
 */
public class SplitAmongEvaluation {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String filename = "/user/hdang/home/Downloads/google_trace/dataA/";


    @Test(timeOut = 10000)
    public void splitAmongTest1() {
        ModelGenerator tm = new ModelGenerator();
        Model m = tm.generateModel(10, 40);
        Set<UUID> apache = tm.getRandomVMs(4);
        Set<UUID> tomcat = tm.getRandomVMs(3);
        Set<UUID> mysql = tm.getRandomVMs(2);

        Set<Set<UUID>> vm_set = new HashSet<Set<UUID>>();
        vm_set.add(mysql);
        vm_set.add(apache);

        Set<UUID> low_delay = tm.getRandomNodes(2);
        Set<UUID> low_delay2 = tm.getRandomNodes(4);

        Set<Set<UUID>> node_set = new HashSet<Set<UUID>>();
        node_set.add(low_delay);
        node_set.add(low_delay2);

        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        Set<SatConstraint> ctrsC = new HashSet<SatConstraint>();

//        ctrs.add(new Spread(apache, false));
//        ctrs.add(new Spread(tomcat, false));
        ctrs.add(new SplitAmong(vm_set, node_set));

//        ctrsC.add(new Spread(apache, false));
//        ctrsC.add(new Spread(tomcat, false));
        ctrsC.add(new SplitAmong(vm_set, node_set, true));

        HardwareFailures ev = new HardwareFailures(m, ctrs, ctrsC);
        ev.evaluate();
    }

    @Test(timeOut = 10000)
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

        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(tr.getAllServices().get(1));
        vmSet.add(tr.getAllServices().get(2));

        Set<Set<UUID>> nodes = new HashSet<Set<UUID>>();
        nodes.add(tr.getNeighborMap().get(1));
        nodes.add(tr.getNeighborMap().get(2));
        nodes.add(tr.getNeighborMap().get(3));

        SplitAmong sa = new SplitAmong(vmSet, nodes);
        SplitAmong saC = new SplitAmong(vmSet, nodes, true);

        dis_cstrs.add(sa);
        cont_cstrs.add(saC);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, dis_cstrs);
        Model result = plan.getResult();
        Assert.assertNotEquals(result, model);
        Assert.assertTrue(saC.isSatisfied(result));
        HardwareFailures hardwareFailures = new HardwareFailures(result, dis_cstrs, cont_cstrs);
        hardwareFailures.evaluate();
    }

    @Test(timeOut = 10000)
    public void testTraceReaderDataA1_1() throws IOException, SolverException {
        TraceReader tr = new TraceReader(filename + "model_a1_1.txt",
                filename + "assignment_a1_1.txt");
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
        nodes.add(tr.getLocationMap().get(1));
        nodes.add(tr.getLocationMap().get(2));

        SplitAmong sa = new SplitAmong(vmSet, nodes);
        SplitAmong saC = new SplitAmong(vmSet, nodes, true);

        dis_cstrs.add(sa);
        cont_cstrs.add(saC);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, dis_cstrs);
        Model result = plan.getResult();
        Assert.assertNotEquals(result, model);
        Assert.assertTrue(saC.isSatisfied(result));
        HardwareFailures hardwareFailures = new HardwareFailures(result, dis_cstrs, cont_cstrs);
        hardwareFailures.evaluate();
    }

    @Test(timeOut = 10000)
    public void testSplit() throws IOException, SolverException {
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
        Set<UUID> vmset2 = tr.getAllServices().get(2);
        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(vmset1);
        vmSet.add(vmset2);

        Split sa = new Split(vmSet);
        Split saC = new Split(vmSet, true);

        dis_cstrs.add(sa);
        cont_cstrs.add(saC);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, dis_cstrs);
        Model result = plan.getResult();
        Assert.assertNotEquals(result, model);
        Assert.assertTrue(saC.isSatisfied(result));
        HardwareFailures hardwareFailures = new HardwareFailures(result, dis_cstrs, cont_cstrs);
        hardwareFailures.evaluate();
    }

    @Test(timeOut = 10000)
    public void testSplitAmongContinuous() throws IOException, SolverException, JSONConverterException {
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

        Set<Set<UUID>> vmSet = new HashSet<Set<UUID>>();
        vmSet.add(tr.getAllServices().get(1));

        Set<Set<UUID>> nodes = new HashSet<Set<UUID>>();
        nodes.add(tr.getNeighborMap().get(1));
        nodes.add(tr.getNeighborMap().get(2));

        log.info("VMs: {}, Set 1:{}, Set 2:{}", tr.getAllServices().get(1).size(), tr.getNeighborMap().get(1).size(),
                tr.getNeighborMap().get(2).size());
        SplitAmong sa = new SplitAmong(vmSet, nodes);
        SplitAmong saC = new SplitAmong(vmSet, nodes, true);

        dis_cstrs.add(sa);
        cont_cstrs.add(saC);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, dis_cstrs);
        Model result = plan.getResult();
        Assert.assertNotEquals(result, model);
        Assert.assertTrue(saC.isSatisfied(result));

        Random rand = new Random();
        Set<Offline> offs = new HashSet<Offline>();
        for (int i = 1; i <= 7; i++) {
            int randomId = rand.nextInt(100);
            log.info("Shutdown node: " + randomId);
            Offline offline = new Offline(new HashSet<UUID>(Arrays.asList(new UUID(1, randomId))));
            offs.add(offline);
        }
        cont_cstrs.addAll(offs);
        plan = cra.solve(result, cont_cstrs);
        Assert.assertNotNull(plan);
        log.info(plan.toString());
    }
}
