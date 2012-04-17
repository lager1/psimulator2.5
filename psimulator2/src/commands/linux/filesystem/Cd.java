/*
 * Erstellt am 12.4.2012.
 */
package commands.linux.filesystem;

import commands.AbstractCommandParser;

/**
 * Linux command cd, no options are supported.
 *
 * @author Tomas Pitrinec
 */
public class Cd extends FileSystemCommand {

	public Cd(AbstractCommandParser parser) {
		super(parser, "cd");
	}

	@Override
	protected void parseOption(char c) {
		invalidOption(c);
	}

	/**
	 * TODO implementovat. Prejdi do prvni slozky ktera je ulozena v seznamu files.
	 */
	@Override
	protected void executeCommand() {

		if (files.isEmpty()) // set prompt to the root
		{
			parser.getShell().getPrompt().setCurrentPath("/");
			return;
		}

		StringBuilder processPath = new StringBuilder(files.get(0));

		StringBuilder pathToSet;

		if (processPath.toString().startsWith("/")) // absolute path
		{
			pathToSet = processPath;
		} else {
			pathToSet = new StringBuilder(parser.getShell().getPrompt().getCurrentPath());

			if (!pathToSet.toString().endsWith("/")) {
				pathToSet.append("/");
			}

			pathToSet.append(processPath);
		}

//		boolean processed = false;
//
//		while (processPath.toString().startsWith(".")) {  // resolving relative path
//
//			processed = true;
//
//			if (processPath.toString().startsWith("./")) // path to set is not changed
//			{
//				processPath.delete(0, 2);  // aka delete "./"
//				continue;
//			} else if (processPath.toString().startsWith("../")) // 
//			{
//				processPath.delete(0, 3);  // aka delete "../"
//
//				int lastPathDelimiter = pathToSet.lastIndexOf("/");
//				pathToSet.delete(lastPathDelimiter, pathToSet.length());  // skip into parent directory
//
//				if (pathToSet.length() < 2) {  // if path is in root of filesystem => no skipping
//					pathToSet = new StringBuilder("/");
//				}
//				continue;
//			}
//
//			break;
//		}
//
//		if (processed) {
//			pathToSet.append(processPath);  // append what left after processing path
//		}else{
//			pathToSet = processPath;  // if no processing was made => absolute path was entered
//		}

		String normalizedPath;

		if (pathToSet.toString().contentEquals("/../")) {
			return;
		}

		normalizedPath = parser.device.getFilesystem().normalize(pathToSet.toString());

		if (normalizedPath != null && parser.device.getFilesystem().isDir(normalizedPath)) // if path is a directory => ok
		{
			parser.getShell().getPrompt().setCurrentPath(normalizedPath);
		} else { // not ok
			parser.getShell().printLine("cd: " + pathToSet.toString() + " directory not found");
		}
	}

	@Override
	protected void controlComand() {
		// nothing to control
	}
}
