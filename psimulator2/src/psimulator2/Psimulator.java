/*
 * created 2.2.2012
 */

package psimulator2;

import device.Device;
import java.util.ArrayList;
import java.util.List;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Instance of Psimulator.
 * Pouzit navrhovy vzor Singleton.
 *
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Psimulator {


	public final List<Device> devices=new ArrayList<>();
	public final Logger logger = new Logger();


	private Psimulator() {

	}



// staticky metody (pro ten singleton)

	private static volatile Psimulator instance;

	public static Psimulator getPsimulator() {
		if (instance == null) {
			synchronized(Psimulator.class) {
				if (instance == null) {
					instance = new Psimulator();
				}
			}
		}
		return instance;
	}

	/**
	 * Zkratka pro logovani.
	 * @return
	 */
	public static Logger getLogger(){
		return getPsimulator().logger;
	}

	/**
	 * Zkratka pro logovani.
	 */
	public static void logg(String name, int logLevel, LoggingCategory category, String message) {
		getPsimulator().logger.log(name, logLevel, category, message);
	}
}
