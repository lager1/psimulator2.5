/*
 * created 25.1.2012
 */

package dataStructures;

import dataStructures.L3Packet.L3PacketType;

/**
 * Represents Ethernet II frames.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public class EthernetPacket extends L2Packet {

	private final MacAddress src;
	private final MacAddress dst;

	/**
	 * Protokol vyssi vrstvy.
	 */
	final L3PacketType ethertype;

	public EthernetPacket(MacAddress src, MacAddress dst, L3PacketType ethertype) {
		this.src = src;
		this.dst = dst;
		this.ethertype = ethertype;
	}

	public EthernetPacket(MacAddress src, MacAddress dst, L3PacketType ethertype, L3Packet data) {
		this.src = src;
		this.dst = dst;
		this.ethertype = ethertype;
		this.data=data;
	}

	public MacAddress getDst() {
		return dst;
	}

	public L3PacketType getEthertype() {
		return ethertype;
	}

	public MacAddress getSrc() {
		return src;
	}

	public L3Packet getData() {
		return data;
	}



	@Override
	public int getSize() {
		int sum = 24; //8,6,6,2,?,4 (preambule, mac, mac, typ, data crc) - 3. predaska PSI
		// TODO: pridat velikost tohoto paketu
		return sum + (data != null ? data.getSize() : 0);
	}

	@Override
	public L2PacketType getType() {
		return L2PacketType.ethernetII;
	}
}
