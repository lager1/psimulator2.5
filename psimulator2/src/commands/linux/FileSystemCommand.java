/*
 * Erstellt am 11.4.2012.
 */

package commands.linux;

import commands.AbstractCommandParser;

/**
 * Spolecna trida pro jednoduchy prikazy pracujici se souborovym systemem.
 * Jenom navrh!
 * @author Tomas Pitrinec
 */
public abstract class FileSystemCommand extends LinuxCommand {


	public FileSystemCommand(AbstractCommandParser parser) {
		super(parser);
	}


	/**
	 * Najde zadanou cestu. TODO: implementovat
	 * @return
	 */
	private boolean findPatch() {
		throw new UnsupportedOperationException("Not yet implemented");
	}


}
