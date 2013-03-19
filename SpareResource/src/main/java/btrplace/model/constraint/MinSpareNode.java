package btrplace.model.constraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;

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

	/**
	 * Make a new constraint with a discrete restriction.
	 * 
	 * @param nodes
	 *            the group of nodes
	 * @param n
	 *            the number of nodes to be reserved
	 */
	public MinSpareNode(Set<UUID> nds, int n) {
		this(nds, n, false);

	}

	/**
	 * Make a new constraint stating the restriction explicitly
	 * 
	 * @param servers
	 *            the group of nodes
	 * @param n
	 *            the number of nodes to be reserved
	 * @param continuous
	 *            {@code true} for a continuous restriction.
	 */
	public MinSpareNode(Set<UUID> servers, int n, boolean continuous) {
		super(Collections.<UUID> emptySet(), servers, continuous);
		qty = n;
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
	public Sat isSatisfied(Model i) {

		Mapping map = i.getMapping();

		Set<UUID> onnodes = map.getOnlineNodes();
		Set<UUID> nodes = new HashSet<UUID>(onnodes);
		nodes.retainAll(getInvolvedNodes());
		Set<UUID> idle_nodes = new HashSet<UUID>(nodes);

		for (UUID n : nodes) {
			if (!map.getRunningVMs(n).isEmpty()) {
				idle_nodes.remove(n);
			}
			if (idle_nodes.size() < qty)
				return Sat.UNSATISFIED;
		}
		return Sat.SATISFIED;

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

}
