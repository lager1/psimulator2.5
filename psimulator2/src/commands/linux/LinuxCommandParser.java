/*
 * created 5.3.2012
 */
package commands.linux;

import commands.linux.filesystem.Touch;
import commands.linux.filesystem.Rm;
import commands.linux.filesystem.Pwd;
import commands.linux.filesystem.Mkdir;
import commands.linux.filesystem.Ls;
import commands.linux.filesystem.*;
import commands.AbstractCommand;
import commands.AbstractCommandParser;
import commands.LongTermCommand.Signal;
import commands.Rnetconn;
import commands.cisco.CiscoCommand;
import commands.completer.Node;
import device.Device;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import logging.*;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import networkModule.NetMod;
import networkModule.TcpIpNetMod;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

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
		shell.getPrompt().setPrefix(device.getName()+":");
		shell.getPrompt().setCurrentPath("/");
		shell.getPrompt().showPath(true);
		shell.getPrompt().setSuffix("#");

		if (device.commandCompleters == null) {
			device.commandCompleters = new HashMap<>();
			addCompleters();
			addCompletionData();
		}
	}



	/**
	 * Metoda registrije spustitelny prikazy.
	 */
	private void registerCommands() {
		commands.put("ifconfig", Ifconfig.class);
		commands.put("exit", Exit.class);
		commands.put("route", Route.class);
		commands.put("ping", Ping.class);
//		commands.put("cping", PingCommand.class);	// zatim si pridavam cisco ping
		commands.put("ip", Ip.class);
		commands.put("help", Help.class);
		commands.put("man", Man.class);
		commands.put("traceroute", Traceroute.class);
		commands.put("iptables", Iptables.class);
		commands.put("rnetconn", Rnetconn.class);
		commands.put("service", Service.class);

		// prace s filesystemem:
		commands.put("cat", Cat.class);
		commands.put("cd", Cd.class);
		commands.put("ls", Ls.class);
		commands.put("mkdir", Mkdir.class);
		commands.put("pwd", Pwd.class);
		commands.put("rm", Rm.class);
		commands.put("touch", Touch.class);
		commands.put("editor",Editor.class); commands.put("mcedit",Editor.class);

	}


// verejny metody konkretniho parseru: --------------------------------------------------------------------------------

	@Override
	public void catchSignal(Signal sig) {
		if(sig==Signal.CTRL_C){
			Logger.log(this,Logger.DEBUG,LoggingCategory.LINUX_COMMANDS,"Dostal jsem signal ctrl+C, vykonava me vlakno "+Util.threadName(),null);
			shell.printLine("^C");
			if(runningCommand != null){
				runningCommand.catchSignal(sig);
			}
		}
		// zadny dalsi signaly nepotrebuju
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
					command.run();
				} else {
					shell.printLine("bash: " + commandName + ": command not found");
				}

			}

		} catch (Exception ex) {
			log(Logger.WARNING, "Some error in linux commands.", null);
			log(Logger.DEBUG, "Byla vyhozena vyjimka.", ex);
		}
		//log(Logger.DEBUG,"konec metody processLineForParsers",null);

	}

	@Override
	public String getDescription() {
		return device.getName()+": LinuxCommandParser";
	}


// privatni metody: ---------------------------------------------------------------------------------------------------

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
		} else if (CiscoCommand.class.isAssignableFrom(tridaPrikazu)) {
			log(Logger.WARNING, "Na linuxu se pokousite zavolat ciscovej prikaz, coz nejde, protoze ten ocekava cisco parser prikazu.", null);
			return null;
		} else {

			try {
				Class[] ctorArgs1 = new Class[1];
				ctorArgs1[0] = AbstractCommandParser.class;
				Constructor konstruktor = tridaPrikazu.getConstructor(ctorArgs1);
				//log(0, "Mam konstruktor pro nazev prikazu " + name, null);
				Object novaInstance = konstruktor.newInstance((AbstractCommandParser) this);
				//log(0, "Mam i vytvorenou novou instanci.", null);
				return (AbstractCommand) novaInstance;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				log(Logger.WARNING, "Chyba privytvareni instance prikazu " + name, ex);
				return null;
			}
		}


	}

	private void log(int logLevel, String message, Object obj){
		Logger.log(this, logLevel, LoggingCategory.LINUX_COMMANDS, message, obj);
	}

	@Override
	protected final void addCompleters() {
		device.commandCompleters.put(CommandShell.DEFAULT_MODE, new LinuxCompleter());
	}

	@Override
	protected final void addCompletionData() {
		Iterator<String> it = commands.keySet().iterator();
		while (it.hasNext()) {
			String cmd = it.next();

			if (cmd.equals("ifconfig")) {
				Node ifconfig = new Node("ifconfig");

				NetMod nm = device.getNetworkModule();
				IPLayer ipLayer = ((TcpIpNetMod) nm).ipLayer;

				for (NetworkInterface iface : ipLayer.getSortedNetworkIfaces()) {
					ifconfig.addChild(new Node(iface.name));
				}
				device.commandCompleters.get(CommandShell.DEFAULT_MODE).addCommand(ifconfig);
			} else {
				device.commandCompleters.get(CommandShell.DEFAULT_MODE).addCommand(cmd);
			}
		}
	}
}
