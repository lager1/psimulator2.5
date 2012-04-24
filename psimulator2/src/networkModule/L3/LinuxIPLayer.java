/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.packets.ArpPacket;
import dataStructures.DropItem;
import dataStructures.packets.IpPacket;
import dataStructures.packets.L4Packet;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IpAddress;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.IpNetworkModule;

/**
 * Linux-specific IPLayer.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class LinuxIPLayer extends IPLayer {

	/**
	 * Packet forwarding flag. <br />
	 * TODO: port_forward ulozit do filesystemu a nacitat od tam tud
	 */
	public boolean ip_forward = true;

	public LinuxIPLayer(IpNetworkModule netMod) {
		super(netMod);
		this.ttl = 64;
	}

	@Override
	protected void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch (packet.operation) {
			case ARP_REQUEST:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "ARP request received.", packet);

				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);

				// jsem ja target? Ano -> poslat ARP reply
				if (isItMyIpAddress(packet.targetIpAddress)) { //poslat ARP reply
					ArpPacket arpPacket = new ArpPacket(packet.targetIpAddress, iface.getMac(), packet.senderIpAddress, packet.senderMacAddress);
					Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Reacting on ARP request: sending REPLY to " +packet.senderIpAddress, arpPacket);
					netMod.ethernetLayer.sendPacket(arpPacket, iface, packet.senderMacAddress);
				} else {
					Logger.log(this, Logger.DEBUG, LoggingCategory.ARP, "ARP request received, but I am not a target - doing nothing.", packet);
				}
				break;

			case ARP_REPLY:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "ARP reply received.", packet);
				// ulozit si target
				// kdyz uz to prislo sem, tak je jasne, ze ta odpoved byla pro me (protoze odpoved se posila jen odesilateli a ne na broadcast), takze si ji muzu ulozit a je to ok
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);
				newArpReply = true;
				worker.wake();
				break;
			default:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Dropping packet: Unknown ARP type packet received.", packet);
				Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
		}
	}

	@Override
	public void handleSendPacket(L4Packet packet, IpAddress dst, int ttl) {

		if (isItMyIpAddress(dst) || dst.isLocalSubnet127()) {
			IpPacket p = new IpPacket(dst, dst, ttl, packet);

			handleReceivePacket(p, null); // rovnou ubsluz v mem vlakne
			return;
		}

		RoutingTable.Record record = routingTable.findRoute(dst);
		if (record == null) { // kdyz nemam zaznam na v RT, tak zahodim
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: unroutable.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			return;
		}

		IpPacket p = new IpPacket(record.iface.getIpAddress().getIp(), dst, ttl, packet);

		processPacket(p, record, null);
	}

	/**
	 * Handles packet: is it for me?, routing, decrementing TTL, postrouting, MAC address finding.
	 *
	 * @param packet
	 * @param iface incomming EthernetInterface, can be null
	 */
	@Override
	protected void handleReceiveIpPacket(IpPacket packet, EthernetInterface iface) {

		NetworkInterface ifaceIn = findIncommingNetworkIface(iface);

		// kdyz je to vuci prichozimu rozhrani broadcast, tak to poslu nahoru (je to pro me) - kvuli DHCP!
		if (ifaceIn != null && ifaceIn.getIpAddress() != null && ifaceIn.getIpAddress().getBroadcast().equals(packet.dst)) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Received IP packet which was sent as broadcast for this interface.", packet);
			netMod.transportLayer.receivePacket(new PacketItem(packet,ifaceIn));
			return;
		}

		// odnatovat
		packet = packetFilter.preRouting(packet, ifaceIn);
		if (packet == null) { // packet dropped, ..
			return;
		}

		// je pro me?
		if (isItMyIpAddress(packet.dst) || packet.dst.isLocalSubnet127()) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Received IP packet destined to be mine.", packet);
			netMod.transportLayer.receivePacket(new PacketItem(packet,ifaceIn));
			return;
		}

		if (!ip_forward) {
			// Jestli se nepletu, tak paket proste zahodi. Chce to ale jeste overit.
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: ip_forward is not set.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			return;
		}

		// osetri TTL
		if (packet.ttl == 1) {
			// posli TTL expired a zaloguj zahozeni paketu
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: TTL expired.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			getIcmpHandler().sendTimeToLiveExceeded(packet.src, packet);
			return;
		}

		// zaroutuj
		RoutingTable.Record record = routingTable.findRoute(packet.dst);
		if (record == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Dropping packet: IP packet received, but packet is unroutable - no record for "+packet.dst+". Will send Destination Network Unreachable.", packet);
			Logger.log(this, Logger.INFO, LoggingCategory.PACKET_DROP, "Logging dropped packet.", new DropItem(packet, getNetMod().getDevice().configID));
			getIcmpHandler().sendNetworkUnreachable(packet.src, packet);
			return;
		}

		// vytvor novy paket a zmensi TTL (kdyz je packet.src null, tak to znamena, ze je odeslan z toho sitoveho device
		//		a tedy IP adresa se musi vyplnit dle iface, ze ktereho to poleze ven
		IpPacket p = new IpPacket(packet.src, packet.dst, packet.ttl - 1, packet.data);

		Logger.log(this, Logger.INFO, LoggingCategory.NET, "IP packet received from interface: "+(ifaceIn == null ? "null" : ifaceIn.name), packet);
		processPacket(p, record, ifaceIn);
	}
}
