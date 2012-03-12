package shell;

import commands.LongTermCommand;
import commands.LongTermCommand.Signal;
import java.util.regex.Pattern;
import logging.Logger;
import logging.LoggingCategory;
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

	public static void handleControlCodes(LongTermCommand catchAble, int code) {

		switch (code) {
			case TerminalIO.CTRL_C:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+C");
				catchAble.catchSignal(Signal.CTRL_C);
				break;
			case TerminalIO.CTRL_Z:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+Z");
				catchAble.catchSignal(Signal.CTRL_Z);
				break;
			case TerminalIO.CTRL_D:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+D");
				catchAble.catchSignal(Signal.CTRL_D);
				break;
			case TerminalIO.CTRL_SHIFT_6:
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+SHIFT+6");
				catchAble.catchSignal(Signal.CTRL_SHIFT_6);

		}

	}

	public static boolean isPrintable(int znakInt) {
		return ShellUtils.printablePatter.matcher(String.valueOf((char) znakInt)).find();
	}
}
