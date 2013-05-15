/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSimpleTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>RndClarkeWright</code> is a randomized implementation of the Clarke and
 * Wright heuristic to generate TRSP tours
 * <p>
 * Creation date: Oct 27, 2011 - 10:23:13 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RndClarkeWright extends TRSPRndConstructiveHeuristic {

	private ArrayList<?>[] mArcs;
	private ListIterator<CWArc> mArcIterator;
	private LinkedList<?>[] mCurrentTours;

	/**
	 * Instantiates a new rnd clarke wright.
	 * 
	 * @param instance
	 *            the instance
	 * @param parameters
	 *            the parameters
	 * @param constraintHandler
	 *            the constraint handler
	 * @param costDelegate
	 *            the cost delegate
	 * @param kMax
	 *            the k max
	 */
	public RndClarkeWright(TRSPInstance instance,
			TRSPGlobalParameters parameters,
			TourConstraintHandler constraintHandler,
			TRSPCostDelegate costDelegate, int kMax) {
		super(instance, parameters, constraintHandler, costDelegate, kMax);
	}

	@Override
	protected void initialize(List<ObservableNode> requests) {
		super.initialize(requests);
		mArcs = new ArrayList<?>[getInstance().getFleet().size()];
		generateArcs(requests);
	}

	/**
	 * Generate the saving arcs for each technician
	 * 
	 * @param requests
	 */
	private void generateArcs(List<ObservableNode> requests) {
		int tid = -1;
		for (Technician t : getInstance().getFleet()) {
			tid = t.getID();
			mArcs[tid] = new ArrayList<CWArc>(getInstance().getRequestCount()
					* getInstance().getRequestCount());

			for (ObservableNode i : requests) {
				for (ObservableNode j : requests) {
					if (i.getId() != j.getId()
							&& (!isCheckTWFeas() || getInstance()
									.isArcTWFeasible(i.getId(), j.getId()))) {
						getArcs(t.getID()).add(
								new CWArc(i.getId(), j.getId(), -getInstance()
										.getCostDelegate().getInsertionCost(
												t.getHome().getID(), i.getId(),
												j.getId(), t)));
					}
				}
			}

			Collections.sort(getArcs(t.getID()));

			// Speed up when the fleet is homogeneous
			if (getInstance().getFleet().isHomogeneous())
				break;
		}

		// Copy the reference for all technicians
		if (getInstance().getFleet().isHomogeneous()) {
			for (Technician t2 : getInstance().getFleet()) {
				mArcs[t2.getID()] = mArcs[tid];
			}
		}
	}

	/**
	 * Returns the list of the saving arcs for a given technician. This is a
	 * precalculated list that should not be modified.
	 * 
	 * @param techId
	 * @return the list of the saving arcs for a given technician
	 */
	@SuppressWarnings("unchecked")
	ArrayList<CWArc> getArcs(int techId) {
		return (ArrayList<CWArc>) mArcs[techId];
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ITRSPTour> generateGiantTour(Technician tech) {
		// A copy of the saving arcs
		LinkedList<CWArc> mergeArcs = new LinkedList<RndClarkeWright.CWArc>(
				getArcs(tech.getID()));

		// Reset the observable nodes (the served flag is not used in this
		// implem)
		// resetNodes(tech.getID());

		mCurrentTours = new LinkedList<?>[getInstance().getMaxId()];

		// Add each request to a tour
		for (ObservableNode req : getCompatibleRequests(tech.getID())) {
			LinkedList<Integer> tour = new LinkedList<Integer>();
			tour.add(req.getId());
			mCurrentTours[req.getId()] = tour;
		}

		mArcIterator = mergeArcs.listIterator();
		while (!mergeArcs.isEmpty()) {
			int k = nextIdx(mergeArcs.size());
			// Select the k-th merge arc
			CWArc arc = popKthArc(k);

			if (arc != null) {
				executeMerge(arc);
			}
		}

		LinkedList<ITRSPTour> tours = new LinkedList<ITRSPTour>();
		for (LinkedList<?> tour : mCurrentTours) {
			if (tour != null) {
				LinkedList<Integer> ltour = (LinkedList<Integer>) tour;
				mCurrentTours[ltour.getFirst()] = null;
				mCurrentTours[ltour.getLast()] = null;
				ltour.addFirst(tech.getHome().getID());
				tours.add(new TRSPSimpleTour(tech.getID(), getInstance(), ltour));
			}
		}

		return tours;
	}

	/**
	 * Returns the k-th feasible arc
	 * 
	 * @param k
	 * @return the k-th feasible arc
	 */
	protected final CWArc popKthArc(int k) {
		CWArc kthArc = null;
		CWArc tmpArc = null;
		while (mArcIterator.nextIndex() < k && mArcIterator.hasNext()) {
			tmpArc = mArcIterator.next();
			if (!isArcFeasible(tmpArc)) // We remove infeasible arcs
				mArcIterator.remove();
			else
				kthArc = tmpArc;
		}
		while (mArcIterator.nextIndex() > k && mArcIterator.hasPrevious()) {
			tmpArc = mArcIterator.previous();
			if (!isArcFeasible(tmpArc)) // We remove infeasible arcs
				mArcIterator.remove();
			else
				kthArc = tmpArc;
		}

		tmpArc = null;
		if (mArcIterator.hasNext()) {
			tmpArc = mArcIterator.next();
			mArcIterator.remove();
			boolean feasible = isArcFeasible(tmpArc);

			while (!feasible && mArcIterator.hasNext()) {
				tmpArc = mArcIterator.next();
				mArcIterator.remove();
				feasible = isArcFeasible(tmpArc);
			}
			if (feasible) {
				kthArc = tmpArc;
			}
		}
		return kthArc;
	}

	/**
	 * Check if an arc represents a feasible merge
	 * 
	 * @param arc
	 * @return
	 */
	private boolean isArcFeasible(CWArc arc) {
		return isExterior(arc.getHead())
				&& isExterior(arc.getTail()) //
				&& isLast(arc.getTail()) && isFirst(arc.getHead())
				&& mCurrentTours[arc.getHead()] != mCurrentTours[arc.getTail()];
	}

	/**
	 * Execute the merge represented by {@code  arc}
	 * 
	 * @param arc
	 */
	private void executeMerge(CWArc arc) {
		LinkedList<Integer> tailTour = getCurrentTour(arc.getTail());
		LinkedList<Integer> headTour = getCurrentTour(arc.getHead());

		// Mark both head and tail as interior : set their current tour to null
		mCurrentTours[arc.getHead()] = null;
		if (tailTour.size() > 1)
			mCurrentTours[arc.getTail()] = null;

		// Append the head tour to the tail tour
		tailTour.addAll(headTour);

		// Update the current tour of the head last node
		mCurrentTours[headTour.getLast()] = tailTour;
	}

	/**
	 * Returns the current tour of {@code  node}
	 * 
	 * @param node
	 * @return the current tour of {@code  node}
	 */
	@SuppressWarnings("unchecked")
	protected LinkedList<Integer> getCurrentTour(int node) {
		return (LinkedList<Integer>) mCurrentTours[node];
	}

	private boolean isExterior(int node) {
		return mCurrentTours[node] != null;
	}

	/**
	 * Returns {@code true} iif {@code  node} is the first node of its tour
	 * 
	 * @param node
	 * @return {@code true} iif {@code  node} is the first node of its tour
	 */
	private boolean isFirst(int node) {
		return getCurrentTour(node).getFirst() == node;
	}

	/**
	 * Returns {@code true} iif {@code  node} is the last node of its tour
	 * 
	 * @param node
	 * @return {@code true} iif {@code  node} is the last node of its tour
	 */
	private boolean isLast(int node) {
		return getCurrentTour(node).getLast() == node;
	}

	@Override
	protected ITRSPTour generateFeasibleTour(Technician tech) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public String toString() {
		return "RCW";
	}

	/**
	 * The Class <code>CWArc</code> wraps a saving arc for the CW algorithm
	 * <p>
	 * Creation date: Oct 27, 2011 - 10:48:26 AM.
	 * 
	 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de
	 *         Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
	 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
	 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp"
	 *         >SLP</a>
	 * @version 1.0
	 */
	protected static class CWArc implements Comparable<CWArc> {

		/** The Head. */
		private final Integer mHead;

		/** The Tail. */
		private final Integer mTail;

		/** The Savings. */
		private final double mSavings;

		/**
		 * Creates a new <code>CWArc</code>.
		 * 
		 * @param head
		 *            the head
		 * @param tail
		 *            the tail
		 * @param savings
		 *            the savings
		 */
		private CWArc(Integer head, Integer tail, double savings) {
			mHead = head;
			mTail = tail;
			mSavings = savings;
		}

		/**
		 * Gets the head.
		 * 
		 * @return the head
		 */
		public Integer getHead() {
			return mHead;
		}

		/**
		 * Gets the tail.
		 * 
		 * @return the tail
		 */
		public Integer getTail() {
			return mTail;
		}

		/**
		 * Gets the savings.
		 * 
		 * @return the savings
		 */
		public double getSavings() {
			return mSavings;
		}

		@Override
		public int compareTo(CWArc o) {
			return Double.compare(this.getSavings(), o.getSavings());
		}

		@Override
		public String toString() {
			return String.format("(%s,%s,%.3f)", getHead(), getTail(),
					getSavings());
		}
	}
}
