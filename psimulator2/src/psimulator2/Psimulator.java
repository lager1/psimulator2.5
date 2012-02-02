/*
 * created 2.2.2012
 */

package psimulator2;

import logging.Logger;

/**
 * Instance of Psimulator.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Psimulator {

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

	public final Logger logger;

	private Psimulator() {
		logger = new Logger();
	}
}
