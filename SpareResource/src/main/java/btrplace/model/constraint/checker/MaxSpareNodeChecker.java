package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MaxSpareNode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/16/13
 * Time: 3:35 PM
 */
public class MaxSpareNodeChecker extends AllowAllConstraintChecker<MaxSpareNode> {
    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */
    public MaxSpareNodeChecker(MaxSpareNode cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        return super.startsWith(mo);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean endsWith(Model mo) {

        Mapping map = mo.getMapping();
        Set<UUID> onnodes = map.getOnlineNodes();
        Set<UUID> nodes = new HashSet<UUID>(onnodes);
        nodes.retainAll(super.getNodes());
        Set<UUID> idle_nodes = new HashSet<UUID>();

        for (UUID n : nodes) {
            if (map.getRunningVMs(n).isEmpty()) {
                idle_nodes.add(n);
            }
            if (idle_nodes.size() > super.getConstraint().getAmount()) {
                return false;
            }
        }
        return true;
    }
}
