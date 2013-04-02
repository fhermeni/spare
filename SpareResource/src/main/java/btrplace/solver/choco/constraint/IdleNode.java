package btrplace.solver.choco.constraint;

import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModelUtils;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.VMActionModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.MaxOfAList;
import choco.cp.solver.constraints.integer.MinOfAList;
import choco.cp.solver.constraints.integer.channeling.BooleanChanneling;
import choco.cp.solver.constraints.reified.IfThenElse;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
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
        idle_start = solver.createBoundIntVar("idleS(" + n.getLeastSignificantBits() + ")", 0, Integer.MAX_VALUE);
        idle_end = solver.createBoundIntVar("idleE(" + n.getLeastSignificantBits() + ")", 0, Integer.MAX_VALUE);
        VMActionModel[] vmActions = rp.getVMActions();

        Set<UUID> vms = rp.getSourceModel().getMapping().getRunningVMs(n);
        List<IntDomainVar> cStarts = new ArrayList<IntDomainVar>();
        List<IntDomainVar> dEnds = new ArrayList<IntDomainVar>();
        cStarts.add(0, idle_start);
        if (!vms.isEmpty()) {
            for (UUID vm : vms) {
                VMActionModel vma = rp.getVMAction(vm);
                Slice c = vma.getCSlice();
                cStarts.add(c.getEnd());
            }
        } else {
            cStarts.add(solver.createIntegerConstant("NoCSlice", 0));
        }
        IntDomainVar[] cArray = cStarts.toArray(new IntDomainVar[cStarts.size()]);

        // Idle start is equals the maximum end of all cSlices
        solver.post(new MaxOfAList(solver.getEnvironment(), cArray));


        List<Slice> dslices = ActionModelUtils.getDSlices(vmActions);
        dEnds.add(0, idle_end);
        for (Slice d : dslices) {
            // Check if dSlice locate on this node
            IntDomainVar eqH = solver.createBooleanVar("eqH(" + d.getSubject().getLeastSignificantBits() + "," + n.getLeastSignificantBits() + ")");
            IntDomainVar sD = solver.createBoundIntVar("dSt(" + d.getSubject().getLeastSignificantBits() + ")", 0, 3600);
            solver.post(new BooleanChanneling(eqH, d.getHoster(), nodeIdx));

            // If dslice lies on node n, then temporary variable sD equals d.Start. Otherwise, it is the end of RP
            solver.post(new IfThenElse(eqH, (AbstractIntSConstraint) solver.eq(sD, d.getStart()),
                    (AbstractIntSConstraint) solver.eq(sD, rp.getEnd())));

            dEnds.add(sD);
        }

        // Idle end is min of start time of all dSlices
        solver.post(new MinOfAList(solver.getEnvironment(), dEnds.toArray(new IntDomainVar[dEnds.size()])));
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
