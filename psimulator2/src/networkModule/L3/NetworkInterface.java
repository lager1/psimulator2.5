/*
 * Erstellt am 22.2.2012.
 */
package networkModule.L3;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import networkModule.L2.EthernetInterface;

/**
 * Representation of network interface in 3th layer in Network Module.
 * Representuje prakticky jen nastaveni interfacu, samotna trida nic nedela.
 * @author neiss
 */
public class NetworkInterface {

	public final int configID;

	public final String name;
	/**
	 * Interface is Up.
	 * default behavior on linux: true
	 * default behavior on cisco: false
	 */
	public boolean isUp = true;
	protected IPwithNetmask ipAddress;
	public final EthernetInterface ethernetInterface;

	public NetworkInterface(Integer configID, String name, EthernetInterface iface) {
		this.configID = configID;
		this.name = name;
		this.ethernetInterface = iface;
	}

	public NetworkInterface(Integer configID, String name, IPwithNetmask ipAddress, EthernetInterface ethernetInterface, boolean isUp) {
		this.configID = configID;
		this.name = name;
		this.ipAddress = ipAddress;
		this.ethernetInterface = ethernetInterface;
		this.isUp = isUp;
	}

	/**
	 * Getter for IP address with mask.
	 * Setter is not available. Set IP address on IPLayer with method setIpAddressOnInterface()
	 * @return
	 */
	public IPwithNetmask getIpAddress() {
		return ipAddress;
	}

	public MacAddress getMacAddress() {
		return ethernetInterface.getMac();
	}
}
