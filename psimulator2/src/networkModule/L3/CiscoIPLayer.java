/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.ArpPacket;
import dataStructures.IpPacket;
import dataStructures.L4Packet;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.L3.RoutingTable.Record;
import networkModule.TcpIpNetMod;

/**
 * Cisco-specific IPLayer.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoIPLayer extends IPLayer {

	public final CiscoWrapperRT wrapper;

	public CiscoIPLayer(TcpIpNetMod netMod) {
		super(netMod);
		this.ttl = 255;
		wrapper = new CiscoWrapperRT(netMod.getDevice(), this);
	}

	@Override
	protected void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch (packet.operation) {
			case ARP_REQUEST:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Prisel ARP request", packet);

				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);

				// posli ARP REPLY ((jsem to ja && mam routu na tazatele, tak odpovez!) || (nejsem to ja && mam routu na tazatele && vim kam mam ten paket dal poslat))
				if ((isItMyIpAddress(packet.targetIpAddress) && haveRouteFor(packet.senderIpAddress))
						|| (!isItMyIpAddress(packet.targetIpAddress) && haveRouteFor(packet.senderIpAddress) && haveRouteFor(packet.targetIpAddress))) {

					// poslat ARP reply
					ArpPacket arpPacket = new ArpPacket(packet.senderIpAddress, packet.senderMacAddress, packet.targetIpAddress, iface.getMac());
					Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Reaguji na ARP request a posilam REPLY na " + packet.senderIpAddress, arpPacket);
					netMod.ethernetLayer.sendPacket(arpPacket, iface, packet.senderMacAddress);
				} else {
					Logger.log(this, Logger.DEBUG, LoggingCategory.ARP, "Prisel mi ARP request, ale nemam odpovedet, takze nic nedelam.", packet);
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

			default:
				Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Prisel mi neznamy typ ARP paketu, zahazuju.", packet);
		}
	}

	/**
	 * Na ciscu, kdyz se odesila novy packet (ze shora), tak se nejdrive kontroluje RT, pokud neni zadny zaznam,
	 * tak se clovek ani nedopingne na sve rozhrani s IP.
	 * @param packet
	 * @param dst
	 */
	@Override
	public void handleSendPacket(L4Packet packet, IpAddress dst) {

		Record record = routingTable.findRoute(dst);
		if (record == null) { // kdyz nemam zaznam na v RT, tak zahodim
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Zahazuji tento packet, protoze nejde zaroutovat", packet);
			return;
		}

		IpPacket p = new IpPacket(record.rozhrani.getIpAddress().getIp(), dst, this.ttl, packet);

		if (isItMyIpAddress(dst)) {
			handleReceivePacket(p, null); // rovnou ubsluz v mem vlakne
			return;
		}

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
		if (packet == null) { // packet dropped, ..
			return;
		}

		// je pro me?
		if (isItMyIpAddress(packet.dst)) { // TODO: cisco asi pravdepovodne se nejdriv podiva do RT, a asi tam bude muset byt zaznam na svoji IP, aby se to dostalo nahoru..
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Prijimam IP paket, ktery je pro me.", packet);
			netMod.transportLayer.receivePacket(packet);
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
		Record record = routingTable.findRoute(packet.dst);
		if (record == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Prijimam IP paket, ale nejde zaroutovat, pac nemam zaznam na "+packet.dst+". Poslu DNU.", packet);
			getIcmpHandler().sendDestinationHostUnreachable(packet.src, packet); // cisco na skolnich routerech odesi DHU a nebo DNU jako linux
			return;
		}

		// vytvor novy paket a zmensi TTL (kdyz je packet.src null, tak to znamena, ze je odeslan z toho sitoveho device
		//		a tedy IP adresa se musi vyplnit dle rozhrani, ze ktereho to poleze ven
		IpPacket p = new IpPacket(packet.src, packet.dst, packet.ttl - 1, packet.data);

		Logger.log(this, Logger.INFO, LoggingCategory.NET, "Prisel IP paket z rozhrani: "+ifaceIn.name, packet);
		processPacket(p, record, ifaceIn);
	}

	@Override
	public void changeIpAddressOnInterface(NetworkInterface iface, IPwithNetmask ipAddress) {
		super.changeIpAddressOnInterface(iface, ipAddress);
		wrapper.update();
	}

	/**
	 * Returs true iff routing table has a valid record for given IP.
	 * @param ip
	 * @return
	 */
	private boolean haveRouteFor(IpAddress ip) {
		Record record = routingTable.findRoute(ip);
		if (record == null) {
			return false;
		}
		return true;
	}
}
