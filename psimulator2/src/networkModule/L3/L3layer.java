/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L3;

import dataStructures.L2Packet;
import dataStructures.L4Packet;
import networkModule.Layer;
import networkModule.NetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * TODO: tohle asi prijde smazat nebo prejmenovat
 * @author neiss
 */
public abstract class L3layer extends Layer implements SmartRunnable {

	protected WorkerThread worker = new WorkerThread(this);

	public L3layer(NetMod netMod) {
		super(netMod);
	}

	/**
	 * Prijima pakety od nizsi vrstvy.
	 * Typ IP nebo ARP
	 */
	public abstract void receivePacket(L2Packet packet);

	/**
	 * Prijima pakety od vyssi vrstvy.
	 */
	public abstract void sendPacket(L4Packet packet);
}
