/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.linux.filesystem.FileSystemCommand;
import commands.AbstractCommandParser;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.NodesWrapper;
import filesystem.exceptions.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Linux command ls. Supported options are -a, -l.
 *
 * @author Tomas Pitrinec
 */
public class Ls extends FileSystemCommand {

	boolean opt_a = false;
	boolean opt_l = false;

	public Ls(AbstractCommandParser parser) {
		super(parser, "ls");
	}

	@Override
	protected void parseOption(char c) {
		if (c == 'a') {
			opt_a = true;
		} else if (c == 'l') {
			opt_l = true;
		} else {
			invalidOption(c);
		}
	}

	/**
	 * TODO: Soubory a slozky, ktery se maj vypsat jsou v promenny files.
	 */
	@Override
	protected void executeCommand() {

		if (opt_a || opt_l) {
			parser.getShell().printLine("Sorry unimplemented funcionality");
			return;
		}


		for (String filePath : files) {
			try {
				NodesWrapper nodes = parser.device.getFilesystem().listDir(filePath);

				for (Node node : nodes.getNodesSortedByTypeAndName()) {
					parser.getShell().printLine(node.toString());
				}

			} catch (FileNotFoundException ex) {
				parser.getShell().printLine("ls: " + filePath + " directory not found");
			}
		}

	}

	@Override
	protected void controlComand() {
		// nothing to control
	}
}
