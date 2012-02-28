package filesystem;

import commands.AbstractCommandParser;
import java.util.Scanner;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ReplayScriptConfig {

	FileSystem fileSystem;

	public ReplayScriptConfig(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public void replay(String fileName, AbstractCommandParser parser) {

		Scanner in = new Scanner(fileSystem.getInputStreamToFile(fileName));

		while (in.hasNextLine()) {

			String command = in.nextLine();

			if (command.startsWith("#")) // ignore commentary
			{
				continue;
			}

			int cmntPosition = command.indexOf("#");

			command = command.substring(0,cmntPosition).trim();

			if (parser != null) {
				parser.processLine(command, 0);  // @TODO opravdu 0, zeptat se asi Standy?
			} else {
				Logger.log(Logger.WARNING, LoggingCategory.FILE_SYSTEM, "Parser object is null, cannot process command:\""+command + "\"" );
			}
		}


		in.close();  // dont forget to close file

	}
}
