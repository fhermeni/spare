/**
 * 
 */
package btrplace.model.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;


/**
 * @author Tu Huynh Dang
 *
 */
public class MaxSpareResources extends SatConstraint {
	
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
	 * @param servers
	 *            the group of nodes
	 * @param rc
	 *            the resource identifier
	 * @param n
	 *            the number of resources to be reserved
	 */
	public MaxSpareResources(Set<UUID> servers, String rc, int n) {
		this(servers, rc, n, false);

	}
	
	public MaxSpareResources(Collection<UUID> servers, String rc, int n,boolean c) {
		super(Collections.<UUID> emptySet(), servers, c);
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
	public Sat isSatisfied(Model i) {
		int spare = 0;
		Mapping map = i.getMapping();
		Set<UUID> onNodes = map.getOnlineNodes();

		ShareableResource rc = (ShareableResource) i
				.getView(ShareableResource.VIEW_ID_BASE + rcId);

		if (rc == null) {
			return Sat.UNSATISFIED;
		}

		for (UUID nj : getInvolvedNodes()) {
			if (onNodes.contains(nj)) {
				spare += rc.get(nj);
			}
		}

		for (UUID nj : getInvolvedNodes()) {
			if (onNodes.contains(nj)) {
				for (UUID vmId : i.getMapping().getRunningVMs(nj)) {
					spare -= rc.get(vmId);
					if (spare <= qty)
						return Sat.SATISFIED;
				}
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
		result = 31 * result + getInvolvedNodes().hashCode()
				+ (isContinuous() ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("maxSpareResources(").append("nodes=")
				.append(getInvolvedNodes()).append(", rc=").append(rcId)
				.append(", amount=").append(qty);

		if (isContinuous()) {
			b.append(", continuous");
		} else {
			b.append(", discrete");
		}
		b.append(')');

		return b.toString();
	}

}
