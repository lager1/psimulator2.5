/*
 * Erstellt am 27.10.2011.
 */
package psimulator2;

/**
 * Thread implements wake and run functions. It sleeps itselfs.
 * @author neiss
 */
public final class WorkerThread implements Runnable {

    private Thread myThread;
    private volatile boolean isRunning = false;
	private SmartRunnable worker;

    public WorkerThread(SmartRunnable worker) {
		assert worker != null;
		this.worker = worker;
        myThread = new Thread(this);
        myThread.start();
    }



    /**
     * Wakes thread.
     */
    public synchronized void wake() {
        if (isRunning) {
            return;
        } else {
            isRunning = true;
            this.notifyAll();
        }
    }

	/**
	 * This function should be never called!
	 * It is called automaticaly thread management.
	 */
    public void run() {
        while (true) {
            worker.doMyWork();
            synchronized (this) {
                isRunning = false;
                while (!isRunning) {
                    //tenhlecten cyklus je tady proti nejakejm falesnejm buzenim.
                    //System.out.println(this.toString()+": Beh cyklu s wait()");
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        System.exit(2);
                    }
                }
            }
        }
    }
}
