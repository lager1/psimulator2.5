/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.dataStructures.jobs.InputFileJob;
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

	/**
	 * TODO: Soubory k vypsani jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		for (String fileName : files) {
			parser.device.getFilesystem().runInputFileJob(fileName, new InputFileJob() {

				@Override
				public int workOnFile(InputStream input) throws Exception {
					Scanner sc = new Scanner(input);

					while (sc.hasNextLine()) {
						parser.getShell().printLine(sc.nextLine());
					}

					return 0;
				}
			});
		}




	}
}
