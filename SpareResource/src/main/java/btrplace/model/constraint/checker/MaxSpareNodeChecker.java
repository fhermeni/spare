package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.plan.event.BootNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/16/13
 * Time: 3:35 PM
 */
public class MaxSpareNodeChecker extends AllowAllConstraintChecker<MaxSpareNode> {

    private Set<UUID> idle_nodes;


    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */

    public MaxSpareNodeChecker(MaxSpareNode cstr) {
        super(cstr);
        idle_nodes = new HashSet<UUID>();
    }

    @Override
    public boolean startsWith(Model mo) {
        idle_nodes = getIdleNodes(mo, getNodes());
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        if (getConstraint().isContinuous() && getNodes().contains(a.getNode())) {
            //return (idle_nodes.size() < getConstraint().getAmount());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        idle_nodes = getIdleNodes(mo, getNodes());

        if (idle_nodes.size() > super.getConstraint().getAmount()) {
            return false;
        }
        return true;
    }

    public Set<UUID> getIdleNodes(Model mo, Collection<UUID> nset) {
        Set<UUID> idleNodes = new HashSet<UUID>();
        Mapping map = mo.getMapping();
        for (UUID n : nset) {
            if (map.getRunningVMs(n).isEmpty() && map.getOnlineNodes().contains(n)) {
                idleNodes.add(n);
            }
        }
        return idleNodes;
    }
}
