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

	private final List<LoggerListener> listeners = new LinkedList<>();

	/**
	 * 1: Zavazna chyba. Vypise na hlavni serverovou konzoli (normalni println) a ukonci program.
	 */
	public static final int ERROR = 1;
	/**
	 * 2: Varovani. Deje se neco divnyho ale nemusim kvuli tomu ukoncit program. Rozhodne by se to ale melo vypisovat na
	 * serverovou konsoli.
	 */
	public static final int WARNING = 2;
	/**
	 * 3: Dulezitejsi zpravy, informace o spusteni pocitace, informace o zahozenych paketech, ..
	 */
	public static final int IMPORTANT = 3;
	/**
	 * 4: Standardni logovani, vcetne posilani zprav pro Martina Svihlika.
	 */
	public static final int INFO = 4;
	/**
	 * 5: Ladici rezim.
	 */
	public static final int DEBUG = 5;

	public Logger() {
		listeners.add(new SystemListener());
	}

	/**
	 * Zalogovat zpravu. Nejkomplexnejsi logovani, loguje se jak objekt, kterej loguje, tak i nejakej objekt k ty
	 * zprave.
	 *
	 * @param caller odkaz na volajiciho
	 * @param logLevel vlozit pres logging.Logger.
	 * @param category ze ktere tridy je logovana zprava, napr. ETHERNET_LAYER nebo IP_LAYER ..
	 * @param message logovana zprava
	 * @param object zalogovany objekt, napr. EthernetPacket ci IpPacket ..
	 */
	public void logg(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {
		for (LoggerListener listener : listeners) {
			listener.listen(caller, logLevel, category, message, object);
		}

		if (logLevel == ERROR) {
			System.exit(2);
		}
	}

	/**
	 * Zalogovat zpravu. Jednodussi logovani, logovana trida nemusi byt Loggable, posila se jen string s description.
	 * Neposila se zadnej dodatecnej objekt.
	 *
	 * @param name identifikace volajiciho (abychom vedeli, kdo tu zpravu poslal)
	 * @param logLevel vlozit pres logging.Logger.
	 * @param category ze ktere tridy je logovana zprava, napr. ETHERNET_LAYER nebo IP_LAYER ..
	 * @param message logovana zprava
	 */
	public void logg(String name, int logLevel, LoggingCategory category, String message) {
		for (LoggerListener listener : listeners) {
			listener.listen(name, logLevel, category, message);
		}

		if (logLevel == ERROR) {
			System.exit(2);
		}
	}

	/**
	 * Zalogovat zpravu. Uple nejjednodussi logovani, automaticky se zjisti jméno třídy která zavolala log.
	 *
	 * @param logLevel vlozit pres logging.Logger.
	 * @param category ze ktere tridy je logovana zprava, napr. ETHERNET_LAYER nebo IP_LAYER ..
	 * @param message logovana zprava
	 */
	public static void log(int logLevel, LoggingCategory category, String message) {
		String name = new Exception().getStackTrace()[1].getClassName();
		List<LoggerListener> listeners = psimulator2.Psimulator.getLogger().listeners;

		for (LoggerListener listener : listeners) {
			listener.listen(name, logLevel, category, message);
		}

		if (logLevel == ERROR) {
			System.exit(2);
		}
	}

	/**
	 * Returns String representation of int logLevel.
	 *
	 * @param logLevel
	 * @return
	 */
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
