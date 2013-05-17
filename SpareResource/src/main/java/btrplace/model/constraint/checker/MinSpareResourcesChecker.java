package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.MinSpareResources;
import btrplace.model.view.ShareableResource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/17/13
 * Time: 10:11 AM
 */
public class MinSpareResourcesChecker extends AllowAllConstraintChecker<MinSpareResources> {
    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */
    public MinSpareResourcesChecker(MinSpareResources cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        return super.startsWith(mo);
    }

    @Override
    public boolean endsWith(Model mo) {

        int spare = 0;
        Mapping map = mo.getMapping();
        Set<UUID> onnodes = map.getOnlineNodes();
        Set<UUID> nodes = new HashSet<UUID>(onnodes);
        nodes.retainAll(super.getNodes());

        ShareableResource rc = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE +
                super.getConstraint().getResource());

        if (rc == null) {
            return false;
        }

        for (UUID nj : nodes) {
            spare += rc.get(nj);
        }

        for (UUID nj : nodes) {
            for (UUID vmId : mo.getMapping().getRunningVMs(nj)) {
                spare -= rc.get(vmId);
                if (spare < super.getConstraint().getAmount())
                    return false;
            }
        }

        return true;

    }


}
