/*
 * created 1.2.2012
 */

package dataStructures;

/**
 * Represents IPv4 packet.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpPacket extends L3Packet {

	// TODO: dodelat

	@Override
	public L3PacketType getType() {
		return L3PacketType.IPv4;
	}

}
