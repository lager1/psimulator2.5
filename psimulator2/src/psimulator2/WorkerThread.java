/*
 * Erstellt am 27.10.2011.
 */
package psimulator2;

/**
 *
 * @author neiss
 */
public abstract class WorkerThread implements Runnable {

    private Thread myThread;
    private volatile boolean isRunning = false;

    public WorkerThread() {
        myThread = new Thread(this);
        myThread.start();
    }

    protected abstract void doMyWork();

    protected synchronized void wake() {
        if (isRunning) {
            return;
        } else {
            isRunning = true;
            this.notifyAll();
        }
    }

    public void run() {
        while (true) {
            doMyWork();
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
