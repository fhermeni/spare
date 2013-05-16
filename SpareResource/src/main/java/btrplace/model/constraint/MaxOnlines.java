package btrplace.model.constraint;

import btrplace.model.Model;
import btrplace.model.constraint.checker.MaxOnlinesChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force a set of nodes to have a maximum number (n) of nodes to
 * be online
 * <p/>
 * In discrete restriction mode, the constraint only ensure that the set of
 * nodes have at most n nodes being online at the end of the reconfiguration
 * process. The set of nodes may have more number than n nodes being online in
 * the reconfiguration process.
 * <p/>
 * In continuous restriction mode, a boot node action is performed only when the
 * number of online nodes is smaller than n.
 *
 * @author Tu Huynh Dang
 */

public class MaxOnlines extends SatConstraint {

    /**
     * number of reserved nodes
     */
    private final int qty;

    /**
     * Make new constraint specifying restriction explicitly
     *
     * @param nodes      The set of nodes
     * @param n          The reserved number of spare nodes
     * @param continuous {@code true} for continuous restriction
     */
    public MaxOnlines(Set<UUID> nodes, int n, boolean continuous) {
        super(Collections.<UUID>emptySet(), nodes, continuous);
        qty = n;
    }

    public MaxOnlines(Set<UUID> nodes, int n) {
        this(nodes, n, false);
    }

    /**
     * Get the amount of spare nodes
     *
     * @return a positive integer
     */
    public int getAmount() {
        return qty;
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan p) {
        Model mo = p.getOrigin();
        if (!isSatisfied(mo)) {
            return false;
        }
        mo = p.getOrigin().clone();
        for (Action a : p) {
            if (!a.apply(mo)) {
                return false;
            }
            if (!isSatisfied(mo)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        MinSpareResources that = (MinSpareResources) o;

        return qty == that.getAmount() && getInvolvedNodes().equals(that.getInvolvedNodes())
                && this.isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + qty;
        result = 31 * result + getInvolvedNodes().hashCode() + (isContinuous() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("maxOnlines(").append("nodes=").append(getInvolvedNodes()).append(", amount=")
                .append(qty);

        if (isContinuous()) {
            b.append(", continuous");
        } else {
            b.append(", discrete");
        }
        b.append(')');

        return b.toString();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new MaxOnlinesChecker(this);
    }

}
