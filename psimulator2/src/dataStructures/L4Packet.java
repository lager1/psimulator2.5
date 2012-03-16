/*
 * Erstellt am 27.10.2011.
 */

package dataStructures;

/**
 *
 * @author neiss
 */
public abstract class L4Packet {

	protected int size;

	public final Object data;

	public L4Packet(Object data) {
		this.data = data;
	}

	public int getSize() {
		return size;
	}

	public enum L4PacketType{
		ICMP,
		TCP,
		UDP,
	}

	public abstract L4PacketType getType();
}
