/*
 * created 5.3.2012
 *
 */

package networkModule.L4;

import dataStructures.IcmpPacket;
import dataStructures.IcmpPacket.Code;
import dataStructures.IcmpPacket.Type;
import dataStructures.IpPacket;
import dataStructures.ipAddresses.IpAddress;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import utils.Util;

/**
 * Handles creating and sending ICMP packets.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IcmpHandler implements Loggable {

	private final TransportLayer transportLayer;

	public IcmpHandler(TransportLayer transportLayer) {
		this.transportLayer = transportLayer;
	}

	public void handleReceivedIcmpPacket(IpPacket packet) {
		IcmpPacket p = (IcmpPacket) packet.data;

		switch (p.type) {
			case REQUEST:
				// odpovedet
				IcmpPacket reply = new IcmpPacket(IcmpPacket.Type.REPLY, IcmpPacket.Code.DEFAULT, p.id, p.seq);
				Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Odesilam ARP odpoved.", packet);
				getIpLayer().handleSendPacket(reply, packet.src);
				break;
			case REPLY:
			case TIME_EXCEEDED:
			case UNDELIVERED:
			case SOURCE_QUENCH:
				// predat aplikacim
				Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Preposilam ARP odpoved aplikaci na port: "+p.id, packet);
				transportLayer.forwardPacketToApplication(packet, p.id);
				break;
			default:
				Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Neznamy typ ICMP paketu, zahazuju..", packet);
		}
	}

	public IPLayer getIpLayer() {
		return transportLayer.getIpLayer();
	}

	/**
	 * Sends ICMP message TimeToLiveExceeded to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendTimeToLiveExceeded(IpAddress dst, IpPacket packet) {
		send(packet, dst, IcmpPacket.Type.TIME_EXCEEDED, IcmpPacket.Code.DEFAULT);
	}

	/**
	 * Sends ICMP message Destination Host Unreachable to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendDestinationHostUnreachable(IpAddress dst, IpPacket packet) {
		send(packet, dst, IcmpPacket.Type.UNDELIVERED, IcmpPacket.Code.HOST_UNREACHABLE);
	}

	/**
	 * Sends ICMP message Destination Network Unreachable to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendDestinationNetworkUnreachable(IpAddress dst, IpPacket packet) {
		send(packet, dst, IcmpPacket.Type.UNDELIVERED, IcmpPacket.Code.NETWORK_UNREACHABLE);
	}

	/**
	 * Sends ICMP Source Quench message to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendSourceQuench(IpAddress dst, IpPacket packet) {
		send(packet, dst, IcmpPacket.Type.SOURCE_QUENCH, IcmpPacket.Code.DEFAULT);
	}

	@Override
	public String getDescription() {
		return Util.zarovnej(transportLayer.netMod.getDevice().getName(), Util.deviceNameAlign)+" IcmpHandler";
	}

	/**
	 * Returns L4 data of given packet or null.
	 * @param packet
	 * @return
	 */
	private IcmpPacket getIcmpPacket(IpPacket packet) {
		if (packet.data != null) {
			return (IcmpPacket) packet.data;
		}
		return null;
	}

	/**
	 * Sends ICMP packet with given type, code to dst. <br />
	 *
	 * @param packet
	 * @param dst destination IP
	 * @param type
	 * @param code
	 */
	private void send(IpPacket packet, IpAddress dst, Type type, Code code) {
		IcmpPacket icmp = getIcmpPacket(packet);
		IcmpPacket p;
		if (icmp != null) {
			p = new IcmpPacket(type, code, icmp.id, icmp.seq);
		} else {
			p = new IcmpPacket(type, code);
		}
		Logger.log(this, Logger.INFO, LoggingCategory.NET, "Posilam "+type+" "+code+" na: "+dst, p);
		getIpLayer().handleSendPacket(p, dst);
	}

	/**
	 * Sends ICMP echo request to given target.
	 * @param target
	 * @param ttl Time To Live - can be null, in that case default value of IPLayer is used
	 * @param seq sequence number
	 * @param id application identifier (port)
	 * @param payload size of data after ICMP header (celikost vyplnovacich dat za ICMP hlavickou)
	 */
	public void sendRequest(IpAddress target, Integer ttl, int seq, Integer id, int payload) {
		int sendTtl;
		if (ttl != null) {
			sendTtl = ttl;
		} else {
			sendTtl = this.getIpLayer().ttl;
		}

		IcmpPacket packet = new IcmpPacket(Type.REQUEST, Code.DEFAULT, id, seq, payload);
		getIpLayer().sendPacket(packet, target, sendTtl);
	}
}

