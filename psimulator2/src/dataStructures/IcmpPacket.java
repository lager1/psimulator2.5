/*
 * created 29.2.2012
 */
package dataStructures;

import utils.Util;

/**
 * Represents ICMP packet.
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
		 * Ozvěna.
		 */
		REPLY,
		/**
		 * Žádost o ozvěnu.
		 */
		REQUEST,
		/**
		 * Signalizace nedoručení IP paketu.
		 */
		UNDELIVERED,
		/**
		 * Čas (ttl) vypršel.
		 */
		TIME_EXCEEDED,
		/**
		 * This message may be generated if a router or host does not have sufficient buffer space to process the request, or may occur if the router or host buffer is approaching its limit.
		 */
		SOURCE_QUENCH,

	}

	/**
	 * Podtypy icmp paketu, pro kazdej typ jinej vyznam, u nas to ma vyznam jen pro typ UNDELIVERED.
	 */
	public enum Code {

		/**
		 * kdyz se odesila REPLY
		 */
		DEFAULT,
		//	   * 0 – nedosažitelná síť (network unreachable)
		NETWORK_UNREACHABLE,
		//     * 1 - nedosažitelný uzel (host unreachable)
		HOST_UNREACHABLE,
		//     * 2 - nedosažitelný protokol (protocol unreachable)
		PROTOCOL_UNREACHABLE,
		//     * 3 – nedosažitelný port (port unreachable)
		PORT_UNREACHABLE,
		//     * 4 - nedosažitelná síť (network unreachable)

		//     * 5 – nutná fragmentace, ale není povolena
		FRAGMENTAION_REQUIRED,
		//     * 6 – neznámá cílová síť (destination network unknown)
		DESTINATION_NETWORK_UNKNOWN,}
	/**
	 * REPLY, REQUEST, UNDELIVERED, TIME_EXCEEDED.
	 */
	public final Type type;
	/**
	 * DHU, DNU, PU, ..
	 */
	public final Code code;
	/**
	 * Identifier is used like a port in TCP or UDP to identify a session.
	 */
	public final int id;
	/**
	 * Sequence number is incremented on each echo request sent. The echoer returns these same values (id+seq) in the
	 * echo reply.
	 */
	public final int seq;

	/**
	 * Creates IcmpPacket with given type and code. <br /> id and seq is set to 0
	 *
	 * @param type
	 * @param code
	 */
	public IcmpPacket(Type type, Code code) {
		super(null);
		this.type = type;
		this.code = code;
		this.id = 0;
		this.seq = 0;
		countSize();
	}

	/**
	 * Creates IcmpPacket with given type, code, id, seq.
	 *
	 * @param type
	 * @param code
	 * @param id
	 * @param seq
	 */
	public IcmpPacket(Type type, Code code, int id, int seq) {
		super(null);
		this.type = type;
		this.code = code;
		this.id = id;
		this.seq = seq;
		countSize();
	}

	/**
	 * Creates IcmpPacket with given type, code, id, seq.
	 *
	 * @param type
	 * @param code
	 * @param id
	 * @param seq
	 * @param size payload size
	 */
	public IcmpPacket(Type type, Code code, int id, int seq, int size) {
		super(null);
		this.type = type;
		this.code = code;
		this.id = id;
		this.seq = seq;
		this.size = 8 + size;
	}

	@Override
	public String toString(){
		return "IcmpPacket: "+Util.zarovnej(type.toString(), 7)+" "+code+" id: " + id + " seq="+seq;
	}

	private void countSize() { // ICMP packet has 8 bytes + data (http://en.wikipedia.org/wiki/Ping)
		this.size = 8 + 56;
	}
}
