package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MaxSpareNode;
import btrplace.plan.event.BootNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/16/13
 * Time: 3:35 PM
 */
public class MaxSpareNodeChecker extends AllowAllConstraintChecker<MaxSpareNode> {

    private Collection<Node> idle_nodes;


    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */

    public MaxSpareNodeChecker(MaxSpareNode cstr) {
        super(cstr);
        idle_nodes = new HashSet<Node>();
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

    public Set<Node> getIdleNodes(Model mo, Collection<Node> nset) {
        Set<Node> idleNodes = new HashSet<Node>();
        Mapping map = mo.getMapping();
        for (Node n : nset) {
            if (map.getRunningVMs(n).isEmpty() && map.getOnlineNodes().contains(n)) {
                idleNodes.add(n);
            }
        }
        return idleNodes;
    }
}
