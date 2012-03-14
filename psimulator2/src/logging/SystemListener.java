/*
 * created 2.2.2012
 */
package logging;

import dataStructures.L2Packet;
import dataStructures.L3Packet;
import dataStructures.L4Packet;
import java.util.EnumMap;
import java.util.Map;

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
				String zacatek = "[" + Logger.logLevelToString(logLevel) + "] " + category + ": " + caller.getDescription() + ": ";

				if (object instanceof Exception) {	// vyjimka
					System.out.println(zacatek + message);
					((Exception) object).printStackTrace();

				} else if (object instanceof L2Packet || object instanceof L3Packet || object instanceof L4Packet) {	// paket
					System.out.println(zacatek + object.toString() + " | " + message);

				} else if (object != null) {	// nejakej jinej object, ten se vypisuje na konec
					System.out.println(zacatek + message+" (" + object.toString()+")");
				} else {
					System.out.println(zacatek + message);
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
