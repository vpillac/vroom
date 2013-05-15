package vroom.common.utilities;

import java.util.concurrent.ThreadFactory;

/**
 * <code>ProcessKiller</code> is a utility class used to monitor a {@link Process} and eventually
 * {@link Process#destroy() destroy} if its running time exceeds a certain limit.
 * <p>
 * Creation date: Sep 20, 2010 - 11:37:01 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ProcessDestroyer implements Runnable {

    private final static ThreadFactory sFactory = new ThreadFactory() {
                                                    private int mThreadCount = 0;

                                                    @Override
                                                    public Thread newThread(Runnable r) {
                                                        Thread t = new Thread(r, "ProcDest-"
                                                                + mThreadCount++);
                                                        t.setDaemon(true);
                                                        return t;
                                                    }
                                                };

    private final long                 mTimeout;

    private final Object               mSync;

    private boolean                    mCanceled;

    private final Process              mProcess;

    public ProcessDestroyer(Process process, long timeout) {
        super();
        mTimeout = timeout;
        mSync = new Object();
        mProcess = process;
        mCanceled = false;
    }

    /**
     * Cancel this process destroyer.
     */
    public void cancel() {
        mCanceled = true;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        synchronized (mSync) {
            while (!mCanceled && System.currentTimeMillis() - startTime <= mTimeout) {
                try {
                    mSync.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!mCanceled) {
            mProcess.destroy();
        }
    }

    public static ProcessDestroyer monitorProcess(Process process, long timeout) {
        ProcessDestroyer dest = new ProcessDestroyer(process, timeout);
        sFactory.newThread(dest).start();
        return dest;
    }
}