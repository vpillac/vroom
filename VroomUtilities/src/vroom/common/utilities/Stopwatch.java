package vroom.common.utilities;

/**
 * The class <code>Stopwatch</code> provides a stopwatch with nanosecond
 * accuracy, pause and resume functionalities, and various methods to convert a
 * time to a string.
 * <p>
 * Creation date: Oct 11, 2011 - 5:21:44 PM
 * 
 * @author Andres L. Medaglia, <a href="http://uniandes.edu.co">Universidad de
 *         Los Andes</a>, Victor Pillac, <a
 *         href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 5.0
 */
public class Stopwatch implements ICloneable<Stopwatch> {

	/** The number of milliseconds in a day. */
	public final static int MS_IN_DAY = 86400000;

	/** The number of nanoseconds in a day. */
	public final static long NS_IN_DAY = 86400000000000l;

	/** The number of milliseconds in an hour. */
	public final static int MS_IN_HOUR = 3600000;

	/** The number of nanoseconds in an hour. */
	public final static long NS_IN_HOUR = 3600000000000l;

	/** The number of milliseconds in a minute. */
	public final static int MS_IN_MIN = 60000;

	/** The number of nanoseconds in a minute. */
	public final static long NS_IN_MIN = 60000000000l;

	/** The number of nanoseconds in a millisecond (10e6). */
	public final static long NS_IN_MS = 1000000l;

	/** The number of nanoseconds in a millisecond (10e6). */
	public final static double NS_IN_MS_D = 1000000d;

	/** The number of nanoseconds in a second (10e9). */
	public final static long NS_IN_S = 1000000000l;

	/** The number of nanoseconds in a second (10e9). */
	public final static double NS_IN_S_D = 1000000000d;

	/** Time when the stopwatch starts. */
	private long mStartTime;

	/** Time when the stopwatch has been paused. */
	private long mPauseTime;

	/** Time when the stopwatch has been stops. */
	private long mEndTime;

	/** A timout for this stopwatch. */
	private long mTimeout;

	/**
	 * Set the timeout for this instance.
	 * 
	 * @param timeout
	 *            the new timout
	 * @see #isTimedout()
	 */
	public void setTimout(long timeout) {
		mTimeout = timeout;
	}

	/**
	 * Get the timeout value for this instance.
	 * 
	 * @return the number of ms after which the {@link #hasTimedOut()} method
	 *         will return <code>true</code>
	 */
	public long getTimeout() {
		return mTimeout;
	}

	/**
	 * Timeout of this stopwatch.
	 * <p/>
	 * Compare the current running time to the timeout value
	 * 
	 * @return <code>true</code> if the stopwatch has timed out
	 * @see #setTimout(long)
	 */
	public boolean hasTimedOut() {
		if (mStartTime < 0) {
			return false;
			// throw new
			// IllegalStateException("The stopwatch has not been started");
		}
		return readTimeMS() > mTimeout;
	}

	/** An accumulated time used when pausing/resuming (in nanoseconds). */
	private long mAccumulated;

	/**
	 * Constructor.
	 * 
	 * @since JDK1.3.1
	 */
	public Stopwatch() {
		mStartTime = -1;
		mEndTime = -1;
		mPauseTime = -1;
		mTimeout = Long.MAX_VALUE;
	}

	/**
	 * Creates a new <code>stopwatch</code> with the given timeout value.
	 * 
	 * @param timeout
	 *            the timeout for this stopwatch (in ms)
	 * @see Stopwatch#hasTimedOut()
	 */
	public Stopwatch(long timeout) {
		this();
		setTimout(timeout);
	}

	/**
	 * Started state.
	 * 
	 * @return {@code true} if the stopwatch is started, {@code false} otherwise
	 */
	public boolean isStarted() {
		return mStartTime >= 0;
	}

	/**
	 * Stopped state.
	 * 
	 * @return {@code true} if the stopwatch is stopped, {@code false} otherwise
	 */
	public boolean isStopped() {
		return mEndTime >= 0;
	}

	/**
	 * Resume a paused stopwatch.
	 */
	public void resume() {
		if (mStartTime < 0)
			throw new IllegalStateException("Stopwatch is not started");
		else if (mPauseTime < 0)
			throw new IllegalStateException("Stopwatch is not paused");
		else if (mEndTime > 0)
			throw new IllegalStateException("Stopwatch is stopped");
		else {
			mStartTime = System.nanoTime();
			mEndTime = -1;
			mPauseTime = -1;
		}

	}

