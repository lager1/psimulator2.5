/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.linux.filesystem.FileSystemCommand;
import commands.AbstractCommandParser;

/**
 * Linux command rm. Supported options are: -f -r, -i
 * @author Tomas Pitrinec
 */
public class Rm extends FileSystemCommand {

	// options:

	boolean opt_f = false;
	boolean opt_r = false;
	boolean opt_i = false;


	public Rm(AbstractCommandParser parser) {
		super(parser, "rm");
	}

	@Override
	protected void parseOption(char c){

		if (c == 'f') opt_f = true;
		else if (c == 'r') opt_r = true;
		else if (c == 'R') opt_r = true;
		else if (c == 'i') opt_i = true;
		else invalidOption(c);
	}


	/**
	 * TODO: implementovat.
	 * Soubory a slozky ke smazani jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not supported yet.");
	}





}
