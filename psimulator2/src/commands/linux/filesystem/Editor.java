/*
 * Erstellt am 16.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Editor extends FileSystemCommand {

	public Editor(AbstractCommandParser parser) {
		super(parser, "editor");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	// TODO implementovat
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}






}
