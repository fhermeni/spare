package btrplace.model.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.view.ShareableResource;

/**
 * A constraint to force a set of nodes to reserve a minimum of spare resources
 * for providing immediately to VMs in case of VMs increasing load
 * <p/>
 * When the restriction is discrete, the constraint only ensure that the set of
 * nodes reserve at least a specific number of spare resources at the end of the
 * reconfiguration process. The nodes may have fewer number of spare resources
 * in the reconfiguration process.
 * <p/>
 * When the restriction is continuous, if a VM is going to be relocated in this
 * set of nodes, the nodes must have more spare resources than the resource
 * demand of the VM pluses the reserved number.
 * 
 * @author Tu Huynh Dang
 */

public class MinSpareResources extends SatConstraint {

	/**
	 * Resource indetifier
	 */
	private final String rcId;

	/**
	 * number of reserved resources
	 */
	private final int qty;

	/**
	 * Make a new constraint with a discrete restriction.
	 * 
	 * @param nodes
	 *            the group of nodes
	 * @param rc
	 *            the resource identifier ('ucpu','mem','node')
	 * @param n
	 *            the number of resources to be reserved
	 */
	public MinSpareResources(Set<UUID> nds, String rc, int n) {
		this(nds, rc, n, false);

	}

	public MinSpareResources(Set<UUID> servers, String rc, int n,
			boolean continuous) {
		super(Collections.<UUID> emptySet(), servers, continuous);
		rcId = rc;
		qty = n;
	}

	@Override
	public Sat isSatisfied(Model i) {
		int spare = 0;
		Mapping map = i.getMapping();
		Set<UUID> onNodes = map.getOnlineNodes();

		if (rcId.equals("node")) {
			Set<UUID> vms = map.getRunningVMs(getInvolvedNodes());

			Collection<UUID> nodes = getInvolvedNodes();
			Collection<UUID> idle_nodes = new ArrayList<UUID>(nodes);

			for (UUID vmId : vms) {
				UUID ni = map.getVMLocation(vmId);
				if (idle_nodes.contains(ni))
					idle_nodes.remove(ni);
			}
			spare = idle_nodes.size();

			/*
			 * for (UUID nj : getInvolvedNodes()) { boolean idle = true; for
			 * (UUID vmId : vms) { if (nj.equals(map.getVMLocation(vmId))) {
			 * idle = false; break; } } if (idle) spare++; }
			 */
		}

		else {
			ShareableResource rc = (ShareableResource) i
					.getView(ShareableResource.VIEW_ID_BASE + rcId);

			if (rc == null) {
				return Sat.UNSATISFIED;
			}

			for (UUID nj : getInvolvedNodes()) {
				if (onNodes.contains(nj)) {
					spare += rc.get(nj);
					for (UUID vmId : i.getMapping().getRunningVMs(nj)) {
						spare -= rc.get(vmId);
					}
				}
			}
		}

		if (spare >= qty)
			return Sat.SATISFIED;
		else
			return Sat.UNSATISFIED;

	}

	public String getResource() {
		return rcId;
	}

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
		result = 31 * result + getInvolvedNodes().hashCode()
				+ (isContinuous() ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("minSpareResources(").append("nodes=")
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
