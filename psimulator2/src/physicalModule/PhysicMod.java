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
 * TODO: PhysicMod: pak nejak poresit velikosti bufferu
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicMod implements SmartRunnable {

	private static class Item {

		L2Packet packet;
		Switchport iface;

		public Item(L2Packet packet, Switchport iface) {
			this.packet = packet;
			this.iface = iface;
		}
	}
	/**
	 * List of interfaces.
	 */
	private List<Switchport> interfaceList;
	/**
	 * Network module.
	 */
	private NetMod netMod;
	/**
	 * Queue for incomming packets from cabels.
	 */
	private final Queue<Item> fromCabels = new LinkedList<Item>();
	/**
	 * Queue for incomming packets from network module.
	 */
	private final Queue<Item> fromNetMod = new LinkedList<Item>();
	/**
	 * Working thread.
	 */
	private WorkerThread worker = new WorkerThread(this);

	public PhysicMod(NetMod netMod, List<Switchport> ifaces) {
		this.netMod = netMod;
		this.interfaceList = ifaces;
	}

	public PhysicMod(NetMod netMod) {
		this.netMod = netMod;
		this.interfaceList = new ArrayList<Switchport>();
	}

	/**
	 * Adds incoming packet from cabel to the buffer. Sychronized via buffer. Wakes worker.
	 *
	 * @param packet to receive
	 * @param iface which receives packet
	 */
	public void receivePacket(L2Packet packet, Switchport iface) {
		synchronized (fromCabels) {
			fromCabels.add(new Item(packet, iface));
		}
		worker.wake();
	}

	/**
	 * Adds incoming packet from network module to the buffer and then try to send it via cabel. Sychronized via buffer.
	 * Wakes worker.
	 *
	 * @param packet to send via physical module
	 * @param iface through it will be send
	 */
	public void sendPacket(L2Packet packet, Switchport iface) {
		synchronized (fromNetMod) {
			fromNetMod.add(new Item(packet, iface));
		}
		worker.wake();
	}

	public void doMyWork() {

		while (!isEmptyFromCabel() || !isEmptyFromNetMod()) {
			if (!isEmptyFromCabel()) {
				synchronized (fromCabels) {
					Item m = fromCabels.remove();
					netMod.receivePacket(m.packet, m.iface);
				}
			}

			if (!isEmptyFromNetMod()) {
				synchronized (fromNetMod) {
					Item m = fromNetMod.remove();
					m.iface.sendPacket(m.packet);
				}
			}
		}
	}

	/**
	 * Return true if empty. synchronized via buffer TODO: ma byt synchronizovane?
	 *
	 * @return
	 */
	private boolean isEmptyFromCabel() {
		synchronized (fromCabels) {
			if (fromCabels.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if empty. synchronized via buffer TODO: ma byt synchronizovane?
	 *
	 * @return
	 */
	private boolean isEmptyFromNetMod() {
		synchronized (fromNetMod) {
			if (fromNetMod.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void addInterface(Switchport iface) {
		interfaceList.add(iface);
	}

	public boolean removeInterface(Switchport iface) {
		return interfaceList.remove(iface);
	}
}
