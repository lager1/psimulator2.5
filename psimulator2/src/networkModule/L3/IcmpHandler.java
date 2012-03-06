/*
 * created 5.3.2012
 *
 * TODO: dodelat icmp_seq
 */

package networkModule.L3;

import dataStructures.IcmpPacket;
import dataStructures.IpPacket;
import dataStructures.ipAddresses.IpAddress;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.NetMod;
import psimulator2.Psimulator;

/**
 * Handles creating and sending ICMP packets.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IcmpHandler implements Loggable {

	private final NetMod netMod;
	private final IPLayer ipLayer;

	public IcmpHandler(NetMod netMod, IPLayer ipLayer) {
		this.netMod = netMod;
		this.ipLayer = ipLayer;
	}

	/**
	 * Sends ICMP message TimeToLiveExceeded to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendTimeToLiveExceeded(IpAddress dst, IpPacket packet) {
		IcmpPacket p = new IcmpPacket(IcmpPacket.Type.TIME_EXCEEDED, IcmpPacket.Code.HOST_UNREACHABLE, 0); // TODO: icmp_seq ?
		Psimulator.getLogger().log(this, Logger.INFO, LoggingCategory.NET, "Posilam Time Exceeded na: "+dst, p);
		ipLayer.handleSendPacket(p, dst);
	}

	/**
	 * Sends ICMP message Destination Host Unreachable to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendDestinationHostUnreachable(IpAddress dst, IpPacket packet) {
		IcmpPacket p = new IcmpPacket(IcmpPacket.Type.UNDELIVERED, IcmpPacket.Code.HOST_UNREACHABLE, 0); // TODO: jaka se posila icmp_seq ?
		Psimulator.getLogger().log(this, Logger.INFO, LoggingCategory.NET, "Posilam Destination Host Unreachable na: "+dst, p);
		ipLayer.handleSendPacket(p, dst);
	}

	/**
	 * Sends ICMP message Destination Network Unreachable to a given IpAddress.
	 * @param dst message target
	 * @param packet for some additional information only.
	 */
	public void sendDestinationNetworkUnreachable(IpAddress dst, IpPacket packet) {
		IcmpPacket pCasted = (IcmpPacket) packet.data; // TODO: asi tu nebude
		IcmpPacket p = new IcmpPacket(IcmpPacket.Type.UNDELIVERED, IcmpPacket.Code.NETWORK_UNREACHABLE, pCasted.icmp_seq); // TODO: jaka se posila icmp_seq ?
		Psimulator.getLogger().log(this, Logger.INFO, LoggingCategory.NET, "Posilam Destination Net Unreachable na: "+packet.src, p);
		ipLayer.handleSendPacket(p, dst);
	}

	@Override
	public String getDescription() {
		return netMod.getDevice().getName() + ": IcmpHandler";
	}
}


