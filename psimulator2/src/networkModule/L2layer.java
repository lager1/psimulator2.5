/*
 * Erstellt am 27.10.2011.
 */
package networkModule;

import dataStructures.ipAdresses.IpAdress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import physicalModule.AbstractInterface;
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
	public abstract void receivePacket(L2Packet packet, AbstractInterface iface);

	/**
	 * Prijima pakety od vyssi vrstvy.
	 */
	public abstract void acceptPacket(L3Packet packet, AbstractInterface iface, IpAdress nextHop);
}
