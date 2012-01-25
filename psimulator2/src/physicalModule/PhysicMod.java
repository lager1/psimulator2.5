/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import networkModule.NetMod;
import psimulator2.SmartRunnable;
import psimulator2.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicMod implements SmartRunnable {

	private static class Item {

		L2Packet packet;
		AbstractInterface iface;

		public Item(L2Packet packet, AbstractInterface iface) {
			this.packet = packet;
			this.iface = iface;
		}
	}
	private List<AbstractInterface> interfaceList;
	private NetMod netMod;
	private final Queue<Item> fromCabels = new LinkedList<Item>();
	private final Queue<Item> fromNetMod = new LinkedList<Item>();
	private WorkerThread worker = new WorkerThread(this);

	public PhysicMod(NetMod networkModule, List<AbstractInterface> ifaces) {
		this.netMod = networkModule;
		this.interfaceList = ifaces;
	}

	public PhysicMod(NetMod networkModule) {
		this.netMod = networkModule;
		this.interfaceList = new ArrayList<AbstractInterface>();
	}

	/**
	 * Adds incoming packet from cabel to the buffer. Sychronized via buffer.
	 *
	 * @param packet to receive
	 * @param iface which receives packet
	 */
	public void receivePacket(L2Packet packet, AbstractInterface iface) {
		// TODO: wake
		synchronized (fromCabels) {
			fromCabels.add(new Item(packet, iface));
		}
	}

	/**
	 * Adds incoming packet from network module to the buffer and then try to send it via cabel. Sychronized via buffer.
	 *
	 * @param packet to send via physical module
	 * @param iface through it will be send
	 */
	public void sendPacket(L2Packet packet, AbstractInterface iface) {
		// TODO: wake
		synchronized (fromNetMod) {
			fromNetMod.add(new Item(packet, iface));
		}
	}

	/**
	 * Return true if empty. synchronized via buffer
	 *
	 * @return
	 */
	private boolean isBufferFromCabelEmpty() {
		synchronized (fromCabels) {
			if (fromCabels.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if empty. synchronized via buffer
	 *
	 * @return
	 */
	private boolean isBufferFromNetworkModuleEmpty() {
		synchronized (fromNetMod) {
			if (fromNetMod.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void addInterface(AbstractInterface iface) {
		interfaceList.add(iface);
	}

	public boolean removeInterface(AbstractInterface iface) {
		return interfaceList.remove(iface);
	}

	public void doMyWork() { // TODO: dopsat obsluhu prichozich paketu - jen jedno kolecko nebo cyklus?


		// dokud neco bude tak makam
		// osetrit null
		// nez skoncim, tak kontroluju, zda tam neco je

		synchronized (fromCabels) {
			Item m = fromCabels.poll(); // TODO null
			netMod.acceptPacket(m.packet, m.iface);
		}

		synchronized (fromNetMod) {
			Item m = fromNetMod.poll();
			m.iface.sendPacket(m.packet);
		}



//		while(!isBufferFromCabelEmpty()) {
//			synchronized(fromCabels) {
//				Item m = fromCabels.poll();
//				netMod.acceptPacket(m.packet, m.iface);
//			}
//		}
//
		throw new UnsupportedOperationException("Not implemented completaly yet.");
	}
}
