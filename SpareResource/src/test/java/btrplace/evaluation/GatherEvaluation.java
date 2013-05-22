package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/15/13
 * Time: 12:50 PM
 */
public class GatherEvaluation {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test(timeOut = 10000)
    public void gatherTest1() throws SolverException {
        ModelGenerator tm = new ModelGenerator();
        Model m = tm.generateModel(20, 40);
        Set<UUID> apache = tm.getRandomVMs(5);
        Set<UUID> tomcat = tm.getRandomVMs(4);
        Set<UUID> mysql = tm.getRandomVMs(3);

        Set<SatConstraint> ctrsC = new HashSet<SatConstraint>();
        Gather e = new Gather(apache);
        ctrsC.add(e);
        Gather e1 = new Gather(tomcat);
        ctrsC.add(e1);
        Gather e2 = new Gather(mysql);
        ctrsC.add(e2);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(m, ctrsC);
        Model result = plan.getResult();
        e.setContinuous(true);
        e1.setContinuous(true);
        e2.setContinuous(true);

        IncreasingLoad ev = new IncreasingLoad(result, ctrsC);
        ev.run();
    }
}
