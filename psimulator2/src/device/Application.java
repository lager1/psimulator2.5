/*
 * created 28.10.2011
 */
package device;

import psimulator2.SmartRunnable;
import psimulator2.WorkerThread;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class Application implements SmartRunnable {
	private int PID;
	private String name;
	private WorkerThread worker = new WorkerThread(this);

	public abstract boolean start();
	public abstract boolean stop();

	/**
	 * Restarts the application. Calls stop() and then start()
	 */
	public boolean restart() {
		boolean stop = stop();
		boolean start = start();
		return start && stop;
	}

	public String getName() {
		return name;
	}

	public int getPID() {
		return PID;
	}
}
