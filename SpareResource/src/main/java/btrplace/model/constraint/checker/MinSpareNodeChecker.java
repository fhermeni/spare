package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MinSpareNode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/17/13
 * Time: 9:58 AM
 */
public class MinSpareNodeChecker extends AllowAllConstraintChecker<MinSpareNode> {
    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */
    public MinSpareNodeChecker(MinSpareNode cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        return super.startsWith(mo);
    }

    @Override
    public boolean endsWith(Model mo) {

        Mapping map = mo.getMapping();

        Set<UUID> onnodes = map.getOnlineNodes();
        Set<UUID> nodes = new HashSet<UUID>(onnodes);
        nodes.retainAll(super.getNodes());
        Set<UUID> idle_nodes = new HashSet<UUID>(nodes);

        for (UUID n : nodes) {
            if (!map.getRunningVMs(n).isEmpty()) {
                idle_nodes.remove(n);
            }
            if (idle_nodes.size() < super.getConstraint().getAmount())
                return false;
        }
        return true;
    }
}
