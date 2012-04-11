/*
 * Erstellt am 11.4.2012.
 */

package commands.linux;

import commands.AbstractCommandParser;

/**
 * Navrh mkdir. Musime se dohodnout.
 * @author Tomas Pitrinec
 */
public class Mkdir extends FileSystemCommand {

	public Mkdir(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		while(! nextWordPeek().equals("")){
			createDirectory(nextWord());
		}
	}

	private void createDirectory(String path){

	}

}
