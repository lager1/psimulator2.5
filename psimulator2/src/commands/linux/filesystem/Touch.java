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
		if (files.isEmpty()) {
			missingOperand();
		}
	}

	/**
	 * TODO: Soubory k vytvoreni jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		String currentDir = parser.getShell().getPrompt().getCurrentPath() + "/";

		for (String fileName : files) {

			String resolvedPath;

			if (fileName.startsWith("/")) // absolute resolving
			{
				resolvedPath = fileName;
			} else {
				resolvedPath = currentDir + fileName;
			}

			try {
				if ((!parser.device.getFilesystem().createNewFile(resolvedPath) && !parser.device.getFilesystem().exists(resolvedPath))) // if new file was not created and doesnt already exist
				{
					this.parser.getShell().printLine("touch: " + resolvedPath + " touching file failed");
				}
			} catch (FileNotFoundException ex) {
				this.parser.getShell().printLine("touch: " + resolvedPath + " touching file failed. Parent directory doesnt exist");
			}
		}

	}
}
