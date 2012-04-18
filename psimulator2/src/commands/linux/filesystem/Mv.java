/*
 * Erstellt am 18.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Mv extends MvOrCp {

	public Mv(AbstractCommandParser parser) {
		super(parser, "mv");
	}

	@Override
	protected void processFile() {
		throw new UnsupportedOperationException("Not supported yet.");
	}



}
