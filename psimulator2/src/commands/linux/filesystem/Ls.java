/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.linux.filesystem.FileSystemCommand;
import commands.AbstractCommandParser;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.NodesWrapper;
import filesystem.exceptions.FileNotFoundException;

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
		
		if(files.isEmpty()){ // no dir to list => list current directory
			files.add("");
		}

		String currentDir = parser.getShell().getPrompt().getCurrentPath() + "/";

		for (String filePath : files) {
			try {
				
				String resolvedPath;
				
				if(filePath.startsWith("/")) // absolute resolving
					resolvedPath=filePath;
				else{
					resolvedPath=currentDir+filePath;
				}
				
				NodesWrapper nodes = parser.device.getFilesystem().listDir(resolvedPath);

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
