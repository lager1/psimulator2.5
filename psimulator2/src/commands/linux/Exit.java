/*
 * Erstellt am 7.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Exit extends AbstractCommand{

	public Exit(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		parser.getShell().closeSession();
	}

	@Override
	public void catchUserInput(String input) {

	}

}
