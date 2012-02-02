/*
 * created 2.2.2012
 */
package logging;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Logger {

	private final List<LoggerListener> listeners = new LinkedList<LoggerListener>();

	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int IMPORTANT = 3;
	public static final int INFO = 4;
	public static final int DEBUG = 5;

	public Logger() {
		listeners.add(new SystemListener());
	}

	public void logg(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {
		for (LoggerListener listener : listeners) {
			listener.listen(caller, logLevel, category, message, object);
		}

		if (logLevel == ERROR) {
			System.exit(2);
		}
	}

	public static String logLevelToString(int logLevel) {
		switch (logLevel) {
			case 1:
				return "ERROR";
			case 2:
				return "WARNING";
			case 3:
				return "IMPORTANT";
			case 4:
				return "INFO";
			case 5:
				return "DEBUG";
			default:
				return "UNKNOWN_LOG_LEVEL";
		}
	}
}
