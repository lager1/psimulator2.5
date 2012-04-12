/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.linux.filesystem.FileSystemCommand;
import commands.AbstractCommandParser;

/**
 * Linux command ls. Supported options are -a, -l.
 * @author Tomas Pitrinec
 */
public class Ls extends FileSystemCommand {

	boolean opt_a = false;
	boolean opt_l = false;

	public Ls(AbstractCommandParser parser) {
		super(parser, "ls");
	}

	@Override
	protected void parseOption(char c) {
		if (c == 'a') {
			opt_a = true;
		} else if (c == 'l') {
			opt_l = true;
		} else {
			invalidOption(c);
		}
	}

	/**
	 * TODO: Soubory a slozky, ktery se maj vypsat jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}







}
