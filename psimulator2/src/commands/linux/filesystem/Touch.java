/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.linux.filesystem.FileSystemCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Touch extends FileSystemCommand {

	public Touch(AbstractCommandParser parser) {
		super(parser, "touch");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	/**
	 * TODO: Soubory k vytvoreni jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}



}
