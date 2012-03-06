/*
 * Erstellt am 6.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Ifconfig extends AbstractCommand{

	public Ifconfig(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void catchUserInput(String input) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
