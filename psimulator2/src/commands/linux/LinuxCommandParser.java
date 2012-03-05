/*
 * created 5.3.2012
 */

package commands.linux;

import commands.AbstractCommandParser;
import device.Device;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class LinuxCommandParser extends AbstractCommandParser {

	public LinuxCommandParser(Device networkDevice, CommandShell shell) {
		super(networkDevice, shell);
	}

	@Override
	public void catchUserInput(String userInput) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void catchSignal(int sig) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String[] getCommands(int mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected int processLineForParsers() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
