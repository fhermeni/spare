/**
 *
 */
package btrplace.model.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;

import java.util.*;

/**
 * A constraint to force a set of nodes to reserve a maximum number (n) of spare
 * resources for efficient usage of nodes
 * <p/>
 * In discrete restriction mode, the constraint only ensure that the set of
 * nodes reserve at most n number of spare nodes at the end of the
 * reconfiguration process. The nodes may have more number of spare nodes than n
 * in the reconfiguration process.
 * <p/>
 * In continuous restriction mode, a VM departs from this set of nodes only when
 * the spare resources increase but do not pass the upper bound by n.
 *
 * @author Tu Huynh Dang
 */
public class MaxSpareResources extends SatConstraint {

    /**
     * Resource identifier
     */
    private final String rcId;

    /**
     * upper bound number of spare resources
     */
    private final int qty;

    /**
     * Make a new constraint
     *
     * @param servers    the group of nodes
     * @param rc         the resource identifier
     * @param n          the number of resources to be reserved
     * @param continuous {@code true} for a continuous restriction.
     */
    public MaxSpareResources(Collection<UUID> servers, String rc, int n, boolean continuous) {
        super(Collections.<UUID>emptySet(), servers, continuous);
        rcId = rc;
        qty = n;
    }

    /**
     * Make a new constraint with discrete restriction
     *
     * @param servers the group of nodes
     * @param rc      the resource identifier
     * @param n       the number of resources to be reserved
     */
    public MaxSpareResources(Set<UUID> servers, String rc, int n) {
        this(servers, rc, n, false);

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
    public Sat isSatisfied(Model i) {
        int spare = 0;
        Mapping map = i.getMapping();
        Set<UUID> onnodes = map.getOnlineNodes();
        Set<UUID> nodes = new HashSet<UUID>(onnodes);
        nodes.retainAll(getInvolvedNodes());

        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + rcId);

        if (rc == null) {
            return Sat.UNSATISFIED;
        }

        for (UUID nj : nodes) {
            spare += rc.get(nj);
        }

        for (UUID nj : nodes) {
            for (UUID vmId : i.getMapping().getRunningVMs(nj)) {
                spare -= rc.get(vmId);
                if (spare <= qty)
                    return Sat.SATISFIED;
            }
        }

        return Sat.UNSATISFIED;
    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan p) {
        Model mo = p.getOrigin();
        if (!isSatisfied(mo).equals(Sat.SATISFIED)) {
            return Sat.UNSATISFIED;
        }
        mo = p.getOrigin().clone();
        for (Action a : p) {
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            if (!isSatisfied(mo).equals(Sat.SATISFIED)) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;
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

        return qty == that.getAmount() && rcId.equals(that.getResource())
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
        b.append("maxSpareResources(").append("nodes=").append(getInvolvedNodes()).append(", rc=")
                .append(rcId).append(", amount=").append(qty);

        if (isContinuous()) {
            b.append(", continuous");
        } else {
            b.append(", discrete");
        }
        b.append(')');

        return b.toString();
    }

}
