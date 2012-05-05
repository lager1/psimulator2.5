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
public class Cp  extends MvOrCp {

	public Cp(AbstractCommandParser parser) {
		super(parser, "cp");
	}

	@Override
	protected int processFile(String source, String target) {

			try {
			parser.device.getFilesystem().cp_r(source, target);
			
			return 0;
		} catch (FileNotFoundException ex) {
			printLine("cp: "+ source + " to " + target + "failed. Directory or file doesnt exist" );
			return -1;
		}
	
		
		
	}
}
