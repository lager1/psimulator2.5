/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.ArpPacket;
import dataStructures.IpPacket;
import dataStructures.L4Packet;
import dataStructures.ipAddresses.IpAddress;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.TcpIpNetMod;

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

	public LinuxIPLayer(TcpIpNetMod netMod) {
		super(netMod);
		this.ttl = 60;
	}

	@Override
	protected void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch (packet.operation) {
			case ARP_REQUEST:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Prisel ARP request", packet);

				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);

				// jsem ja target? Ano -> poslat ARP reply
				if (isItMyIpAddress(packet.targetIpAddress)) { //poslat ARP reply
					ArpPacket arpPacket = new ArpPacket(packet.senderIpAddress, packet.senderMacAddress, packet.targetIpAddress, iface.getMac());
					Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Reaguji na ARP request a posilam REPLY na "+packet.senderIpAddress, arpPacket);
					netMod.ethernetLayer.sendPacket(arpPacket, iface, packet.senderMacAddress);
				} else {
					Logger.log(this, Logger.DEBUG, LoggingCategory.ARP, "Prisel mi ARP request, ale nejsem cilem, takze nic nedelam. Cilem je: "+packet.targetIpAddress, packet);
				}
				break;

			case ARP_REPLY:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Prisel ARP reply - ukladam ji tez do cache.", packet);
				// ulozit si target
				// kdyz uz to prislo sem, tak je jasne, ze ta odpoved byla pro me (protoze odpoved se posila jen odesilateli a ne na broadcast), takze si ji muzu ulozit a je to ok
				arpCache.updateArpCache(packet.targetIpAddress, packet.targetMacAddress, iface);
				newArpReply = true;
				worker.wake();
				break;
		}
	}

	@Override
	public void handleSendPacket(L4Packet packet, IpAddress dst) {

		if (isItMyIpAddress(dst)) {
			IpPacket p = new IpPacket(dst, dst, this.ttl, packet);

			handleReceivePacket(p, null); // rovnou ubsluz v mem vlakne
			return;
		}

		RoutingTable.Record record = routingTable.findRoute(dst);
		if (record == null) { // kdyz nemam zaznam na v RT, tak zahodim
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Zahazuji tento packet, protoze nejde zaroutovat", packet);
			return;
		}

		IpPacket p = new IpPacket(record.rozhrani.getIpAddress().getIp(), dst, this.ttl, packet);

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
		// odnatovat
		NetworkInterface ifaceIn = findIncommingNetworkIface(iface);
		packet = packetFilter.preRouting(packet, ifaceIn);

		// je pro me?
		if (isItMyIpAddress(packet.dst)) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Prijimam IP paket, ktery je pro me.", packet);
			netMod.transportLayer.receivePacket(packet);
			return;
		}

		if (!ip_forward) {
			// Jestli se nepletu, tak paket proste zahodi. Chce to ale jeste overit.
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Zahazuji tento packet, protoze neni nastaven ip_forward.", packet);
			return;
		}

		// osetri TTL
		if (packet.ttl == 1) {
			// posli TTL expired a zaloguj zahozeni paketu
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Zahazuji tento packet, protoze vyprselo TTL", packet);
			getIcmpHandler().sendTimeToLiveExceeded(packet.src, packet);
			return;
		}

		// zaroutuj
		RoutingTable.Record record = routingTable.findRoute(packet.dst);
		if (record == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Prijimam IP paket, ale nejde zaroutovat, pac nemam zaznam na "+packet.dst+". Poslu DNU.", packet);
			getIcmpHandler().sendDestinationNetworkUnreachable(packet.src, packet);
			return;
		}

		// vytvor novy paket a zmensi TTL (kdyz je packet.src null, tak to znamena, ze je odeslan z toho sitoveho device
		//		a tedy IP adresa se musi vyplnit dle rozhrani, ze ktereho to poleze ven
		IpPacket p = new IpPacket(packet.src, packet.dst, packet.ttl - 1, packet.data);

		Logger.log(this, Logger.INFO, LoggingCategory.NET, "Prisel IP paket z rozhrani: "+ifaceIn.name, packet);
		processPacket(p, record, ifaceIn);
	}
}
