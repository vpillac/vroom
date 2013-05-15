/**
 * 
 */
package vroom.common.modeling.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;

/**
 * <code>DefaultRouteCostDelegate</code> is an implementation of
 * {@link IRouteCostDelegate} based on a {@link CostCalculationDelegate}
 * 
 * <p>
 * Creation date: Feb 16, 2011 - 11:27:31 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * 
 * @version 1.0
 * 
 */
public class DefaultRouteCostDelegate implements IRouteCostDelegate {

	private final CostCalculationDelegate mCostDelegate;

	public DefaultRouteCostDelegate(CostCalculationDelegate costDelegate) {
		mCostDelegate = costDelegate;
	}

	@Override
	public double evaluateRoute(IRoute<?> route) {
		double cost = 0;
		ListIterator<? extends INodeVisit> it = route.iterator();
		if (it.hasNext()) {
			INodeVisit pred = it.next();
			INodeVisit succ;
			while (it.hasNext()) {
				succ = it.next();
				cost += mCostDelegate.getCost(pred, succ, route.getVehicle());
				pred = succ;
			}
		}
		route.updateCost(-route.getCost() + cost);
		return cost;
	}

	@Override
	public double getInsertionCost(IRoute<?> route, INodeVisit predecessor,
			INodeVisit node, INodeVisit successor) {
		double delta = 0;

		// Head cost
		if (predecessor != null)
			delta += mCostDelegate.getCost(predecessor, node,
					route.getVehicle());
		// Tail cost
		if (successor != null)
			delta += mCostDelegate.getCost(node, successor, route.getVehicle());
		// Removed arc
		if (predecessor != null && successor != null)
			delta -= mCostDelegate.getCost(predecessor, successor,
					route.getVehicle());

		return delta;
	}

	@Override
	public double getInsertionCost(IRoute<?> route, INodeVisit predecessor,
			IRoute<?> insertedRoute, INodeVisit successor) {
		if (insertedRoute.length() == 0)
			return 0;
		double delta = 0;

		// Head cost
		if (predecessor != null)
			delta += mCostDelegate.getCost(predecessor,
					insertedRoute.getFirstNode(), route.getVehicle());
		// Tail cost
		if (successor != null)
			delta += mCostDelegate.getCost(insertedRoute.getLastNode(),
					successor, route.getVehicle());
		// Removed arc
		if (predecessor != null && successor != null)
			delta -= mCostDelegate.getCost(predecessor, successor,
					route.getVehicle());
		// Inserted route cost
		delta += insertedRoute.getCost();

		return delta;

	}

