/*
 * Erstellt am 16.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;
import filesystem.exceptions.FileNotFoundException;
import shell.apps.TextEditor.TextEditor;

/**
 *
 * @author Tomas Pitrinec
 */
public class Editor extends FileSystemCommand {

	public Editor(AbstractCommandParser parser) {
		super(parser, "editor");
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

	@Override
	protected void executeCommand() {

		if(files.isEmpty()){
			parser.getShell().printLine("File name needed to run editor");
			return;
		}
		
		String filePath = files.get(0);

		if (filePath.endsWith("/")) {
			parser.getShell().printLine("Cannot run editor on a directory");
			return;
		}

		String currentDir = parser.getShell().getPrompt().getCurrentPath() + "/";


		String resolvedPath;

		if (filePath.startsWith("/")) // absolute resolving
		{
			resolvedPath = filePath;
		} else {
			resolvedPath = currentDir + filePath;
		}

		if (!parser.device.getFilesystem().isFile(resolvedPath)) {  // if path is not existing file
			try {

				if (!parser.device.getFilesystem().createNewFile(resolvedPath)) {  // if file cannot be even created
					parser.getShell().printLine("Cannot create new empty file with your path: " + resolvedPath);
					return;
				}
			} catch (FileNotFoundException ex) {
				parser.getShell().printLine("Cannot create new empty file with your path: " + resolvedPath);
					return;
			}
		}

		
		
		// OK resolved path should be now poining to regular file. time to start texteditor

		TextEditor editor = new TextEditor(parser.getShell().getTerminalIO(), parser.device, resolvedPath);

		try {
			parser.getShell().setChildProcess(editor);
			editor.run();
		} finally {
			parser.getShell().setChildProcess(null);
			//parser.getShell().clearScreen();
		}

	}
}
