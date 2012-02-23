/*
 * Erstellt am 22.2.2012.
 */
package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import networkModule.L2.EthernetInterface;

/**
 * Representation of network interface in 3th layer in Network Module.
 * Representuje prakticky jen nastaveni interfacu, samotna trida nic nedela.
 * @author neiss
 */
public class NetworkIface {

	public final String name;
	public boolean isUp = true; //je nahozene
	public IPwithNetmask ipAddress;
	public final EthernetInterface ethernetInterface;

	public NetworkIface(String name, EthernetInterface iface) {
		this.name = name;
		this.ethernetInterface = iface;
	}
}
