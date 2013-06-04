package btrplace.model.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.checker.MinSpareResourcesChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.view.ShareableResource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A constraint to force a set of nodes to reserve a minimum number (n) of spare
 * resources for providing immediately resources to VMs in case of VMs
 * increasing load
 * <p/>
 * In discrete restriction mode, the constraint only ensure that the set of
 * nodes reserve at least n number of spare resources at the end of the
 * reconfiguration process. The nodes may have fewer than n number of spare
 * resources in the reconfiguration process.
 * <p/>
 * In continuous restriction mode, if a VM is going to be relocated in this set
 * of nodes, the nodes must have more spare resources than the resource demand
 * of the VM pluses n number.
 *
 * @author Tu Huynh Dang
 */

public class MinSpareResources extends SatConstraint {

    /**
     * Resource identifier
     */
    private final String rcId;

    /**
     * number of reserved resources
     */
    private final int qty;

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param servers the group of nodes
     * @param rc      the resource identifier
     * @param n       the number of resources to be reserved
     */
    public MinSpareResources(Set<Node> servers, String rc, int n) {
        this(servers, rc, n, false);

    }

    /**
     * @param servers    the group of nodes
     * @param rc         the resource identifier
     * @param n          the number of resources to be reserved
     * @param continuous {@code true} for a continuous restriction.
     */
    public MinSpareResources(Set<Node> servers, String rc, int n, boolean continuous) {
        super(Collections.<VM>emptySet(), servers, continuous);
        rcId = rc;
        qty = n;
    }

    /**
     * Get the resource identifier.
     *
     * @return a String
     */
    public String getResource() {
        return rcId;
    }

    /**
     * Get the amount of reserved resources
     *
     * @return a positive integer
     */
    public int getAmount() {
        return qty;
    }

    @Override
    public boolean isSatisfied(Model i) {
        int spare = 0;
        Mapping map = i.getMapping();
        Set<Node> onnodes = map.getOnlineNodes();
        Set<Node> nodes = new HashSet<Node>(onnodes);
        nodes.retainAll(getInvolvedNodes());

        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + rcId);

        if (rc == null) {
            return false;
        }

        for (Node nj : nodes) {
            spare += rc.getCapacity(nj);
        }

        for (Node nj : nodes) {
            for (VM vmId : i.getMapping().getRunningVMs(nj)) {
                spare -= rc.getConsumption(vmId);
                if (spare < qty)
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

        return qty == that.qty && rcId.equals(that.rcId)
                && getInvolvedNodes().equals(that.getInvolvedNodes())
                && this.isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + qty;
        result = 31 * result + rcId.hashCode();
        result = 31 * result + getInvolvedNodes().hashCode() + (isContinuous() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("minSpareResources(").append("nodes=").append(getInvolvedNodes()).append(", rc=")
                .append(rcId).append(", amount=").append(qty);

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
        return new MinSpareResourcesChecker(this);
    }

}