	@Override
	public double getInsertionCost(IRoute<?> route, INodeVisit predecessor,
			List<? extends INodeVisit> insertedRoute, INodeVisit successor) {
		if (insertedRoute.isEmpty())
			return 0;

		double delta = 0;

		Iterator<? extends INodeVisit> it = insertedRoute.iterator();

		// Head cost
		INodeVisit pred = it.next();
		if (predecessor != null)
			delta += mCostDelegate.getCost(predecessor, pred,
					route.getVehicle());

		// Inserted route cost
		INodeVisit succ = pred;
		while (it.hasNext()) {
			succ = it.next();
			delta += mCostDelegate.getCost(pred, succ, route.getVehicle());
			pred = succ;
		}

		// Tail cost
		if (successor != null)
			delta += mCostDelegate.getCost(succ, successor, route.getVehicle());

		// Removed arc gain
		if (predecessor != null && successor != null)
			delta -= mCostDelegate.getCost(predecessor, successor,
					route.getVehicle());

		return delta;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#nodeInserted(vroom.common
	 * .modelling.dataModel.IRoute, vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void nodeInserted(IRoute<?> route, INodeVisit predecessor,
			INodeVisit node, INodeVisit successor) {
		route.updateCost(getInsertionCost(route, predecessor, node, successor));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#routeInserted(vroom.common
	 * .modelling.dataModel.IRoute, vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.IRoute,
	 * vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void routeInserted(IRoute<?> route, INodeVisit predecessor,
			IRoute<?> insertedRoute, INodeVisit successor) {
		route.updateCost(getInsertionCost(route, predecessor, insertedRoute,
				successor));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#routeInserted(vroom.common
	 * .modelling.dataModel.IRoute, vroom.common.modeling.dataModel.INodeVisit,
	 * java.util.List, vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void routeInserted(IRoute<?> route, INodeVisit predecessor,
			List<? extends INodeVisit> insertedRoute, INodeVisit successor) {
		route.updateCost(getInsertionCost(route, predecessor, insertedRoute,
				successor));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#nodeRemoved(vroom.common
	 * .modelling.dataModel.IRoute, vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void nodeRemoved(IRoute<?> route, INodeVisit predecessor,
			INodeVisit node, INodeVisit successor) {
		route.updateCost(-getInsertionCost(route, predecessor, node, successor));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#subrouteRemoved(vroom.
	 * common.modelling.dataModel.IRoute,
	 * vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.IRoute,
	 * vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void subrouteRemoved(IRoute<?> route, INodeVisit predecessor,
			IRoute<?> removedRoute, INodeVisit successor) {
		route.updateCost(-getInsertionCost(route, predecessor, removedRoute,
				successor));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#subrouteRemoved(vroom.
	 * common.modelling.dataModel.IRoute,
	 * vroom.common.modeling.dataModel.INodeVisit, java.util.List,
	 * vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void subrouteRemoved(IRoute<?> route, INodeVisit predecessor,
			List<? extends INodeVisit> removedRoute, INodeVisit successor) {
		route.updateCost(-getInsertionCost(route, predecessor, removedRoute,
				successor));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.modeling.util.IRouteCostDelegate#subrouteReversed(vroom
	 * .common.modelling.dataModel.IRoute,
	 * vroom.common.modeling.dataModel.INodeVisit,
	 * vroom.common.modeling.dataModel.INodeVisit)
	 */
	@Override
	public void subrouteReversed(IRoute<?> route, INodeVisit predecessor,
			INodeVisit first, INodeVisit last, INodeVisit successor) {
		double delta = 0;

		if (predecessor != null) {
			delta -= mCostDelegate.getCost(predecessor, first,
					route.getVehicle());
			delta += mCostDelegate.getCost(predecessor, last,
					route.getVehicle());
		}

		if (successor != null) {
			delta -= mCostDelegate.getCost(last, successor, route.getVehicle());
			delta += mCostDelegate
					.getCost(first, successor, route.getVehicle());
		}

		route.updateCost(delta);

	}

	@Override
	public void nodeReplaced(IRoute<?> route, INodeVisit predecessor,
			INodeVisit previousNode, INodeVisit node, INodeVisit successor) {
		double delta = 0;

		if (predecessor != null) {
			delta -= mCostDelegate.getCost(predecessor, previousNode,
					route.getVehicle());
			delta += mCostDelegate.getCost(predecessor, node,
					route.getVehicle());
		}
		if (successor != null) {
			delta -= mCostDelegate.getCost(previousNode, successor,
					route.getVehicle());
			delta += mCostDelegate.getCost(node, successor, route.getVehicle());
		}

		route.updateCost(delta);
	}

	@Override
	public void nodesSwapped(IRoute<?> route, INodeVisit pred1,
			INodeVisit node1, INodeVisit succ1, INodeVisit pred2,
			INodeVisit node2, INodeVisit succ2) {
		double delta = 0;
		if (node2 == succ1) {
			if (pred1 != null) {
				delta -= mCostDelegate
						.getCost(pred1, node1, route.getVehicle());
				delta += mCostDelegate
						.getCost(pred1, node2, route.getVehicle());
			}
			if (succ2 != null) {
				delta -= mCostDelegate
						.getCost(node2, succ2, route.getVehicle());
				delta += mCostDelegate
						.getCost(node1, succ2, route.getVehicle());
			}
			delta -= mCostDelegate.getCost(node1, node2, route.getVehicle());
			delta += mCostDelegate.getCost(node2, node1, route.getVehicle());
		} else {
			delta -= getInsertionCost(route, pred1, node1, succ1);
			delta += getInsertionCost(route, pred2, node1, succ2);
			delta -= getInsertionCost(route, pred2, node2, succ2);
			delta += getInsertionCost(route, pred1, node2, succ1);
		}
		route.updateCost(delta);
	}

}
