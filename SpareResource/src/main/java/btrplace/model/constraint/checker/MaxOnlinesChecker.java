package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MaxOnlines;

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
    public MaxOnlinesChecker(MaxOnlines cstr) {
        super(cstr);
    }

    @Override
    public boolean endsWith(Model mo) {

        Mapping map = mo.getMapping();

        Set<UUID> onnodes = map.getOnlineNodes();
        Set<UUID> nodes = new HashSet<UUID>(onnodes);
        nodes.retainAll(super.getNodes());

        return (nodes.size() <= super.getConstraint().getAmount());
    }

    @Override
    public boolean startsWith(Model mo) {

        Mapping map = mo.getMapping();

        Set<UUID> onnodes = map.getOnlineNodes();
        Set<UUID> nodes = new HashSet<UUID>(onnodes);
        nodes.retainAll(super.getNodes());

        /*if (nodes.size() > super.getConstraint().getAmount())
            return false;*/

        return true;

    }
}
