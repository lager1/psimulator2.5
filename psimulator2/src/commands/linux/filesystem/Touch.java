/*
 * Erstellt am 12.4.2012.
 */

package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.FileNotFoundException;

/**
 *
 * @author Tomas Pitrinec
 */
public class Touch extends FileSystemCommand {

	public Touch(AbstractCommandParser parser) {
		super(parser, "touch");
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

	/**
	 * TODO: Soubory k vytvoreni jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		String currentDir = parser.getShell().getPrompt().getCurrentPath()+"/";
		
		for (String fileName : files) {
			
			String absFile = currentDir+fileName;
			
			try {
				if((!parser.device.getFilesystem().createNewFile(absFile) && !parser.device.getFilesystem().exists(absFile))) // if new file was not created and doesnt already exist
					this.parser.getShell().printLine("touch: " + absFile + " touching file failed");
			} catch (FileNotFoundException ex) {
				this.parser.getShell().printLine("touch: " + absFile + " touching file failed. Parent directory doesnt exist");
			}
		}

	}



}
