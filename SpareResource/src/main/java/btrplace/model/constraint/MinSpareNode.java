package btrplace.model.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;

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
	 *            the number of resources to be reserved
	 */
	public MinSpareNode(Set<UUID> nds, int n) {
		this(nds, n, false);

	}

	/**
	 * @param servers
	 *            the group of nodes
	 * @param n
	 *            the number of resources to be reserved
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
	public int getQty() {
		return qty;
	}

	@Override
	public Sat isSatisfied(Model i) {

		Mapping map = i.getMapping();

		Set<UUID> vms = map.getRunningVMs(getInvolvedNodes());

		Collection<UUID> nodes = getInvolvedNodes();
		Collection<UUID> idle_nodes = new HashSet<UUID>(nodes);

		for (UUID vmId : vms) {
			UUID ni = map.getVMLocation(vmId);
			if (idle_nodes.contains(ni))
				idle_nodes.remove(ni);
			if (idle_nodes.size() < qty)
				return Sat.UNSATISFIED;
		}
		return Sat.SATISFIED;

	}

}
