/*
 * Erstellt am 18.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Cp  extends MvOrCp {

	public Cp(AbstractCommandParser parser) {
		super(parser, "cp");
	}

	@Override
	protected void processFile() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
