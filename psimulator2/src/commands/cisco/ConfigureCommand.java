/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import static shell.apps.CommandShell.CommandShell.CISCO_CONFIG_MODE;

/**
 * Configure command on Cisco IOS.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ConfigureCommand extends CiscoCommand {

	public ConfigureCommand(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {

		String nextWord = nextWord();

		if (nextWord.isEmpty()) {
			parser.runningCommand = this;
			print("Configuring from terminal, memory, or network [terminal]? ");
			return;
		}

		if ("terminal".startsWith(nextWord)) {
			// zmen rovnou do CONFIGURE_MODE
			parser.changeMode(CISCO_CONFIG_MODE);
			return;
		}

		if ("memory".startsWith(nextWord) || "network".startsWith(nextWord) || "replace".startsWith(nextWord) || "?".startsWith(nextWord)) {
			parser.printSimulatorInfo("only supported keyword by this version of simulator is \"terminal\"");
			return;
		}

		printLine("?Must be \"terminal\", \"replace\", \"memory\" or \"network\"");
	}

	@Override
	public void catchUserInput(String nextWord) {
		parser.runningCommand = null;
		if ("terminal".startsWith(nextWord) || nextWord.isEmpty()) {
			// zmen rovnou do CONFIGURE_MODE
			parser.changeMode(CISCO_CONFIG_MODE);
			return;
		}

		if ("memory".startsWith(nextWord) || "network".startsWith(nextWord) || "?".startsWith(nextWord)) {
			printLine("?Must be \"terminal\"");
			parser.printSimulatorInfo("only supported keyword \"terminal\"");
			return;
		}

		parser.invalidInputDetected();
	}
}