	/**
	 * Starts the stopwatch.
	 */
	public void start() {
		if (mEndTime > 0)
			throw new IllegalStateException(
					"Stopwatch is stopped - restart first");
		if (mStartTime > 0)
			if (mPauseTime > 0)
				throw new IllegalStateException(
						"Stopwatch is already started and paused, use resume instead");
			else
				throw new IllegalStateException("Stopwatch is already started");

		if (mPauseTime >= 0) {
			mAccumulated += mPauseTime - mStartTime;
		}
		mStartTime = System.nanoTime();
		mEndTime = -1;
		mPauseTime = -1;
	}

	/**
	 * Restart this stopwatch, erasing possible accumulated time.
	 */
	public void restart() {
		reset();
		start();
	}

	/**
	 * Stops the stopwatch.
	 * <p>
	 * This method prevents further calls to {@link #pause()}, {@link #resume()}
	 * , and {@link #start()} until {@link #reset()} is called
	 * </p>
	 * <p>
	 * If the stopwatch was paused the final time is the time at which the
	 * stopwatch was paused
	 * </p>
	 */
	public void stop() {
		if (mEndTime > 0)
			throw new IllegalStateException("Stopwatch is already stopped");
		else if (mStartTime < 0)
			throw new IllegalStateException("Stopwatch is not started");
		if (mPauseTime > 0) {
			// Virtually resume and stop the time at the last pause time
			mEndTime = mPauseTime;
			mStartTime = mPauseTime;
			mPauseTime = -1;
		} else {
			mEndTime = System.nanoTime();
		}
	}

	/**
	 * Pause the stopwatch, that can latter be resumed with a call to
	 * {@link #resume()}.
	 */
	public void pause() {
		if (mEndTime > 0)
			throw new IllegalStateException("Stopwatch is stopped");
		else if (mStartTime < 0)
			throw new IllegalStateException("Stopwatch is not started");
		else if (mPauseTime > 0)
			throw new IllegalStateException("Stopwatch is already paused");

		mPauseTime = System.nanoTime();
		mAccumulated += mPauseTime - mStartTime;
	}

	/**
	 * Sets the accumulated time on this timer.
	 * 
	 * @param time
	 *            the accumulated time to be set (in ns)
	 */
	public void setAccumulatedTime(long time) {
		if (mPauseTime < 0)
			throw new IllegalStateException("Stopwatch must be paused first");
		mAccumulated = time;
	}

	/**
	 * Reset this stopwatch.
	 */
	public void reset() {
		mStartTime = -1;
		mEndTime = -1;
		mPauseTime = -1;
		mAccumulated = 0;
	}

	/**
	 * Read the elapsed time on this stopwatch (in nanoseconds):
	 * <ul>
	 * <li>If the stopwatch is not started: returns {@code  0}</li>
	 * <li>If the stopwatch is stopped: returns the total final elapsed time</li>
	 * <li>If the stopwatch is paused: returns the total elapsed time at the
	 * time it was paused</li>
	 * <li>If the stopwatch is running: returns the current elapsed time</li>.
	 * 
	 * @return the elapsed time on this stopwatch (in nanoseconds)
	 */
	public long readTime() {
		if (mStartTime < 0)
			return 0;
		else if (mEndTime > 0)
			return mEndTime - mStartTime + mAccumulated;
		else if (mPauseTime > 0)
			return mAccumulated;
		else
			return System.nanoTime() - mStartTime + mAccumulated;
	}

	/**
	 * Read the elapsed time on this stopwatch (in milliseconds).
	 * 
	 * @return the elapsed time on this stopwatch (in milliseconds)
	 * @see #readTime()
	 */
	public double readTimeMS() {
		return readTime() / NS_IN_MS_D;
	}

	/**
	 * Read the elapsed time on this stopwatch (in seconds).
	 * 
	 * @return the elapsed time on this stopwatch (in seconds)
	 * @see #readTime()
	 */
	public double readTimeS() {
		return readTime() / NS_IN_S_D;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (!isStarted()) {
			if (mTimeout != Long.MAX_VALUE && mTimeout != Integer.MAX_VALUE) {
				return String.format("not started, timeout:%sms", getTimeout());
			} else {
				return "not started";
			}
		} else {
			if (mTimeout != Long.MAX_VALUE && mTimeout != Integer.MAX_VALUE) {
				return String.format("%.0f/%sms", readTimeMS(), getTimeout());
			} else {
				return String.format("%.0fms", readTimeMS());
			}
		}
	}

