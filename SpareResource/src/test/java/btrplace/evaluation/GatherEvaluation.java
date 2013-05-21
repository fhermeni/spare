package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.SatConstraint;
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

    @Test
    public void gatherTest1() {
        TestModelGenerator tm = new TestModelGenerator(10, 20);
        Model m = tm.generateModel();
        Set<UUID> apache = tm.getRandomVMs(5);
        Set<UUID> tomcat = tm.getRandomVMs(4);
        Set<UUID> mysql = tm.getRandomVMs(3);

        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        Set<SatConstraint> ctrsC = new HashSet<SatConstraint>();
        ctrs.add(new Gather(apache));
        ctrs.add(new Gather(tomcat));
        ctrs.add(new Gather(mysql));
        ctrsC.add(new Gather(apache, true));
        ctrsC.add(new Gather(tomcat, true));
        ctrsC.add(new Gather(mysql, true));
        log.info(m.toString());
        log.info(ctrs.toString());
        ShutdownEvaluation ev = new ShutdownEvaluation(m, ctrs, ctrsC);
        ev.evaluate();
    }
}
