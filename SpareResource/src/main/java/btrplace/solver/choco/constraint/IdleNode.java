package btrplace.solver.choco.constraint;

import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModelUtils;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.VMActionModel;
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.bool.BooleanFactory;
import choco.cp.solver.constraints.integer.channeling.BooleanChanneling;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 3/27/13
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class IdleNode {

    // The start moment of node being idle
    private IntDomainVar idle_start;
    // The end moment of node being idle
    private IntDomainVar idle_end;

    public IdleNode(ReconfigurationProblem rp, UUID n) throws SolverException {

        int nodeIdx = rp.getNode(n);
        CPSolver solver = rp.getSolver();
        idle_start = solver.createBoundIntVar("idleS", 0, Integer.MAX_VALUE);
        idle_end = solver.createBoundIntVar("idleE", 0, Integer.MAX_VALUE);
        VMActionModel[] vmActions = rp.getVMActions();

        Set<UUID> vms = rp.getSourceModel().getMapping().getRunningVMs(n);

        if (!vms.isEmpty()) {
            for (UUID vm : vms) {
                VMActionModel vma = rp.getVMAction(vm);
                Slice c = vma.getCSlice();
                ChocoUtils.postImplies(solver, solver.lt(idle_start, c.getEnd()), solver.eq(idle_start, c.getEnd()));
            }
        }

        List<Slice> dslices = ActionModelUtils.getDSlices(vmActions);
        for (Slice d : dslices) {
            // Check if dSlice locate on this node
            IntDomainVar eqH = solver.createBooleanVar("eqH(" + d.getSubject() + ")");
            solver.post(new BooleanChanneling(eqH, d.getHoster(), nodeIdx));

            // Check if the idle_time is greater than the starting of dSlice on this host
            IntDomainVar gt = solver.createBooleanVar("gtS(" + d.getSubject() + ")");
            ReifiedFactory.builder(gt, solver.gt(idle_end, d.getStart()), solver);
            AbstractSConstraint<IntDomainVar> and = BooleanFactory.and(eqH, gt);

            // If both conditions above are true, then set idle_end to the Min dslice Start
            ChocoUtils.postImplies(solver, and, solver.eq(idle_end, d.getStart()));
            System.out.println(solver.pretty());

        }

        solver.post(solver.geq(idle_end, idle_start));
    }

    public IntDomainVar getIdle_end() {
        return idle_end;
    }

    public void setIdle_end(IntDomainVar idle_end) {
        this.idle_end = idle_end;
    }

    public IntDomainVar getIdle_start() {
        return idle_start;
    }

    public void setIdle_start(IntDomainVar idle_start) {
        this.idle_start = idle_start;
    }
}
