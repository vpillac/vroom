/**
 * 
 */
package vroom.trsp.optimization.constraints;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.localSearch.TRSPRelocate.RelocateMove;

/**
 * <code>SolutionConstraintHandler</code>
 * <p>
 * Creation date: Feb 9, 2012 - 11:18:36 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolutionConstraintHandler extends ConstraintHandler<TRSPSolution> {

    private final TourConstraintHandler mTourConstraintHandler;

    /**
     * Returns the associated tour constraint handler
     * 
     * @return the associated tour constraint handler
     */
    public TourConstraintHandler getTourConstraintHandler() {
        return mTourConstraintHandler;
    }

    /**
     * Creates a new <code>SolutionConstraintHandler</code>
     * 
     * @param tourHandler
     * @param constraints
     */
    public SolutionConstraintHandler(TourConstraintHandler tourHandler,
            IConstraint<TRSPSolution>... constraints) {
        super(constraints);
        mTourConstraintHandler = tourHandler;
        addConstraint(0, new TourConstraints());
    }

    public static SolutionConstraintHandler newConstraintHandler(TRSPInstance instance) {
        TourConstraintHandler tourHandler = TourConstraintHandler.newConstraintHandler(instance);
        return new SolutionConstraintHandler(tourHandler);
    }

    /**
     * The class <code>TourConstraints</code> serves as interface to check moves using the tour constraints defined in
     * {@link SolutionConstraintHandler#getTourConstraintHandler()}
     * <p>
     * Creation date: Feb 9, 2012 - 11:31:16 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class TourConstraints implements IConstraint<TRSPSolution> {

        @Override
        public boolean isFeasible(TRSPSolution solution) {
            for (TRSPTour t : solution)
                if (!getTourConstraintHandler().isFeasible(t))
                    return false;
            return true;
        }

        @Override
        public boolean isFeasible(TRSPSolution solution, IMove move) {
            if (move instanceof RelocateMove) {
                RelocateMove mve = (RelocateMove) move;
                return getTourConstraintHandler().isFeasible(mve.getInsertion().getTour(),
                        mve.getInsertion());
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported move: %s", move));
            }
        }

        @Override
        public String getInfeasibilityExplanation(TRSPSolution solution) {
            String infeas = null;
            for (TRSPTour t : solution) {
                String i = getTourConstraintHandler().getInfeasibilityExplanation(t);
                if (i != null) {
                    infeas = String.format("%s%s:%s", infeas == null ? "" : infeas + ", ",
                            t.getTechnicianId(), i);
                }
            }
            return infeas;
        }

        @Override
        public String getInfeasibilityExplanation(TRSPSolution solution, IMove move) {
            if (move instanceof RelocateMove) {
                RelocateMove mve = (RelocateMove) move;
                return getTourConstraintHandler().getInfeasibilityExplanation(
                        mve.getInsertion().getTour(), mve.getInsertion());
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported move: %s", move));
            }
        }
    }
}
