/*
 * Erstellt am 26.10.2011.
 */
package dataStructures;

/**
 *
 * @author neiss
 */
public abstract class L2Packet {

    protected L3Packet data;

	public enum L2PacketType{
		ethernetII;
	}


    // TODO: getSize() cachovat, jinak bude pekne narocnej
    public abstract int getSize();

	public abstract L2PacketType getType();
}
