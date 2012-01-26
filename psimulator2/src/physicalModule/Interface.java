/*
 * created 24.1.2012
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.LinkedList;
import java.util.Queue;
import networkModule.NetMod;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Interface extends AbstractInterface {

	public Interface(String name, Cabel cabel, PhysicMod physicMod) {
		super(name, cabel, physicMod);
	}

	public Interface(String name, PhysicMod physicMod) {
		super(name, physicMod);
	}
	/**
	 * Storage for packets to be sent.
	 */
	private Queue<L2Packet> buffer = new LinkedList<L2Packet>();
	/**
	 * Current size of buffer in bytes.
	 */
	private int size = 0;
	/**
	 * Capacity of buffer in bytes.
	 */
	private int capacity = 150000; // zatim: 100 x max velikost ethernetovyho pakatu
	/**
	 * Count of dropped packets.
	 *
	 * @param packet
	 * @return
	 */
	private int dropped = 0;

	@Override
	public void sendPacket(L2Packet packet) {
		int packetSize = packet.getSize();
		if (size + packetSize > capacity) { // drop packet, run out of capacity
			dropped++;
		} else {
			size += packetSize;
			synchronized (buffer) {
				buffer.add(packet);
			}
			cabel.worker.wake();
		}
	}

	/**
	 * Receives packet from cabel and pass it to physical module.
	 */
	@Override
	public void receivePacket(L2Packet packet) {
		physicMod.receivePacket(packet, this);
	}

	/**
	 * Removes packet form buffer and returns it, decrements size of buffer. Synchronised via buffer. Throws exception when this method
	 * is called and no packet is in buffer.
	 *
	 * @return
	 */
	public L2Packet popPacket() {
		L2Packet packet;
		synchronized (buffer) {
			packet = buffer.remove();
		}
		size -= packet.getSize();
		return packet;
	}

	/**
	 * Return true if buffer is empty.
	 * Synchronied via buffer.
	 */
	public boolean isEmptyBuffer() {
		synchronized (buffer) {
			if (buffer.isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
