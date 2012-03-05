/*
 * created 1.2.2012
 */

package networkModule.L3;

import dataStructures.IpPacket;

/**
 * Represents packet filter, implements network address translation.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PacketFilter {

	private final IPLayer ipLayer;
	private final NatTable natTable;

	public PacketFilter(IPLayer ipLayer) {
		this.ipLayer = ipLayer;
		this.natTable = new NatTable(ipLayer);
	}



	public IpPacket preRouting(IpPacket packet, NetworkInterface in) {
		return packet;
	}

	public IpPacket postRouting(IpPacket packet, NetworkInterface in, NetworkInterface out) {
		return packet;
	}

	// cisco NAT
	// http://www.cisco.com/en/US/tech/tk648/tk361/technologies_tech_note09186a0080133ddd.shtml
}
