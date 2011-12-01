/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import networkModule.NetworkModule;
import psimulator2.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicalModule extends WorkerThread {

	private static class Item {
		L2Packet packet;
		AbstractInterface iface;

		public Item(L2Packet packet, AbstractInterface iface) {
			this.packet = packet;
			this.iface = iface;
		}
	}

	private List<AbstractInterface> interfaceList;
	private NetworkModule networkModule;

	private final Queue<Item> inputFromCabels;
	private final Queue<Item> inputFromNetworkModule;

	/**
	 * Adds incoming packet from cabel to the buffer.
	 * Sychronized via buffer.
	 * @param packet
	 */
	public void addIncomingPacketToBuffer(L2Packet packet, AbstractInterface iface) {
		synchronized (inputFromCabels) {
			inputFromCabels.add(new Item(packet, iface));
		}
	}

	/**
	 * Return true if empty.
	 * synchronized via buffer
	 * @return
	 */
	public boolean isBufferFromCabelEmpty() {
		synchronized (inputFromCabels) {
			if (inputFromCabels.isEmpty()) return true;
		}
		return false;
	}

	/**
	 * Return true if empty.
	 * synchronized via buffer
	 * @return
	 */
	public boolean isBufferFromNetworkModuleEmpty() {
		synchronized (inputFromNetworkModule) {
			if (inputFromNetworkModule.isEmpty()) return true;
		}
		return false;
	}

	public PhysicalModule(NetworkModule networkModule, List<AbstractInterface> ifaces) {
		this.networkModule = networkModule;
		this.interfaceList = ifaces;
		this.inputFromCabels = new LinkedList<Item>();
		this.inputFromNetworkModule = new LinkedList<Item>();
	}

	public PhysicalModule(NetworkModule networkModule) {
		this.networkModule = networkModule;
		this.interfaceList = new ArrayList<AbstractInterface>();
		this.inputFromCabels = new LinkedList<Item>();
		this.inputFromNetworkModule = new LinkedList<Item>();
	}

	public void addInterface(AbstractInterface iface) {
		interfaceList.add(iface);
	}

	public boolean removeInterface(AbstractInterface iface) {
		return interfaceList.remove(iface);
	}

	@Override
	protected void doMyWork() { // TODO: dopsat obsluhu prichozich paketu - jen jedno kolecko nebo cyklus?

		while(!isBufferFromCabelEmpty()) {
			synchronized(inputFromCabels) {
				Item m = inputFromCabels.poll();
				networkModule.acceptPacket(m.packet, m.iface);
			}
		}

		throw new UnsupportedOperationException("Not implemented completaly yet.");
	}
}
