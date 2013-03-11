package btrplace.solver.choco.constraint;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.MaxSpareResources;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.VMActionModel;
import btrplace.solver.choco.view.CShareableResource;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class CMaxSpareResources implements ChocoSatConstraint {

	private final MaxSpareResources cstr;

	/**
	 * Make a new constraint.
	 * 
	 * @param msr
	 *            is maxSpareResources constraint to rely on
	 */
	public CMaxSpareResources(MaxSpareResources msr) {
		super();
		this.cstr = msr;
	}

	@Override
	public Set<UUID> getMisPlacedVMs(Model m) {
		return m.getMapping().getRunningVMs();
	}

	@Override
	public boolean inject(ReconfigurationProblem rp) throws SolverException {

		CShareableResource rcm = (CShareableResource) rp
				.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
		if (rcm == null) {
			throw new SolverException(rp.getSourceModel(),
					"No resource associated to identifier '"
							+ cstr.getResource() + "'");
		}

		if (cstr.isContinuous()) {
			// The constraint must be already satisfied
			if (!cstr.isSatisfied(rp.getSourceModel()).equals(
					SatConstraint.Sat.SATISFIED)) {
				rp.getLogger()
						.error("The constraint '{}' must be already satisfied to provide a continuous restriction",
								cstr);
				return false;
			} else {
				int[] alias = new int[cstr.getInvolvedNodes().size()];
				int i = 0;
				int capas = 0;
				for (UUID n : cstr.getInvolvedNodes()) {
					alias[i++] = rp.getNode(n);
					capas += rcm.getSourceResource().get(n);
				}
				
				TIntArrayList cUse = new TIntArrayList();
				List<IntDomainVar> dUse = new ArrayList<IntDomainVar>();

				for (UUID vmId : rp.getVMs()) {
					VMActionModel a = rp.getVMAction(vmId);
					Slice c = a.getCSlice();
					Slice d = a.getDSlice();
					if (c != null) {
						cUse.add(rcm.getSourceResource().get(vmId));
					}
					if (d != null) {
						dUse.add(rcm.getVMsAllocation(rp.getVM(vmId)));
					}
				}
				rp.getAliasedCumulativesBuilder().add(capas-cstr.getAmount(),
						cUse.toArray(),
						dUse.toArray(new IntDomainVar[dUse.size()]), alias);
				
				
			}
		}
		List<IntDomainVar> vs = new ArrayList<IntDomainVar>();
		int tot = 0;
		
		for (UUID u : cstr.getInvolvedNodes()) {
			vs.add(rcm.getVirtualUsage()[rp.getNode(u)]);
			tot += rcm.getSourceResource().get(u);
		}

		CPSolver s = rp.getSolver();
		
		IntExp sumcon = CPSolver.sum(vs.toArray(new IntDomainVar[vs.size()]));
		
		IntExp spare = CPSolver.minus(tot, sumcon);
		
		
		s.post(s.leq(spare, cstr.getAmount()));
		
		return true;
	}
	
    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
 
        @Override
		public Class<? extends SatConstraint> getKey() {
            return MaxSpareResources.class;
        }


        @Override
		public CMaxSpareResources build(SatConstraint cstr) {
            return new CMaxSpareResources((MaxSpareResources) cstr);
        }
    }
}
