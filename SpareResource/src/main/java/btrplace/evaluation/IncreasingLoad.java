package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * User: Tu Huynh Dang
 * Date: 5/22/13
 * Time: 12:01 AM
 */
public class IncreasingLoad {
    private static ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
    private Model model;
    private Set<SatConstraint> constraints;

    public IncreasingLoad(Model m, Set<SatConstraint> c) {
        model = m;
        constraints = c;
    }

    public IncreasingLoad(Model m, SatConstraint c) {
        this(m, new HashSet<SatConstraint>(Arrays.asList(c)));
    }


    public ReconfigurationPlan run() {
        constraints.addAll(preserveForInvolveVMs());
        ReconfigurationPlan plan = EvaluationTools.solve(cra, model, constraints);
        return plan;
    }

    private Set<SatConstraint> preserveConstraints(Model model) {
        Set<SatConstraint> constraints = new HashSet<SatConstraint>();
        Set<UUID> vms = model.getMapping().getRunningVMs();
        Iterator<UUID> iter = vms.iterator();
        for (int i = 0; iter.hasNext() && i < vms.size() / 2; i++) {
            UUID vm = iter.next();
            constraints.add(new Preserve(Collections.singleton(vm), "cpu", 4));
        }
        return constraints;
    }

    private Set<SatConstraint> preserveForInvolveVMs() {
        ShareableResource sr = (ShareableResource) model.getView("ShareableResource.cpu");

        Set<SatConstraint> additional_constraint = new HashSet<SatConstraint>();
        for (SatConstraint c : constraints) {
            for (UUID vm : c.getInvolvedVMs()) {
                additional_constraint.add(new Preserve(new HashSet<UUID>(Collections.singleton(vm)), "cpu", sr.get(vm) + 2));
            }
        }
        return additional_constraint;
    }
}
