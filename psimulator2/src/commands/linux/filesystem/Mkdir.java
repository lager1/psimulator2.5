/*
 * Erstellt am 11.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 * Command mkdir. No parameters are supported.
 * @author Tomas Pitrinec
 */
public class Mkdir extends FileSystemCommand {

// promenny parseru:



	public Mkdir(AbstractCommandParser parser) {
		super(parser, "mkdir");
	}


	@Override
	protected void parseOption(char c) {
		// zadny prepinace nepodporuju

		invalidOption(c);

	}

	@Override
	protected void controlComand() {
		if(files.isEmpty()){
			missingOperand();
		}
	}


	/**
	 * TODO: Tohle Martine implementuj. Vytvor vsechny adresare zadany ve files.
	 */
	@Override
	protected void executeCommand() {
		throw new UnsupportedOperationException("Not yet implemented");
	}



}
