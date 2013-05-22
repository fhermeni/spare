package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Gather;
import btrplace.model.constraint.SatConstraint;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestStuff {

    public static void main(String[] args) {
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

        Object[] copy = ctrsC.toArray();
        Set<SatConstraint> clone = new HashSet<SatConstraint>();


        for (Object c : copy) {
            clone.add((SatConstraint) c);
        }

        for (SatConstraint c : clone) {
            c.setContinuous(true);
        }
        System.out.println(clone);

        System.out.println(ctrsC);
    }
}
