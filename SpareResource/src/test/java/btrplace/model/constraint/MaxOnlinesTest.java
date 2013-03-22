package btrplace.model.constraint;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint.Sat;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;
import btrplace.test.PremadeElements;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MaxOnlinesTest implements PremadeElements {

    @Test
    public void maxOnlinesSetUUIDintboolean() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        MaxOnlines mo = new MaxOnlines(s, 1);
        Assert.assertEquals(s, mo.getInvolvedNodes());
        Assert.assertEquals(1, mo.getAmount());
        Assert.assertFalse(mo.isContinuous());
        mo.setContinuous(true);
        Assert.assertTrue(mo.isContinuous());

        System.out.println(mo.toString());
    }

    @Test
    public void isSatisfiedModel() {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        Model model = new DefaultModel(map);
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2, n3));
        MaxOnlines mo = new MaxOnlines(s, 2);

        Assert.assertEquals(mo.isSatisfied(model), Sat.SATISFIED);

        model.getMapping().addOnlineNode(n3);
        Assert.assertEquals(mo.isSatisfied(model), Sat.UNSATISFIED);
    }

    @Test
    public void isSatisfiedReconfigurationPlan() {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        Model model = new DefaultModel(map);
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2, n3));
        MaxOnlines mo = new MaxOnlines(s, 2);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);

        Assert.assertEquals(mo.isSatisfied(plan), Sat.SATISFIED);

        plan.add(new BootNode(n3, 3, 9));
        Assert.assertEquals(mo.isSatisfied(plan), Sat.UNSATISFIED);

        plan.add(new ShutdownNode(n2, 0, 5));
        Assert.assertEquals(mo.isSatisfied(plan), Sat.SATISFIED);

    }
}
