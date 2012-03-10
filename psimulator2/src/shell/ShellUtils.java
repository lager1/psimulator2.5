package shell;

import commands.AbstractCommandParser;
import java.util.regex.Pattern;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;
import telnetd.io.TerminalIO;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ShellUtils {

	public static Pattern printablePatter = Pattern.compile(ShellUtils.getPrintableRegExp());

	public static String getPrintableRegExp() {
		return "\\p{Print}";
	}

	public static void handleControlCodes(SignalCatchAble catchAble, int code) {

		switch (code) {
			case TerminalIO.CTRL_C:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+C");
				catchAble.catchSignal(AbstractCommandParser.Signal.INT);
				break;
			case TerminalIO.CTRL_Z:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+Z");
				catchAble.catchSignal(AbstractCommandParser.Signal.ENDZ);
				break;
			case TerminalIO.CTRL_D:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+D");
				catchAble.catchSignal(SignalCatchAble.Signal.CTRL_D);
				break;

		}

	}

	public static boolean isPrintable(int znakInt) {
		return ShellUtils.printablePatter.matcher(String.valueOf((char) znakInt)).find();
	}
}
