/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Linux command cat. No options are supported.
 *
 * @author Tomas Pitrinec
 */
public class Cat extends FileSystemCommand {

	public Cat(AbstractCommandParser parser) {
		super(parser, "cat");
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
	 * TODO: Soubory k vypsani jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		String currentDir = parser.getShell().getPrompt().getCurrentPath() + "/";

		for (String fileName : files) {
			try {

				String absFile = parser.device.getFilesystem().resolveAbsolutePath(currentDir + fileName);
				
				if(absFile==null)
					throw new FileNotFoundException();

				parser.device.getFilesystem().runInputFileJob(absFile, new InputFileJob() {

					@Override
					public int workOnFile(InputStream input) throws Exception {
						Scanner sc = new Scanner(input);

						while (sc.hasNextLine()) {
							parser.getShell().printLine(sc.nextLine());
						}

						return 0;
					}
				});
			} catch (FileNotFoundException ex) {
				parser.getShell().printLine("cat: " + currentDir+fileName + ": file not found");
			}
		}

	}
}
