/*
 * Erstellt am 18.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.FileNotFoundException;

/**
 *
 * @author Tomas Pitrinec
 */
public class Mv extends MvOrCp {

	public Mv(AbstractCommandParser parser) {
		super(parser, "mv");
	}

	@Override
	protected int processFile(String source, String target) {
		try {
			parser.device.getFilesystem().mv(source, target);
			
			return 0;
		} catch (FileNotFoundException ex) {
			printLine("mv: "+ source + " to " + target + "failed. Directory or file doesnt exist" );
			return -1;
		}
	
	}

	



}
