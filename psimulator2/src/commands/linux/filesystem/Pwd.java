/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Pwd  extends FileSystemCommand {

	public Pwd(AbstractCommandParser parser) {
		super(parser, "pwd");
	}


	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	@Override
	protected void controlComand() {
		// nothing to control
	}


}
