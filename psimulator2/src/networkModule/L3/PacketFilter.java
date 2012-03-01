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

	public IpPacket preRouting(IpPacket packet, NetworkIface in) {
		return packet;
	}

	public IpPacket postRouting(IpPacket packet, NetworkIface in, NetworkIface out) {
		return packet;
	}

	// cisco NAT
	// http://www.cisco.com/en/US/tech/tk648/tk361/technologies_tech_note09186a0080133ddd.shtml
}
