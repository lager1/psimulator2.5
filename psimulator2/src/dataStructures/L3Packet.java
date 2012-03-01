/*
 * Erstellt am 27.10.2011.
 */

package dataStructures;

/**
 *
 * @author neiss
 */
public abstract class L3Packet {

    public final L4Packet data;

	public L3Packet() {
		this.data = null;
	}

	public L3Packet(L4Packet data) {
		this.data = data;
	}

	public enum L3PacketType{
		IPv4,
		ARP;
	}

	int getSize() {
		int sum = 0;
		// TODO: pridat velikost tohoto paketu
		return sum + (data != null ? data.getSize() : 0);
	}

	public abstract L3PacketType getType();

}
