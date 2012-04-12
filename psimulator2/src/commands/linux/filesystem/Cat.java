/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 * Linux command cat. No options are supported.
 * @author Tomas Pitrinec
 */
public class Cat extends FileSystemCommand {

	public Cat(AbstractCommandParser parser) {
		super(parser, "cat");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	/**
	 * TODO: Soubory k vypsani jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}





}
