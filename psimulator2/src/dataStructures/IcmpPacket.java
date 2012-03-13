/*
 * created 29.2.2012
 */
package dataStructures;

import utils.Util;

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

		/**
		 * kdyz se odesila REPLY
		 */
		DEFAULT,
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
		this.type = type;
		this.code = code;
		this.id = 0;
		this.seq = 0;
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
		this.type = type;
		this.code = code;
		this.id = id;
		this.seq = seq;
	}

	@Override
	public String toString(){
		return "IcmpPacket: "+Util.zarovnej(type.toString(), 7)+" "+code+" id: " + id + " seq="+seq;
	}
}
