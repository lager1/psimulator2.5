/*
 * created 1.2.2012
 */

package dataStructures;

import dataStructures.ipAddresses.IpAddress;

/**
 * Represents IPv4 packet.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpPacket extends L3Packet {

	// TODO: dodelat
	public final IpAddress src;
	public final IpAddress dst;
	public final int ttl; // TODO: poresit ruzny TTL na ruznych systemech

	public IpPacket(IpAddress src, IpAddress dst, int ttl, L4Packet data) {
		super(data);
		this.src = src;
		this.dst = dst;
		this.ttl = ttl;
	}

//	public IpPacket copy() {
//		IpAddress ip =  new IpPacket(src, dst);
//		ip.
//	}

	@Override
	public L3PacketType getType() {
		return L3PacketType.IPv4;
	}

}
