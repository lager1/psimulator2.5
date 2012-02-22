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
	 * Nema pro simulator zadnej velkej vyznam, ale je soucasti toho ethernet II paketu - 3. predaska PSI, slide 32
	 */
	final L3PacketType type;

	public EthernetPacket(MacAddress src, MacAddress dst, L3PacketType ethertype) {
		this.src = src;
		this.dst = dst;
		this.type = ethertype;
	}

	public EthernetPacket(MacAddress src, MacAddress dst, L3PacketType ethertype, L3Packet data) {
		this.src = src;
		this.dst = dst;
		this.type = ethertype;
		this.data=data;
	}

	public MacAddress getDst() {
		return dst;
	}

	/**
	 * Vrati protokol vyssi vrstvy (polozku type z toho paketu)
	 * @return 
	 */
	public L3PacketType getEthertype() {
		return type;
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
		return sum + (data != null ? data.getSize() : 0);
	}

	@Override
	public L2PacketType getType() {
		return L2PacketType.ethernetII;
	}
}
