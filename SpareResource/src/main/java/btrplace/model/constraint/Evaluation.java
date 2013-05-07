package btrplace.model.constraint;

import btrplace.model.Model;
import btrplace.model.SatConstraint;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/7/13
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Evaluation {

    private Model model;
    private Set<SatConstraint> constraints;

    public Evaluation(Model m, Set<SatConstraint> c) {
        model = m;
        constraints = c;
    }

    public void evaluate() {

    }

    public boolean isSatisfiedDiscreteRestriction() {

        return false;
    }

    public boolean isSatisfiedContinuousRestriction() {

        return false;
    }

}
