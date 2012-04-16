/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 * Linux command cd, no options are supported.
 * @author Tomas Pitrinec
 */
public class Cd extends FileSystemCommand {

	public Cd(AbstractCommandParser parser) {
		super(parser, "cd");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	/**
	 * TODO implementovat.
	 * Prejdi do prvni slozky ktera je ulozena v seznamu files.
	 */
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void controlComand() {
		// nothing to control
	}

}
