/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.ipAddresses.IpAddress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import networkModule.Layer;
import networkModule.NetMod;
import physicalModule.AbstractSwitchport;
import psimulator2.SmartRunnable;
import psimulator2.WorkerThread;

/**
 *
 * @author neiss
 */
public abstract class L2layer extends Layer implements SmartRunnable {

	private WorkerThread worker = new WorkerThread(this);

	public L2layer(NetMod netMod) {
		super(netMod);
	}

	/**
	 * Prijima pakety od nizsi vrstvy.
	 */
	public abstract void receivePacket(L2Packet packet, AbstractSwitchport iface);

	/**
	 * Prijima pakety od vyssi vrstvy.
	 */
	public abstract void sendPacket(L3Packet packet, AbstractSwitchport iface, IpAddress nextHop);
}
