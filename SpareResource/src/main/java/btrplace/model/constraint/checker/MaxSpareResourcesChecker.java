package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.MaxSpareResources;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.MigrateVM;

import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/17/13
 * Time: 10:07 AM
 */
public class MaxSpareResourcesChecker extends AllowAllConstraintChecker<MaxSpareResources> {

    private int spare_amount;
    private Model origin_model;

    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */

    public MaxSpareResourcesChecker(MaxSpareResources cstr) {
        super(cstr);
    }

    @Override
    public boolean startsWith(Model mo) {
        spare_amount = spare(mo);
        origin_model = mo;
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        ShareableResource rc = (ShareableResource) origin_model.getView(getConstraint().getResource());
        if (getConstraint().isContinuous() && getNodes().contains(a.getSourceNode())) {
            return (spare_amount <= (spare_amount + rc.getConsumption(a.getVM())));
        }
        return true;
    }

    @Override
    public void end(MigrateVM a) {
        ShareableResource rc = (ShareableResource) origin_model.getView(getConstraint().getResource());
        if (getConstraint().isContinuous() && getNodes().contains(a.getSourceNode())) {
            spare_amount += rc.getConsumption(a.getVM());
        }
        if (getConstraint().isContinuous() && getNodes().contains(a.getDestinationNode())) {
            spare_amount -= rc.getConsumption(a.getVM());
        }
    }

    @Override
    public boolean endsWith(Model mo) {
        int spare = 0;
        Mapping map = mo.getMapping();
        Set<Node> onnodes = map.getOnlineNodes();
        Set<Node> nodes = new HashSet<Node>(onnodes);
        nodes.retainAll(getNodes());
        ShareableResource rc = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE +
                getConstraint().getResource());
        if (rc == null) {
            return false;
        }
        for (Node nj : nodes) {
            spare += rc.getCapacity(nj);
        }
        for (Node nj : nodes) {
            for (VM vmId : mo.getMapping().getRunningVMs(nj)) {
                spare -= rc.getConsumption(vmId);
                if (spare <= getConstraint().getAmount())
                    return true;
            }
        }
        return false;
    }

    public int spare(Model mo) {
        int spare = 0;
        Mapping map = mo.getMapping();
        Set<Node> onnodes = map.getOnlineNodes();
        Set<Node> nodes = new HashSet<Node>(onnodes);
        nodes.retainAll(getNodes());

        ShareableResource rc = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE +
                getConstraint().getResource());
        for (Node nj : nodes) {
            spare += rc.getCapacity(nj);
        }
        for (Node nj : nodes) {
            for (VM vmId : mo.getMapping().getRunningVMs(nj)) {
                spare -= rc.getConsumption(vmId);
            }
        }
        return spare;
    }
}
