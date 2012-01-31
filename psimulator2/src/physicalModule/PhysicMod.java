/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import networkModule.NetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

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
	private final List<Item> receiveBuffer = Collections.synchronizedList(new LinkedList<Item>());
	/**
	 * Queue for incomming packets from network module.
	 */
	private final List<Item> sendBuffer = Collections.synchronizedList(new LinkedList<Item>());
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
		receiveBuffer.add(new Item(packet, iface));
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
		sendBuffer.add(new Item(packet, iface));
		worker.wake();
	}

	public void doMyWork() {

		while (!receiveBuffer.isEmpty() || !sendBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				Item m = receiveBuffer.remove(0);
				netMod.receivePacket(m.packet, m.iface);
			}

			if (!sendBuffer.isEmpty()) {
				Item m = sendBuffer.remove(0);
				m.iface.sendPacket(m.packet);
			}
		}
	}

	public void addInterface(Switchport iface) {
		interfaceList.add(iface);
	}

	public boolean removeInterface(Switchport iface) {
		return interfaceList.remove(iface);
	}
}
