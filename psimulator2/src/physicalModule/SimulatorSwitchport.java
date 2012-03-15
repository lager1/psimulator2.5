/*
 * created 24.1.2012
 */
package physicalModule;

import dataStructures.IpPacket;
import dataStructures.L2Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L4.IcmpHandler;
import networkModule.TcpIpNetMod;

/**
 * Represents "interface" on L2.
 * For switchport connected to real network implement your own class.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class SimulatorSwitchport extends Switchport implements Loggable {

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
	private int capacity = 150_000; // zatim: 100 x max velikost ethernetovyho pakatu
	/**
	 * Count of dropped packets.
	 *
	 * @param packet
	 * @return
	 */
	private int dropped = 0;
	private IcmpHandler icmpHandler = null;
	private boolean hasL3module;
	private boolean firstTime =  true;

	public SimulatorSwitchport(PhysicMod physicMod, int number, int configID) {
		super(physicMod, number, configID);
	}

	@Override
	protected void sendPacket(L2Packet packet) {
		int packetSize = packet.getSize();
		if (size + packetSize > capacity) { // run out of capacity
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Odesilaci fronta je plna, proto zahazuji paket.", packet.toStringWithData());
			dropped++;

			if (firstTime) {
				firstTime = false;
				hasL3module = physicMod.device.getNetworkModule().isStandardTcpIpNetMod();
				if (hasL3module) {
					icmpHandler = ((TcpIpNetMod) (physicMod.device.getNetworkModule())).transportLayer.icmphandler;
				}
			}

			if (hasL3module) {
				handleSourceQuench(packet);
			}

		} else if (cabel == null) { // no cabel attached
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Neni pripojen zadny kabel, proto zahazuji paket.", packet.toStringWithData());
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
	@Override
	public L2Packet popPacket() {
		L2Packet packet;
		packet = buffer.remove(0);
		size -= packet.getSize();
		return packet;
	}

	/**
	 * Return true if buffer is empty.
	 */
	@Override
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

	@Override
	public String getDescription() {
		return "SimulatorSwitchport";
	}

	private void handleSourceQuench(L2Packet packet) {
		if (packet.data != null && packet.data instanceof IpPacket) {
			IpPacket p = (IpPacket) packet.data;
			icmpHandler.sendSourceQuench(p.src, p);
		}
	}
}
