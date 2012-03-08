/*
 * Erstellt am 27.10.2011.
 */
package utils;

import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Thread implements wake and run functions. It sleeps itselfs.
 * @author neiss
 */
public final class WorkerThread implements Runnable, Loggable {

    private Thread myThread;
    private volatile boolean isRunning = false;
	private SmartRunnable worker;
	private boolean dieCalled = false;

    public WorkerThread(SmartRunnable worker) {
		assert worker != null;
		this.worker = worker;
        myThread = new Thread(this);
        myThread.start();
    }

	/**
	 * Wakes thread so it can work.
	 */
    public synchronized void wake() {
        if (!isRunning) {
            isRunning = true;
            this.notifyAll();
        }
    }

	/**
	 * This function should be never called!
	 * It is called automaticaly by thread management.
	 */
	@Override
    public void run() {
        while (!dieCalled) {
            worker.doMyWork();
            synchronized (this) {
                isRunning = false;
                while (!isRunning && !dieCalled) {
                    //tenhlecten cyklus je tady proti nejakejm falesnejm buzenim.
                    //System.out.println(this.toString()+": Beh cyklu s wait()");
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
						Logger.log(this, Logger.ERROR, LoggingCategory.GENERIC, "InterruptedException in the sleep! Is it normal on not?", ex);
                    }
                }
            }
        }
    }

	/**
	 * Wakes thread and dies.
	 */
	public void die() {
		this.dieCalled = true;
		wake();
	}

	@Override
	public String getDescription() {
		return "WorkerThread";
	}
}
