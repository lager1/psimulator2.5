/*
 * created 5.3.2012
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import device.Device;
import java.util.ArrayList;
import java.util.List;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Tomáš Pitřinec
 */
public class LinuxCommandParser extends AbstractCommandParser {

	private List<String> commands = new ArrayList<>();

	public LinuxCommandParser(Device networkDevice, CommandShell shell) {
		super(networkDevice, shell);
		registerCommands();
	}

	/**
	 * Metoda by mela na zacatku zaregistrovat spustitelny prikazy, zatim ale jeste nevim jak, tak tady plnim list
	 * stringu a rozhodovani o spusteni prikazu mam v metode getLinuxCommand.
	 */
	private void registerCommands() {
		commands.add("ifconfig");
	}

	@Override
	public void catchSignal(int sig) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String[] getCommands(int mode) {
		return (String[]) commands.toArray();
	}

	@Override
	protected void processLineForParsers() {
		AbstractCommand command = getLinuxCommand();
		if(command != null){
			command.run();
		}

	}

	private AbstractCommand getLinuxCommand() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
