/*
 * created 28.10.2011
 */
package device;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class Application {
	private int PID;
	private String name;
	private AbstractDevice device;

	public Application(int PID, String name, AbstractDevice device) {
		this.PID = PID;
		this.name = name;
		this.device = device;
	}

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
