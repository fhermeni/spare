package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.MinSpareResources;
import btrplace.model.view.ShareableResource;

import java.util.HashSet;
import java.util.Set;

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
        Set<Node> onnodes = map.getOnlineNodes();
        Set<Node> nodes = new HashSet<Node>(onnodes);
        nodes.retainAll(super.getNodes());

        ShareableResource rc = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE +
                super.getConstraint().getResource());

        if (rc == null) {
            return false;
        }

        for (Node nj : nodes) {
            spare += rc.getCapacity(nj);
        }

        for (Node nj : nodes) {
            for (VM vmId : mo.getMapping().getRunningVMs(nj)) {
                spare -= rc.getConsumption(vmId);
                if (spare < super.getConstraint().getAmount())
                    return false;
            }
        }

        return true;

    }


}
