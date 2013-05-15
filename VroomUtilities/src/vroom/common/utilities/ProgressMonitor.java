/**
 * 
 */
package vroom.common.utilities;

/**
 * The class <code>ProgressMonitor</code> provides basic functionalities to monitor and print the progress of a set of
 * iterations. It also estimates the remaining time based on the average time of past iterations.
 * <p>
 * Creation date: Oct 11, 2011 - 10:20:22 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ProgressMonitor {

    private final Stopwatch mTimer;
    private final int       mIterationCount;
    private int             mCurrentIteration;
    private long            mETC;

    private final boolean   mProgressBased;

    private final String    mItFormatString;

    /**
     * Creates a new <code>ProgressMonitor</code>
     * 
     * @param iterationCount
     *            the total expected number of iterations
     * @param progressBased
     *            <code>true</code> if this monitor is based on the progress (in %), or <code>false</code> if it is
     *            based on the current iteration (changes the output of {@link #toString()})
     */
    public ProgressMonitor(int iterationCount, boolean progressBased) {
        super();
        mTimer = new Stopwatch();
        mIterationCount = iterationCount;
        mProgressBased = progressBased;

        int prec = (int) Math.max(1, Math.ceil(Math.log10(mIterationCount)));
        mItFormatString = "%" + prec + "s [t:%s etc:%s]";
    }

    /**
     * Start the progress monitor
     */
    public void start() {
        mTimer.start();
        mCurrentIteration = 0;
        mETC = -1;
    }

    /**
     * Stop the progress monitor
     */
    public void stop() {
        mTimer.stop();
    }

    /**
     * Update the progress monitor once a iteration has been completed
     */
    public void iterationFinished() {
        iterationsFinished(1);
    }

    /**
     * Update the progress monitor once a number of iterations has been completed
     * 
     * @param iterationCount
     *            the number of iterations that were completed
     */
    public void iterationsFinished(int iterationCount) {
        mCurrentIteration += iterationCount;
        mETC = (long) (mTimer.readTimeMS() / mCurrentIteration * (mIterationCount - mCurrentIteration));
    }

    /**
     * Returns the current iteration
     * 
     * @return the current iteration
     */
    public int getIteration() {
        return mCurrentIteration;
    }

    /**
     * Returns the current time in seconds
     * 
     * @return the current time in seconds
     */
    public long getCurrentTime() {
        return (long) mTimer.readTimeS();
    }

    /**
     * Returns the expected time to completion in seconds
     * 
     * @return the expected time to completion in seconds
     */
    public long getExpectedTime() {
        return mETC;
    }

    /**
     * Returns the progress (between 0 and 1) of the monitored process
     * 
     * @return
     */
    public double getProgress() {
        return mIterationCount != 0 ? ((double) mCurrentIteration) / mIterationCount : 0;
    }

    @Override
    public String toString() {
        if (mProgressBased)
            return String.format("%5.1f%% [t:%s etc:%s]",//
                    getProgress() * 100, //
                    Utilities.Time.millisecondsToString((long) mTimer.readTimeMS(), 3, false, false),//
                    Utilities.Time.millisecondsToString(getExpectedTime(), 3, false, false));
        else
            return String.format(mItFormatString,//
                    getIteration(), //
                    Utilities.Time.millisecondsToString((long) mTimer.readTimeMS(), 3, false, false),//
                    Utilities.Time.millisecondsToString(getExpectedTime(), 3, false, false));
    }

}
