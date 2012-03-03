/*
 * created 2.2.2012
 */

package psimulator2;

import device.Device;
import java.util.ArrayList;
import java.util.List;
import logging.Logger;

/**
 * Instance of Psimulator.
 * Pouzit navrhovy vzor Singleton.
 *
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Psimulator {


	public final List<Device> devices=new ArrayList<Device>();
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
}
