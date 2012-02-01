/*
 * Erstellt am 27.10.2011.
 * TODO: implementovat
 */

package networkModule;

import dataStructures.L2Packet;
import device.AbstractDevice;
import networkModule.L2.EthernetLayer;
import physicalModule.PhysicMod;
import physicalModule.Switchport;

/**
 * Implementation of network module of generic simple switch.
 * @author neiss
 */
public class SimpleSwitchNetMod extends NetMod  {
	
	protected EthernetLayer linkLayer;

    public SimpleSwitchNetMod(EthernetLayer linkLayer, AbstractDevice device, PhysicMod physicMod) {
		super(device, physicMod);
		this.linkLayer = linkLayer;
	}

	/**
	 * Prijimani od fysickyho modulu.
	 * @param packet
	 * @param switchportNumber 
	 */
	@Override
	public void receivePacket(L2Packet packet, int switchportNumber) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
