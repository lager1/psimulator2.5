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

	@Override
	protected void controlComand() {
		if(files.isEmpty()){
			missingOperand();
		}
	}


	@Override
	protected void executeCommand() {
		// TODO tady to doimplementuj:
		throw new UnsupportedOperationException("Not supported yet.");
	}






}
