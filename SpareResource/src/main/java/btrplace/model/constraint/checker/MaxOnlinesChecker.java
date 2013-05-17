package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MaxOnlines;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/16/13
 * Time: 2:46 PM
 */
public class MaxOnlinesChecker extends AllowAllConstraintChecker<MaxOnlines> {
    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */

    private int current_online;

    public MaxOnlinesChecker(MaxOnlines cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        Mapping map = mo.getMapping();
        Set<UUID> onlineNodes = map.getOnlineNodes();
        // Keep the below line to not modify the RP variable
        Set<UUID> onlineNodesCopy = new HashSet<UUID>(onlineNodes);
        onlineNodesCopy.retainAll(getNodes());
        current_online = onlineNodesCopy.size();
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        if (getConstraint().isContinuous() && getNodes().contains(a.getNode()))
            return (current_online < getConstraint().getAmount());

        return true;
    }

    @Override
    public void end(BootNode a) {
        if (getNodes().contains(a.getNode())) current_online++;
    }

    @Override
    public void end(ShutdownNode a) {
        current_online--;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping map = mo.getMapping();
        Set<UUID> onlineNodes = map.getOnlineNodes();
        // Keep the below line to not modify the RP variable
        Set<UUID> onlineNodesCopy = new HashSet<UUID>(onlineNodes);
        onlineNodesCopy.retainAll(getNodes());
        current_online = onlineNodesCopy.size();
        return (current_online <= getConstraint().getAmount());
    }
}
