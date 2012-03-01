/*
 * created 29.2.2012
 */
package dataStructures;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IcmpPacket extends L4Packet {

	@Override
	public L4PacketType getType() {
		return L4PacketType.ICMP;
	}

	/**
	 * Types of ICMP packet.
	 */
	public enum Type {
		/**
		 * Ozvěna
		 */
		REPLY,
		/**
		 * Žádost o ozvěnu
		 */
		REQUEST,
		/**
		 * Signalizace nedoručení IP paketu
		 */
		UNDELIVERED,
		/**
		 * Čas (ttl) vypršel
		 */
		TIME_EXCEEDED,
	}

	/**
	 * Podtypy icmp paketu, pro kazdej typ jinej vyznam, u nas to ma vyznam jen pro typ UNDELIVERED.
	 */
	public enum Code {
//	   * 0 – nedosažitelná síť (network unreachable)<br />
		NETWORK_UNREACHABLE,
//     * 1 - nedosažitelný uzel (host unreachable)<br />
		HOST_UNREACHABLE,
//     * 2 - nedosažitelný protokol (protocol unreachable)<br />
		PROTOCOL_UNREACHABLE,
//     * 3 – nedosažitelný port (port unreachable)<br />
		PORT_UNREACHABLE,
//     * 4 - nedosažitelná síť (network unreachable)<br />


//     * 5 – nutná fragmentace, ale není povolena<br />
		FRAGMENTAION_REQUIRED,
//     * 6 – neznámá cílová síť (destination network unknown) <br />
		DESTINATION_NETWORK_UNKNOWN,
	}

	public final Type type;
	public final Code code;
	public final int icmp_seq;

	public IcmpPacket(Type type, Code code, int icmp_seq) {
		this.type = type;
		this.code = code;
		this.icmp_seq = icmp_seq;
	}
}
