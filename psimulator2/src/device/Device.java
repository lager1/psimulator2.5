/*
 * Erstellt am 27.10.2011.
 */
package device;

import applications.Application;
import commands.AbstractCommandParser;
import commands.cisco.CiscoCommandParser;
import commands.completer.Completer;
import commands.linux.LinuxCommandParser;
import filesystem.FileSystem;
import java.util.HashMap;
import java.util.Map;
import networkModule.NetMod;
import physicalModule.PhysicMod;
import shell.apps.CommandShell.CommandShell;
import telnetd.pridaneTridy.TelnetProperties;

/**
 *
 * @author neiss
 */
public class Device {

	public final int configID;	// configID z konfiguraku
	private String name;
	public final DeviceType type;
	public final PhysicMod physicalModule;
	private NetMod networkModule;
	/**
	 * Completers for all available modes.
	 *
	 * Key - mode number - static modes from CommandShell<br />
	 * Value - Completer
	 */
	public Map<Integer, Completer> commandCompleters; // schvalne neinicializovano! vytvari se az v parserech
	/**
	 * List of running network applications. <br />
	 *
	 * Key - Application PID <br />
	 * Value - Application
	 */
	Map<Integer, Application> applications;
	private int pidCounter = 1;

	/**
	 * telnet port is configured by TelnetProperties.addListerner method, which is called in constructor
	 * telnetPort is allocated when simulator is started. There is no need to store it in file.
	 */
	private transient int telnetPort = -1;

	private FileSystem filesystem;
	/**
	 * Konstruktor. Nastavi zadany promenny, vytvori si fysickej modul.
	 *
	 * @param configID
	 * @param name
	 * @param type
	 *
	 */
	public Device(int configID, String name, DeviceType type) {
		this.configID = configID;
		this.name = name;
		this.type = type;
		physicalModule = new PhysicMod(this);
		// telnetovy pripojeni se pridava jen kdyz to neni switch
		if(type!=DeviceType.simple_switch){
			TelnetProperties.addListener(this);  // telnetPort is configured in this method
		}
		this.applications = new HashMap<>();
	}

	public int getTelnetPort() {
		return telnetPort;
	}

	public void setTelnetPort(int telnetPort) {
		this.telnetPort = telnetPort;
	}

	public FileSystem getFilesystem() {
		return filesystem;
	}

	public void setFilesystem(FileSystem filesystem) {
		this.filesystem = filesystem;
	}



	public String getName() {
		return name;
	}

	public NetMod getNetworkModule() {
		return networkModule;
	}

	/**
	 * Tuto metodu pouzivat jen na zacatku behu programu pri konfiguraci!
	 *
	 * @param networkModule
	 */
	public void setNetworkModule(NetMod networkModule) {
		this.networkModule = networkModule;
	}

	/**
	 * Returns free PID for new applications.
	 * @return
	 */
	public int getFreePID() {
		return pidCounter++;
	}

	/**
	 * Register application to list of running applications.
	 * @param app
	 */
	public void registerApplication(Application app) {
		applications.put(app.getPID(), app);
	}

	/**
	 * Unregister application from list of running applications.
	 * @param app
	 */
	public void unregisterApplication(Application app) {
		applications.remove(app.getPID());
	}

	/**
	 * Creates parser according to a DeviceType.
	 * @param cmd
	 * @return
	 */
	public AbstractCommandParser createParser(CommandShell cmd){

		switch (type) {
			case cisco_router:
				return new CiscoCommandParser(this, cmd);

			case linux_computer:
				return new LinuxCommandParser(this, cmd);

			case simple_switch:
				return null; // no parser

			default:
				throw new AssertionError();
		}
	}

	public enum DeviceType {

		cisco_router,
		linux_computer,
		simple_switch
	}
}
