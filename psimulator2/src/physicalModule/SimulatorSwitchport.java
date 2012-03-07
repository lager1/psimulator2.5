/*
 * created 24.1.2012
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents "interface" on L2.
 * For switchport connected to real network implement your own class.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class SimulatorSwitchport extends Switchport {

//	/**
//	 * Link to cable's connector.
//	 * Until cable is not connected ti is null.
//	 */
//	protected Connector connector;

	protected Cable cabel;

	/**
	 * Storage for packets to be sent.
	 */
	private final List<L2Packet> buffer = Collections.synchronizedList(new LinkedList<L2Packet>());

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


	public SimulatorSwitchport(PhysicMod physicMod, int number, int configID) {
		super(physicMod, number, configID);
	}

	@Override
	protected void sendPacket(L2Packet packet) {
		int packetSize = packet.getSize();
		if ((size + packetSize > capacity) || cabel == null) { // (drop packet, run out of capacity) || (no cable is connected)
			dropped++;
		} else {
			size += packetSize;
			buffer.add(packet);
			cabel.worker.wake();
		}
	}

	/**
	 * Receives packet from cable and pass it to physical module.
	 */
	@Override
	protected void receivePacket(L2Packet packet) {
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
		packet = buffer.remove(0);
		size -= packet.getSize();
		return packet;
	}

	/**
	 * Return true if buffer is empty.
	 */
	public boolean isEmptyBuffer() {
		return buffer.isEmpty();
	}

	/**
	 * Returns true, if on the other end of cable is connected other network device.
	 * @return
	 */
	@Override
	public boolean isConnected() {
		if (cabel == null) {
			return false;
		}
		if (cabel.getTheOtherSwitchport(this) != null) {
			return true;
		}
		return false;
	}
}
