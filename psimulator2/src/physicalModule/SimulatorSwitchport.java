/*
 * created 24.1.2012
 */
package physicalModule;

import dataStructures.DropItem;
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
 * Represent's switchport on layer 2.
 * Nebezi ve vlasti vlakne, ma jeden bufer odchozich paketu, ktery plni fysicka vrstva a vybira kabel.
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


// konstruktory a buildeni pri startu: --------------------------------------------------------------------------------

	public SimulatorSwitchport(PhysicMod physicMod, int number, int configID) {
		super(physicMod, number, configID);
	}

	/**
	 * In configuration it isn't saved, that switchport is real, it's discovered while cables are plugged in. So I need
	 * this function to convert this SimulatorSwitchport to RealSwitchport.
	 *
	 * V konfiguraci neni ulozeno, je-li switchport realny, to se zjisti az podle toho, jestli kabel od neho natazenej
	 * vede k realnymu pocitaci. Proto potrebuju tuto metodu, abych moh puvodne vytvoreny simulator switchport
	 * konvertovat na realnej.
	 */
	public void replaceWithRealSwitchport(){
		physicMod.addSwitchport(number, true, configID);
	}



// metody pro sitovou komunikaci:

	@Override
	protected void sendPacket(L2Packet packet) {
		int packetSize = packet.getSize();
		if (size + packetSize > capacity) { // run out of capacity
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: Queue is full.", packet.toStringWithData());
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicMod.device.configID));
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
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: No cable is attached.", packet.toStringWithData());
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicMod.device.configID));
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
	 * Removes packet form buffer and returns it, decrements size of buffer. Synchronised via buffer. Throws exception
	 * when this method is called and no packet is in buffer.
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
	 * Returns true if buffer is empty.
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

	@Override
	public String getDescription() {
		return "SimulatorSwitchport number: "+number + ", configID: "+configID;
	}

	private void handleSourceQuench(L2Packet packet) {
		if (packet.data != null && packet.data instanceof IpPacket) {
			IpPacket p = (IpPacket) packet.data;
			icmpHandler.sendSourceQuench(p.src, p);
		} else {
			Logger.log(this, Logger.INFO, LoggingCategory.PHYSICAL, "Dropping packet: queue is full - packet is not IP so no source-quench is sent.", packet.toStringWithData());
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, physicMod.device.configID));
		}
	}

	@Override
	public boolean isReal() {
		return false;
	}
}