	/**
	 * Remaining time before the timeout.
	 * 
	 * @return the time remaining before timeout
	 */
	public long getRemainingTime() {
		if (mTimeout == Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		} else {
			return (long) (getTimeout() - readTimeMS());
		}
	}

	/**
	 * Remaining time before the timeout, in seconds.
	 * 
	 * @return the time remaining before timeout, in seconds
	 */
	public long getRemainingTimeSec() {
		if (mTimeout == Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		} else {
			return (long) (getTimeout() / 1000 - readTimeS());
		}
	}

	/**
	 * Read the elapsed time and return an human friendly string.
	 * 
	 * @return the elapsed time in an human friendly string
	 */
	public String readTimeString() {
		return Utilities.Time.millisecondsToString((long) readTimeMS(), 3,
				false, false);
	}

	/**
	 * Read the elapsed time and return an human friendly string.
	 * 
	 * @param maxDigits
	 *            the number of digits to show
	 * @param showMS
	 *            <code>true</code> if ms should be displayed,
	 *            <code>false</code> otherwise
	 * @param showAll
	 *            <code>false</code> to show only non-null values
	 * @return the elapsed time in an human friendly string
	 */
	public String readTimeString(int maxDigits, boolean showMS, boolean showAll) {
		return Utilities.Time.millisecondsToString((long) readTimeMS(),
				maxDigits, showMS, showAll);
	}

	/** The R ostopwatch. */
	private final ReadOnlyStopwatch mROstopwatch = new ReadOnlyStopwatch();

	/**
	 * Read only proxy to this stopwatch.
	 * 
	 * @return a read only stopwatch linked to this instance
	 */
	public ReadOnlyStopwatch getReadOnlyStopwatch() {
		return mROstopwatch;
	}

	/**
	 * <code>ReadOnlystopwatch</code> is a proxy that provides read only access
	 * to a stopwatch
	 * <p>
	 * Creation date: 18/08/2010 - 16:27:36.
	 * 
	 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de
	 *         Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
	 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
	 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp"
	 *         >SLP</a>
	 * @version 1.0
	 */
	public class ReadOnlyStopwatch {

		/**
		 * Get the timeout value for this instance.
		 * 
		 * @return the number of ms after which the {@link #hasTimedOut()}
		 *         method will return <code>true</code>
		 */
		public long getTimeout() {
			return Stopwatch.this.getTimeout();
		}

		/**
		 * Timeout of this stopwatch.
		 * <p/>
		 * Compare the current running time to the timeout value
		 * 
		 * @return <code>true</code> if the stopwatch has timed out
		 * @see #setTimout(long)
		 */
		public boolean hasTimedOut() {
			return Stopwatch.this.hasTimedOut();
		}

		/**
		 * Read the elapsed time on this stopwatch (in milliseconds).
		 * 
		 * @return the elapsed time on this stopwatch (in milliseconds)
		 * @see Stopwatch#readTime()
		 */
		public double readTimeMS() {
			return Stopwatch.this.readTimeMS();
		}

		/**
		 * Read the elapsed time on this stopwatch (in seconds).
		 * 
		 * @return the elapsed time on this stopwatch (in seconds)
		 * @see Stopwatch#readTime()
		 */
		public double readTimeS() {
			return Stopwatch.this.readTimeS();
		}

		/**
		 * Read the elapsed time on this stopwatch (in nanoseconds).
		 * 
		 * @return the elapsed time on this stopwatch (in nanoseconds)
		 * @see Stopwatch#readTime()
		 */
		public long readTime() {
			return Stopwatch.this.readTime();
		}

		/**
		 * Elapsed time in a human friendly string.
		 * 
		 * @return a string describing the current elapsed time
		 */
		public String readTimeString() {
			return Stopwatch.this.readTimeString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return Stopwatch.this.toString();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Stopwatch clone() {
		Stopwatch clone = new Stopwatch();
		clone.mStartTime = mStartTime;
		clone.mEndTime = mEndTime;
		clone.mAccumulated = mAccumulated;
		clone.mTimeout = mTimeout;
		return clone;
	}

}
