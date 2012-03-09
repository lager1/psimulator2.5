/*
 * created 5.3.2012
 */
package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import commands.cisco.CiscoCommand;
import commands.cisco.PingCommand;
import device.Device;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import logging.*;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Tomáš Pitřinec
 */
public class LinuxCommandParser extends AbstractCommandParser implements Loggable{

	/**
	 * Mapa mezi nazvama prikazu a jejich tridou.
	 */
	private Map<String, Class> commands = new HashMap<>();

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
		commands.put("ifconfig", Ifconfig.class);
		commands.put("exit", Exit.class);
		commands.put("route", Route.class);
		commands.put("ping", Ping.class);
		commands.put("cping", PingCommand.class);	// zatim si pridavam cisco ping
	}

	@Override
	public void catchSignal(Signal sig) {
		shell.printLine("^C");	// TODO dodelat
	}

	@Override
	public String[] getCommands(int mode) {
		return (String[]) commands.values().toArray();
	}

	/**
	 * Tady se predevsim zpracovava prichozi radek. Chytaj se tu vsechny vyjimky, aby se mohly zalogovat a nesly nikam
	 * dal.
	 */
	@Override
	protected void processLineForParsers() {
		try {	// nechci hazet pripadne hozeny vyjimky dal

			String commandName = nextWord();
			if (!commandName.isEmpty()) {	// kdyz je nejakej prikaz vubec poslanej, nejradsi bych posilani niceho zrusil

				AbstractCommand command = getLinuxCommand(commandName);
				if (command != null) {
					command.run();	// TODO: doresit nastaveni prave spustenyho prikazu a navratovyho kodu
				} else {
					shell.printLine("bash: " + commandName + ": command not found");
				}

			}

		} catch (Exception ex) {
			log(Logger.WARNING, "Nejaka chyba v linuxovejch prikazech.", null);
			log(0, "Byla vyhozena vyjimka.", ex);
		}
	}

	/**
	 * Tohle pravdepodobne prijde smazat.
	 * @param name
	 * @return
	 */
	private AbstractCommand getLinuxCommandStara(String name) {
		if (name.equals("ifconfig")) {
			return new Ifconfig(this);
		} else if (name.equals("route")){
			return new Route(this);
		} else if (name.equals("exit")){
			return new Exit(this);
		} else if (name.equals("ping")){
			return new Ping(this);
		} else {
			return null;
		}
	}

	/**
	 * Tahle metoda vrati instanci prikazu podle zadanyho jmena. Je to trochu hack, na druhou stranu se nemusi
	 * registrovat prikaz na dvou mistech.
	 *
	 * @param name
	 * @return
	 */
	private AbstractCommand getLinuxCommand(String name) {

		Class tridaPrikazu = commands.get(name);
		if (tridaPrikazu == null) {
			return null;
		} else if(CiscoCommand.class.isAssignableFrom(tridaPrikazu)) {
			log(Logger.WARNING,"Na linuxu se pokousite zavolat ciscovej prikaz, coz nejde, protoze ten ocekava cisco parser prikazu.", null);
			return null;
		}else{

			try {
				Class[] ctorArgs1 = new Class[1];
				ctorArgs1[0] = AbstractCommandParser.class;
				Constructor konstruktor = tridaPrikazu.getConstructor(ctorArgs1);
				//log(0, "Mam konstruktor pro nazev prikazu " + name, null);
				Object novaInstance = konstruktor.newInstance((AbstractCommandParser) this);
				//log(0, "Mam i vytvorenou novou instanci.", null);
				return (AbstractCommand) novaInstance;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				log(Logger.WARNING, "Chyba privytvareni instance prikazu "+name, ex);
				return null;
			}
		}


	}

	@Override
	public String getDescription() {
		return device.getName()+": LinuxCommandParser";
	}


	private void log(int logLevel, String message, Object obj){
		if (logLevel == 0) {
			logLevel = Logger.DEBUG;
		}
		Logger.log(this, logLevel, LoggingCategory.LINUX_COMMAND_PARSER, message, obj);
	}


}
