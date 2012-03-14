/*
 * created 2.2.2012
 */
package logging;

import dataStructures.L2Packet;
import dataStructures.L3Packet;
import java.util.EnumMap;
import java.util.Map;
import psimulator2.Psimulator;

/**
 * Main server listener. Writes everything to server's console.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomáš Pitřinec
 */
public class SystemListener implements LoggerListener {

	/**
	 * Key - logging cathegory <br />
	 * Value - Logger.{ERROR,WARNING,IMPORTAN,INFO,DEBUG}, if INFO selected all facilities before it are also selected.
	 */
	public final Map<LoggingCategory, Integer> configuration = new EnumMap<>(LoggingCategory.class);

	public SystemListener() {
		ConfigureSystemListener.configure(configuration);
	}

	@Override
	public void listen(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {
		try {

			if (logLevel <= configuration.get(category)) {
				String vypsat = "[" + Logger.logLevelToString(logLevel) + "] " + category + ": " + caller.getDescription() + ": ";

				if (object instanceof Exception) {
					// vypisuje se to az potom.
				} else if (object != null) {
					vypsat+=object.toString()+" | ";

				}
				vypsat+=message;
				System.out.println(vypsat);
				if (object instanceof Exception) {
					((Exception) object).printStackTrace();
				}
			}

		} catch (NullPointerException e) {
			System.out.println("An error occured during logging:-) \n"
					+ "LoggingCategory was null.");
			System.exit(3);
		}
	}

	@Override
	public void listen(String name, int logLevel, LoggingCategory category, String message) {
		try {
			if (logLevel <= configuration.get(category)) {
				System.out.println("[" + Logger.logLevelToString(logLevel) + "] " + category + ": " + name + ": " + message);
			}
		} catch (NullPointerException e) {
			System.out.println("An error occured during logging:-) \n"
					+ "LoggingCategory was null.");
			System.exit(3);
		}
	}
}
