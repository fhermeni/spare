package btrplace.model.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;

import java.util.*;

/**
 * A constraint to force a set of nodes to reserve a maximum number of spare
 * nodes for efficient usage of nodes
 * <p/>
 * In discrete restriction mode, the constraint only ensure that the set of
 * nodes reserve at least n number of spare nodes at the end of the
 * reconfiguration process. The nodes may have fewer than n number of spare
 * nodes in the reconfiguration process.
 * <p/>
 * In continuous restriction mode, there are always at most n number of spare
 * nodes in the reconfiguration process.
 *
 * @author Tu Huynh Dang
 */

public class MaxSpareNode extends SatConstraint {

    /**
     * number of reserved nodes
     */
    private final int qty;

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param nodes the group of nodes
     * @param n     the number of nodes to be reserved
     */
    public MaxSpareNode(Set<UUID> nodes, int n) {
        this(nodes, n, false);

    }

    /**
     * Make a new constraint stating the restriction explicitly
     *
     * @param servers    the group of nodes
     * @param n          the number of nodes to be reserved
     * @param continuous {@code true} for a continuous restriction.
     */
    public MaxSpareNode(Set<UUID> servers, int n, boolean continuous) {
        super(Collections.<UUID>emptySet(), servers, continuous);
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
        Set<UUID> idle_nodes = new HashSet<UUID>();

        for (UUID n : nodes) {
            if (map.getRunningVMs(n).isEmpty()) {
                idle_nodes.add(n);
            }
            if (idle_nodes.size() > qty) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;

    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan p) {
        Model mo = p.getOrigin().clone();


        //---------- find concurrent actions ------------
        HashSet<Integer> skipIdx = new HashSet<Integer>();
        Map<Integer, ArrayList<Integer>> concurrent_actions = new HashMap<Integer, ArrayList<Integer>>();
        int idx = 0;
        Action[] actions = new Action[p.getSize()];
        for (Action ac : p) {
            actions[idx++] = ac;
        }

        for (int i = 0; i < idx - 1; i++) {
            if (skipIdx.contains(i)) continue;

            ArrayList<Integer> alist = new ArrayList<Integer>();
            for (int j = i + 1; j < idx; j++) {
                if (actions[i].getStart() == actions[j].getStart()) {
                    alist.add(j);
                }
            }
            if (!alist.isEmpty()) {
                concurrent_actions.put(i, alist);
                skipIdx.addAll(alist);
            }
        }
        //---------- find concurrent actions ------------


        for (int i = 0; i < idx; i++) {
            if (skipIdx.contains(i)) continue;

            Action a = actions[i];
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            // execute actions which are concurrent with action a;
            if (concurrent_actions.containsKey(i)) {
                ArrayList<Integer> calist = concurrent_actions.get(i);

                for (int k : calist) {
                    Action b = actions[k];
                    if (!b.apply(mo)) {
                        return Sat.UNSATISFIED;
                    }
                }

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
        b.append("maxSpareNode(").append("nodes=").append(getInvolvedNodes()).append(", amount=")
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
