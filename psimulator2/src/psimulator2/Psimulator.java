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

	private static String name="psimulator";


	public final List<Device> devices=new ArrayList<>();


	private Psimulator() {

	}

	public static String getNameOfProgram(){
		return name;
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


}
