/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.ipAddresses.IpAddress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import dataStructures.MacAddress;
import networkModule.Layer;
import networkModule.NetMod;
import physicalModule.Switchport;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 *
 * @author neiss
 */
public abstract class L2layer extends Layer implements SmartRunnable {

	protected WorkerThread worker = new WorkerThread(this);

	public L2layer(NetMod netMod) {
		super(netMod);
	}

	/**
	 * Prijima pakety od nizsi vrstvy.
	 */
	public abstract void receivePacket(L2Packet packet, Switchport swport);

	/**
	 * Prijima pakety od vyssi vrstvy.
	 */
	public abstract void sendPacket(L3Packet packet, L2Interface iface, MacAddress target);
}
