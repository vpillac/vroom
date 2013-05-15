/**
 * 
 */
package vroom.common.heuristics.vrp.constraints;

import java.util.Iterator;
import java.util.List;

import vroom.common.heuristics.cw.kernel.RouteMergingMove;
import vroom.common.heuristics.vrp.OrOptMove;
import vroom.common.heuristics.vrp.PairMove;
import vroom.common.heuristics.vrp.RelocateMove;
import vroom.common.heuristics.vrp.RelocateMove.RelocateAtomicMove;
import vroom.common.heuristics.vrp.StringExchangeMove;
import vroom.common.heuristics.vrp.SwapMove;
import vroom.common.heuristics.vrp.TwoOptMove;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;

/**
 * <code>FixedNodesConstraint</code> is a constraint that checks that a move
 * will not change the position of a fixed node.
 * <p>
 * Supported moves:
 * <ul>
 * <li>{@link TwoOptMove}</li>
 * <li>{@link SwapMove}</li>
 * </ul>
 * <p>
 * Creation date: Jun 22, 2010 - 9:44:05 AM
 * 
 * @param <S>
 *            the generic type
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class FixedNodesConstraint<S extends IVRPSolution<?>> implements
		IConstraint<S> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.heuristics.IConstraint#checkMove(java.lang.Object,
	 * vroom.common.heuristics.Move)
	 */
	@Override
	public boolean isFeasible(S solution, IMove move) {
		if (move instanceof PairMove<?>) {
			PairMove<?> m = (PairMove<?>) move;

			IRoute<?> rI = solution.getRoute(m.getRouteI());
			IRoute<?> rJ = solution.getRoute(m.getRouteJ());
			int i = m.getI();
			int j = m.getJ();

			if (i < 0 || j < 0 || i > rI.length() - 1 || j > rJ.length() - 1) {
				return false;
			}
			// -----------------------------------
			// 2-opt
			// -----------------------------------
			if (move instanceof TwoOptMove) {
				return checkTwoOpt(solution, (TwoOptMove) move);
				// -----------------------------------
				// Swap
				// -----------------------------------
			} else if (move instanceof SwapMove) {
				return checkSwap(solution, (SwapMove) move);
				// -----------------------------------
				// Or-opt
				// -----------------------------------
			} else if (move instanceof OrOptMove<?>) {
				return checkOrOpt(solution, (OrOptMove<?>) move);

			} else {
				throw new UnsupportedOperationException("Unsupported move: "
						+ move.getClass().getSimpleName());
				// return true;
			}
		} else if (move instanceof StringExchangeMove<?>) {
			return checkStrExchange(solution, (StringExchangeMove<?>) move);
		} else if (move instanceof RouteMergingMove) {
			return true;
		} else if (move instanceof RelocateMove) {
			for (RelocateAtomicMove reloc : ((RelocateMove) move)
					.getAtomicMoves()) {
				if (!checkRelocAtomicMove(reloc)) {
					return false;
				}
			}
			return true;
		} else if (move instanceof RelocateAtomicMove) {
			return checkRelocAtomicMove((RelocateAtomicMove) move);
		} else {
			throw new UnsupportedOperationException("Unsupported move: "
					+ move.getClass().getSimpleName());
			// return true;
		}
	}

	/**
	 * Check two opt move.
	 * 
	 * @param solution
	 *            the solution
	 * @param move
	 *            the
	 * @return <code>true</code> if move is feasible
	 */
	public boolean checkTwoOpt(S solution, TwoOptMove move) {
		IRoute<?> rI = solution.getRoute(move.getRouteI());
		IRoute<?> rJ = solution.getRoute(move.getRouteJ());
		int i = move.getI();
		int j = move.getJ();

		return i < rI.length() - 1
				&& j < rJ.length() - 1
				&& !(rI.getNodeAt(i).isFixed() && rI.getNodeAt(i + 1).isFixed())
				&& !(rJ.getNodeAt(j).isFixed() && rJ.getNodeAt(j + 1).isFixed());
	}

	/**
	 * Check swap move.
	 * 
	 * @param solution
	 *            the solution
	 * @param move
	 *            the
	 * @return <code>true</code> if move is feasible
	 */
	public boolean checkSwap(S solution, SwapMove move) {
		IRoute<?> rI = solution.getRoute(move.getRouteI());
		IRoute<?> rJ = solution.getRoute(move.getRouteJ());
		int i = move.getI();
		int j = move.getJ();

		return !rI.getNodeAt(i).isFixed() && !rJ.getNodeAt(j).isFixed();
	}

	/**
	 * Check or opt move.
	 * 
	 * @param solution
	 *            the solution
	 * @param move
	 *            the mve
	 * @return <code>true</code> if move is feasible
	 */
	public boolean checkOrOpt(S solution, OrOptMove<?> move) {
		IRoute<?> rI = solution.getRoute(move.getRouteI());
		IRoute<?> insRoute = solution.getRoute(move.getInsertionRoute());

		int i = move.getI();
		int j = move.getJ();
		int ins = move.getInsertionIndex();

		if (ins < 0 || ins > insRoute.length() // out of bounds
				// append but last node is fixed
				|| (ins == insRoute.length() && insRoute.getLastNode()
						.isFixed())
				// insertion node is fixed
				|| (ins < insRoute.length() && insRoute.getNodeAt(ins)
						.isFixed())
				// wrong node order
				|| j < i || (insRoute == rI && i <= ins && ins <= j)) {
			return false;
		}

		List<?> segment = rI.subroute(i, j);

		Iterator<INodeVisit> it = Utilities.castIterator(segment.iterator());
		while (it.hasNext()) {
			INodeVisit node = it.next();
			if (node.isFixed()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check str exchange move.
	 * 
	 * @param solution
	 *            the solution
	 * @param mve
	 *            the mve
	 * @return <code>true</code> if move is feasible
	 */
	public boolean checkStrExchange(S solution, StringExchangeMove<?> mve) {
		IRoute<?> r1 = solution.getRoute(mve.getFirstRoute());
		IRoute<?> r2 = solution.getRoute(mve.getSecondRoute());

		int i = mve.getNodeI();
		int j = mve.getNodeJ();
		int k = mve.getNodeK();
		int l = mve.getNodeL();

		if (i < 0 || i > r1.length() - 1 || j < 0 || j > r1.length() - 1
				|| j < i || k < 0 || k > r2.length() - 1 || l < 0
				|| l > r2.length() - 1 || l < k) {
			return false;
		}

		List<?> segment = r1.subroute(i, j);
		Iterator<INodeVisit> it = Utilities.castIterator(segment.iterator());
		while (it.hasNext()) {
			INodeVisit node = it.next();
			if (node.isFixed()) {
				return false;
			}
		}
		segment = r2.subroute(k, l);
		it = Utilities.castIterator(segment.iterator());
		while (it.hasNext()) {
			INodeVisit node = it.next();
			if (node.isFixed()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check reloc atomic move.
	 * 
	 * @param reloc
	 *            the reloc
	 * @return <code>true</code> if move is feasible
	 */
	public boolean checkRelocAtomicMove(RelocateAtomicMove reloc) {
		if (reloc.getInsertion() == null) {
			return true;
		}
		IRoute<?> rte = reloc.getInsertion().getRoute();
		int ins = reloc.getInsertion().getPosition();
		return !reloc.getNode().isFixed()
				&& (rte.length() <= 3 || ((ins <= 0 || !rte.getNodeAt(ins - 1)
						.isFixed()) && (ins > rte.length() - 1 || !rte
						.getNodeAt(ins).isFixed())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.heuristics.IConstraint#checkSolution(java.lang.Object)
	 */
	@Override
	public boolean isFeasible(S solution) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.heuristics.IConstraint#getInfeasibilityExplanation(java.
	 * lang.Object)
	 */
	@Override
	public String getInfeasibilityExplanation(S solution) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.heuristics.IConstraint#getInfeasibilityExplanation(java.
	 * lang.Object, vroom.common.heuristics.Move)
	 */
	@Override
	public String getInfeasibilityExplanation(S solution, IMove move) {

		if (!isFeasible(solution, move)) {
			return String.format(
					"move:%s will change the position of a fixed node", move);
		}

		return null;
	}

}
