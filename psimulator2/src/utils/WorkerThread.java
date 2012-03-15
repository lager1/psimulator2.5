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
	private SmartRunnable smartRunnable;

	/**
	 * Jestli ma vlakno umrit.
	 */
	private boolean dieCalled = false;

    public WorkerThread(SmartRunnable smartRunnable) {
		assert smartRunnable != null;
		this.smartRunnable = smartRunnable;
        myThread = new Thread(this,smartRunnable.getDescription());
        myThread.start();
    }

	/**
	 * Wakes thread so it can work.
	 */
	public synchronized void wake() {

		if (!isRunning) {	//kdyz nebezi tak se zapne
			isRunning = true;
			this.notifyAll();

		} else if (smartRunnable.getClass() == Alarm.class) {	// specialni fce JEN PRO BUDIK !!!
			if (myThread.getState() == Thread.State.TIMED_WAITING) {
				myThread.interrupt();
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

	/**
	 * This function should be never called! It is called automaticaly by thread management.
	 */
	@Override
	public void run() {
		while (!dieCalled) {	// ma-li se umrit kdyz zrovna nebezi doMyWork, umre se okamzite
			smartRunnable.doMyWork();
			if (dieCalled) {	// ma-li se umrit po metode doMyWork taky se hned umre
				return;
			}
			sleep();
		}
	}

	/**
	 * Synchronizovana metoda na spani. Pred 13.3. byla v synchronizovanym bloku v ramci metody run, ted jsem ji pro prehlednost vyclenil sem.
	 */
	private synchronized void sleep() {
		isRunning = false;
		while (!isRunning) {
			//tenhlecten cyklus je tady proti nejakejm falesnejm buzenim.
			try {
				wait();
			} catch (InterruptedException ex) {
				Logger.log(this, Logger.ERROR, LoggingCategory.GENERIC, "InterruptedException in the sleep! Is it normal on not?", ex);
			}
		}
	}

	@Override
	public String getDescription() {
		return "WorkerThread";
	}

	public String getThreadName(){
		return myThread.getName();
	}
}
