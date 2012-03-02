/*
 * created 31.1.2012
 */
package networkModule.L3;

import psimulator2.Psimulator;
import logging.LoggingCategory;
import logging.Logger;
import networkModule.L3.RoutingTable.Record;
import dataStructures.*;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import exceptions.UnsupportedL3TypeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import networkModule.L2.EthernetInterface;
import networkModule.TcpIpNetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents IP layer of ISO/OSI model.
 *
 * TODO: pridat paketovy filtr + routovaci tabulku TODO: predelat EthernetInterface na neco abstratniho??
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer implements SmartRunnable, Loggable {

	protected final WorkerThread worker = new WorkerThread(this);
	/**
	 * ARP cache table.
	 */
	private final ArpCache arpCache = new ArpCache();
	/**
	 * Packet filter. Controls NAT, packet dropping, ..
	 */
	private final PacketFilter packetFilter = new PacketFilter();
	private final List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());
	private final List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	/**
	 * Zde budou pakety, ktere je potreba odeslat, ale nemam ARP zaznam, takze byla odeslana ARP request, ale jeste
	 * nemam odpoved. Obsluhovat me bude doMyWork(). Neni potreba miti synchronizaci, protoze sem leze jen vlakno z
	 * doMyWork().
	 */
	private final List<SendItem> storeBuffer = new LinkedList<SendItem>();
	/**
	 * Routing table with record.
	 */
	private final RoutingTable routingTable = new RoutingTable();
	/**
	 * Link to network module.
	 */
	private final TcpIpNetMod netMod;
	/**
	 * When some ARP reply arrives this is set to true so doMyWork() can process storeBuffer. After processing
	 * storeBuffer it is set to false.
	 */
	private boolean newArpReply = false;
	private final List<NetworkIface> networkIfaces = new ArrayList<NetworkIface>();

	public IPLayer(TcpIpNetMod netMod) {
		this.netMod = netMod;
		processEthernetInterfaces();
	}
	/**
	 * Default TTL values.
	 */
	public int ttl = 255;

	/**
	 * Potrebne pro vypis pro cisco a linux.
	 *
	 * @return
	 */
	public HashMap<IpAddress, ArpCache.ArpRecord> getArpCache() {
		return arpCache.getCache();
	}

	/**
	 * Method for receiving packet from layer 2.
	 *
	 * @param packet
	 * @param iface
	 */
	public void receivePacket(L3Packet packet, EthernetInterface iface) {
		receiveBuffer.add(new ReceiveItem(packet, iface));
		worker.wake();
	}

	private void handleReceivePacket(L3Packet packet, EthernetInterface iface) {
		switch (packet.getType()) {
			case ARP:
				ArpPacket arp = (ArpPacket) packet;
				handleReceiveArpPacket(arp, iface);
				break;

			case IPv4:
				IpPacket ip = (IpPacket) packet;
				handleReceiveIpPacket(ip, iface);
				break;

			default:
				throw new UnsupportedL3TypeException("Unsupported L3 type: " + packet.getType());
		}
	}

	/**
	 * Handles imcomming ARP packets.
	 *
	 * @param packet
	 * @param iface incomming EthernetInterface
	 */
	private void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch (packet.operation) {
			case ARP_REQUEST:
				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);
				// jsem ja target? Ano -> poslat ARP reply
				if (isItMyIpAddress(packet.targetIpAddress)) { //poslat ARP reply
					ArpPacket arpPacket = new ArpPacket(packet.senderIpAddress, packet.senderMacAddress, packet.targetIpAddress, iface.getMac());
					netMod.ethernetLayer.sendPacket(arpPacket, iface, packet.senderMacAddress);
				}
				break;

			case ARP_REPLY:
				// ulozit si target
				// kdyz uz to prislo sem, tak je jasne, ze ta odpoved byla pro me (protoze odpoved se posila jen odesilateli a ne na broadcast), takze si ji muzu ulozit a je to ok
				arpCache.updateArpCache(packet.targetIpAddress, packet.targetMacAddress, iface);
				newArpReply = true;
				break;
		}
	}

	private void handleArpBuffer() {

		long now = System.currentTimeMillis();

		List<SendItem> remove = new ArrayList<SendItem>();

		for (SendItem m : storeBuffer) {

			if (now - m.timeStamp > 10000) { // vice jak 10s stare se smaznou, kdyz ICMP REQ, tak se posle zpatky DHU

				remove.add(m);
				// TODO: poslat zpatky DHU ?? jen pokud je to ICMP REQ ?
				// TODO: kdyz mi neprijde zadna odpoved, tak se NIKDY neodesle 'destination host unreachable', takze budem muset implementovat asi nejakej budik, kterej me zbudi za 5s.
				continue;
			}

			// TODO: aso bi tu mela byt adresa z routovaci tabulky a ne cilova IP adresa daneho packetu, je tu potreba IP adresa nextHopu!

			MacAddress mac = arpCache.getMacAdress(m.dst);
			if (mac != null) {
				// TODO: obslouzit
//				IpPacket packet = new IpPacket(m., m.dst, ttl);


//				netMod.ethernetLayer.sendPacket(m., null, mac);

				// je obslouzeno, tak vyndam z bufferu
				remove.add(m);
			}
		}

		// vymazani proslych zaznamu
		for (SendItem m : remove) {
			storeBuffer.remove(m);
		}


		newArpReply = false;
	}

	private void handleReceiveIpPacket(IpPacket packet, EthernetInterface iface) {
		// odnatovat
		NetworkIface ifaceIn = findIncommingNetworkIface(iface);

		// zpracovat paket
		handleSendIpPacket(packet, iface);
	}

	/**
	 * Bude resit zaroutovani, zanatovani, zjisteni MAC, ..
	 *
	 * TODO: routuje se, kdyz ma packet TTL==1 a az pak se zahodi?
	 *
	 * @param packet
	 * @param iface incomming EthernetInterface
	 */
	private void handleSendIpPacket(IpPacket packet, EthernetInterface iface) {

		// je pro me?
		if (isItMyIpAddress(packet.dst)) {
			netMod.tcpipLayer.receivePacket(packet.data);
			return;
		}

		NetworkIface ifaceIn = findIncommingNetworkIface(iface);

		// osetri TTL
		if (packet.ttl == 1) {
			// jestli je to ICMP, tak posli TTL expired
			// zaloguj zahozeni paketu
			Psimulator.getLogger().logg(this, Logger.IMPORTANT, LoggingCategory.NET, "Zahazuji tento packet, protoze vyprselo TTL", packet);
			sendIcmpTimeExceeded(packet);
			return;
		}

		// zaroutuj
		Record record = routingTable.findRoute(packet.dst);

		if (record == null) {
			if (isPacketIcmpRequest(packet)) { // nema to nahodou vracet DHU vzdy??
				sendIcmpDestinationNetworkUnreachable(packet); // TODO nemela by tohle resit vyssi vrstva?
			}
			Psimulator.getLogger().logg(this, Logger.IMPORTANT, LoggingCategory.NET, "Zahazuji tento packet, protoze nejde zaroutovat", packet);
			return;
		}

		// zmensi TTL
		IpPacket p = new IpPacket(packet.src, packet.dst, packet.ttl - 1);

		// zanatuj
		p = packetFilter.postRouting(p, ifaceIn, record.rozhrani);

		// zjisti MAC adresu z ARP cache - je=OK, neni=vygenerovat ARP request a vlozit do storeBuffer + touch na sendItem, ktera bude v storeBuffer
		MacAddress mac = arpCache.getMacAdress(record.brana);
		if (mac == null) { // posli ARP request a dej do fronty
			ArpPacket arpPacket = new ArpPacket(record.rozhrani.ipAddress.getIp(), record.rozhrani.getMacAddress(), record.brana);





			// TODO: tady pokracovat!


			netMod.ethernetLayer.sendPacket(arpPacket, null, MacAddress.broadcast()); // TODO: odeslat ARP req na vsechny rozhrani v dany siti????
//			storeBuffer.add(new SendItem(p, null));
			return;
		}


		netMod.ethernetLayer.sendPacket(p, record.rozhrani.ethernetInterface, mac);
	}

	/**
	 * Method for sending packet from layer 4.
	 *
	 * @param packet
	 * @param dst
	 */
	public void sendPacket(L4Packet packet, IpAddress dst) {
		sendBuffer.add(new SendItem(packet, dst));
		worker.wake();
	}

	private void handleSendPacket(L4Packet packet, IpAddress dst) {
//		tady se bude volat nejakym zpusobem metoda handleSendIpPacket()
//		handleSendIpPacket(null, null);
	}

	public void doMyWork() {

		// prochazet jednotlivy buffery a vyrizovat jednotlivy pakety
		while (!sendBuffer.isEmpty() || !receiveBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				ReceiveItem m = receiveBuffer.remove(0);
				handleReceivePacket(m.packet, m.iface);
			}

			if (!sendBuffer.isEmpty()) {
				SendItem m = sendBuffer.remove(0);
				handleSendPacket(m.packet, m.dst);
			}

			if (newArpReply && !storeBuffer.isEmpty()) {
				// bude obskoceno vzdy, kdyz se tam neco prida, tak snad ok
				handleArpBuffer();
			}
		}
	}

	/**
	 * Sets IpAddress with NetMask to given interface. There is no other way to set IP address to interface.
	 *
	 * Reason for this method is that we might to add some actions after setting address in future.
	 *
	 * @param iface
	 * @param ipAddress
	 */
	public void setIpAddressOnInterface(NetworkIface iface, IPwithNetmask ipAddress) {
		iface.ipAddress = ipAddress;
	}

	/**
	 * Process EthernetInterfaces (L2 iface) and creates adequate NetworkIface (L3 iface).
	 */
	private void processEthernetInterfaces() {
		for (EthernetInterface iface : netMod.ethernetLayer.ifaces) {
			networkIfaces.add(new NetworkIface(iface.name, iface));
		}
	}

	/**
	 * Returns true if targetIpAddress is on my NetworkIface and isUp
	 *
	 * @param targetIpAddress
	 * @return
	 */
	private boolean isItMyIpAddress(IpAddress targetIpAddress) {
		for (NetworkIface iface : networkIfaces) {
			if (iface.ipAddress.getIp().equals(targetIpAddress) && iface.isUp) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if packet is ICMP REQUEST.
	 *
	 * @param packet
	 * @return
	 */
	private boolean isPacketIcmpRequest(IpPacket packet) {
		if (packet.data != null && packet.data.getType() == L4Packet.L4PacketType.ICMP) {
			IcmpPacket p = (IcmpPacket) packet.data;
			if (p.type == IcmpPacket.Type.REQUEST) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Sends DHU to packet.src.
	 *
	 * @param packet
	 */
	private void sendIcmpDestinationNetworkUnreachable(IpPacket packet) {
		IcmpPacket pCasted = (IcmpPacket) packet.data;
		IcmpPacket p = new IcmpPacket(IcmpPacket.Type.UNDELIVERED, IcmpPacket.Code.NETWORK_UNREACHABLE, pCasted.icmp_seq); // TODO: jaka se posila icmp_seq ?
		Psimulator.getLogger().logg(this, Logger.INFO, LoggingCategory.NET, "Posilam Destination Network Unreachable: ", p);
		handleSendPacket(p, packet.src);
	}

	/**
	 * Send ICMP Time Exceeded message to the sender (packet.src).
	 *
	 * @param packet
	 */
	private void sendIcmpTimeExceeded(IpPacket packet) {
		IcmpPacket p = new IcmpPacket(IcmpPacket.Type.TIME_EXCEEDED, IcmpPacket.Code.HOST_UNREACHABLE, 0); // TODO: icmp_seq ?
		Psimulator.getLogger().logg(this, Logger.INFO, LoggingCategory.NET, "Posilam Time Exceeded: ", p);
		handleSendPacket(p, packet.src);
	}

	@Override
	public String getDescription() {
		return netMod.getDevice().getName() + ": IpLayer";
	}

	private NetworkIface findIncommingNetworkIface(EthernetInterface inc) {
		if (inc == null) {
			return null;
		}
		for (NetworkIface iface : networkIfaces) {
			if (iface.ethernetInterface.name.equals(inc.name)) {
				return iface;
			}
		}
		throw new RuntimeException("Nenazeleno NetworkIface, ktere by bylo spojeno s EthernetInterface, ze ktereho prave prisel packet!");
	}

	private class SendItem {

		final L4Packet packet;
		final IpAddress dst;
		long timeStamp; // pro potreby storeBufferu

		public SendItem(L4Packet packet, IpAddress dst) {
			this.packet = packet;
			this.dst = dst;
		}

		public void touch() {
			this.timeStamp = System.currentTimeMillis();
		}
	}

	private class ReceiveItem {

		final L3Packet packet;
		final EthernetInterface iface;

		public ReceiveItem(L3Packet packet, EthernetInterface iface) {
			this.packet = packet;
			this.iface = iface;
		}
	}
}
