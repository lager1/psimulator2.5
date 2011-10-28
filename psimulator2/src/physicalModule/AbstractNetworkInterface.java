/*
 * Erstellt am 26.10.2011.
 */
package physicalModule;

import java.util.ArrayList;
import java.util.List;
import networkDataStructures.L2Packet;

/**
 * 
 * @author neiss
 */
public abstract class AbstractNetworkInterface {

	private String name;
	private Cabel cabel;
	/**
	 * buffer for incoming packets
	 */
	private final List<L2Packet> buffer;

	/**
	 * Adds incoming packet to the buffer.
	 * Sychronized via buffer.
	 * @param packet 
	 */
	public void addIncomingPacketToBuffer(L2Packet packet) {
		synchronized (buffer) {
			buffer.add(packet);
		}
	}

	public AbstractNetworkInterface(String name, Cabel cabel) {
		this.name = name;
		this.cabel = cabel;
		this.buffer = new ArrayList<L2Packet>();
	}

	public AbstractNetworkInterface(String name) {
		this.name = name;
		this.buffer = new ArrayList<L2Packet>();
	}

	public String getName() {
		return name;
	}

	public Cabel getCabel() {
		return cabel;
	}

	public void plugInCable(Cabel cabel) { // TODO: predelat co kam???
		this.cabel = cabel;
	}

	/**
	 * Try to send packet thgroug this interfaces
	 * @return true if packet was delivered to the interface at the end of cable
	 */
	public boolean sendPacket(L2Packet packet) {
		return cabel.transportPacket(packet, this);
	}

	/**
	 * Compare using only by names
	 * @param obj
	 * @return 
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AbstractNetworkInterface) {
			AbstractNetworkInterface iface = (AbstractNetworkInterface) obj;
			if (iface.name.equals(name)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}
}
