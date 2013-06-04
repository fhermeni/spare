package btrplace.model.constraint;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.checker.MinSpareNodeChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * A constraint to force a set of nodes to reserve a minimum number of spare
 * nodes for providing immediately resources to VMs in case of VMs increasing
 * load
 * <p/>
 * In discrete restriction mode, the constraint only ensure that the set of
 * nodes reserve at least n number of spare nodes at the end of the
 * reconfiguration process. The nodes may have fewer than n number of spare
 * nodes in the reconfiguration process.
 * <p/>
 * In continuous restriction mode, there are always at least n number of spare
 * nodes in the reconfiguration process.
 *
 * @author Tu Huynh Dang
 */

public class MinSpareNode extends SatConstraint {

    /**
     * number of reserved nodes
     */
    private final int qty;

    private HashMap<Node, Integer> nodemap = new HashMap<Node, Integer>();

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param nodes the group of nodes
     * @param n     the number of nodes to be reserved
     */
    public MinSpareNode(Set<Node> nodes, int n) {
        this(nodes, n, false);

    }

    /**
     * Make a new constraint stating the restriction explicitly
     *
     * @param servers    the group of nodes
     * @param n          the number of nodes to be reserved
     * @param continuous {@code true} for a continuous restriction.
     */
    public MinSpareNode(Set<Node> servers, int n, boolean continuous) {
        super(Collections.<VM>emptySet(), servers, continuous);
        qty = n;
        int i = 0;
        for (Node node : getInvolvedNodes()) {
            nodemap.put(node, i);
            i++;
        }
    }

    /**
     * Get the amount of reserved nodes
     *
     * @return a positive integer
     */
    public int getAmount() {
        return qty;
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

        MinSpareNode that = (MinSpareNode) o;

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
        b.append("minSpareNode(").append("nodes=").append(getInvolvedNodes()).append(", amount=")
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
        return new MinSpareNodeChecker(this);
    }
}
