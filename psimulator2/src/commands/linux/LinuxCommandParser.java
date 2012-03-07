/*
 * created 5.3.2012
 */
package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import device.Device;
import java.util.ArrayList;
import java.util.List;
import logging.*;
import logging.LoggingCategory;
import psimulator2.Psimulator;
import shell.apps.CommandShell.CommandShell;
import utils.Other;

/**
 *
 * @author Tomáš Pitřinec
 */
public class LinuxCommandParser extends AbstractCommandParser implements Loggable{

	private List<String> commands = new ArrayList<>();

	public LinuxCommandParser(Device networkDevice, CommandShell shell) {
		super(networkDevice, shell);
		registerCommands();
		shell.prompt=device.getName()+": ~# ";
	}

	/**
	 * Metoda by mela na zacatku zaregistrovat spustitelny prikazy, zatim ale jeste nevim jak, tak tady plnim list
	 * stringu a rozhodovani o spusteni prikazu mam v metode getLinuxCommand.
	 */
	private void registerCommands() {
		commands.add("ifconfig");
		commands.add("exit");
	}

	@Override
	public void catchSignal(int sig) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String[] getCommands(int mode) {
		return (String[]) commands.toArray();
	}

	/**
	 * Tady se predevsim zpracovava prichozi radek. Chytaj se tu vsechny vyjimky, aby se mohly zalogovat a nesly nikam
	 * dal.
	 */
	@Override
	protected void processLineForParsers() {
		try {	// nechci hazet pripadne hozeny vyjimky dal

			String commandName = nextWordPeek();
			if (!commandName.isEmpty()) {	// kdyz je nejakej prikaz vubec poslanej, nejradsi bych posilani niceho zrusil

				AbstractCommand command = getLinuxCommand(commandName);
				if (command != null) {
					command.run();	// TODO: doresit nastaveni prave spustenyho prikazu a navratovyho kodu
				} else {
					shell.printLine("bash: " + commandName + ": command not found");
				}

			}

		} catch (Exception ex) {
			logDebug(Logger.WARNING, ex.toString() + "\n" + Other.stackToString(ex));
		}
	}

	private AbstractCommand getLinuxCommand(String name) {
		if (name.equals("ifconfig")) {
			return new Ifconfig(this);
		} else if (name.equals("exit")){
			return new Exit(this);
		} else {
			return null;
		}
	}

	@Override
	public String getDescription() {
		return device.getName()+": LinuxCommandParser: ";
	}


	private void logDebug(int logLevel, String message){
		if (logLevel == 0) {
			logLevel = Logger.DEBUG;
		}
		Logger.log(this, logLevel, LoggingCategory.LINUX_COMMAND_PARSER, message, null);
	}


}
