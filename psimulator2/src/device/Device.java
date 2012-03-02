/*
 * Erstellt am 27.10.2011.
 */
package device;

import physicalModule.PhysicMod;
import networkModule.NetMod;

/**
 *
 * @author neiss
 */
public class Device {

	public final int id;	// id z konfiguraku
	String name;
	public final DeviceType type;
	public final PhysicMod physicalModule;
	private NetMod networkModule;
	ApplicationsList applications;
	
	private boolean networkModuleSet = false;

	/**
	 * Konstruktor. Nastavi zadany promenny, vytvori si fysickej modul.
	 *
	 * @param id
	 * @param name
	 * @param type 	 *
	 *
	 */
	public Device(int id, String name, DeviceType type) {
		this.id = id;
		this.name = name;
		this.type = type;
		physicalModule = new PhysicMod(this);
	}

	public ApplicationsList getApplications() {
		return applications;
	}

	public String getName() {
		return name;
	}

	public NetMod getNetworkModule() {
		return networkModule;
	}

	public void setNetworkModule(NetMod networkModule) {
		if(!networkModuleSet){
			this.networkModule = networkModule;
			networkModuleSet=true;
		} else throw new RuntimeException("Tohle by nemelo nastat, kontaktujte tvurce softwaru.");
		
	}
	
	

	public enum DeviceType {

		cisco_router,
		linux_computer,
		simple_switch
	}
}
