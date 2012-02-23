/*
 * created 31.1.2012
 */
package networkModule.L3;

import dataStructures.*;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import exceptions.UnsupportedL3TypeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import networkModule.L2.EthernetInterface;
import networkModule.TcpIpNetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents IP layer of ISO/OSI model.
 *
 * TODO: pridat paketovy filtr + routovaci tabulku
 * TODO: predelat EthernetInterface na neco abstratniho??
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer implements SmartRunnable {

	protected final WorkerThread worker = new WorkerThread(this);
	/**
	 * ARP cache table.
	 */
	private final ArpCache arpCache = new ArpCache();
	/**
	 * Packet filter.
	 * Controls NAT, packet dropping, ..
	 */
	private final PacketFilter packetFilter = new PacketFilter();
	private final List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());
	private final List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	/**
	 * Zde budou pakety, ktere je potreba odeslat, ale nemam ARP zaznam, takze byla odeslana ARP request, ale jeste nemam odpoved.
	 * Obsluhovat me bude doMyWork().
	 * Neni potreba miti synchronizaci, protoze sem leze jen vlakno z doMyWork().
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
	 * When some ARP reply arrives this is set to true
	 * so doMyWork() can process storeBuffer.
	 * After processing storeBuffer it is set to false.
	 */
	private boolean newArpReply = false;
	private final List<NetworkIface> networkIfaces = new ArrayList<NetworkIface>();

	public IPLayer(TcpIpNetMod netMod) {
		this.netMod = netMod;
		processEthernetInterfaces();
	}

	/**
	 * Potrebne pro vypis pro cisco a linux.
	 * @return
	 */
	public HashMap<IpAddress, ArpCache.ArpRecord> getArpCache() {
		return arpCache.getCache();
	}

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

	private void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch (packet.operation) {
			case ARP_REQUEST:
				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);
				// jsem ja target? Ano -> poslat ARP reply
				if (isItMyIpAddress(packet.targetIpAddress)) { //poslat ARP reply
					ArpPacket arp = new ArpPacket(packet.senderIpAddress, packet.senderMacAddress, packet.targetIpAddress, iface.getMac());
					netMod.ethernetLayer.sendPacket(arp, iface, packet.senderMacAddress);
				}
				break;

			case ARP_REPLY:
				// ulozit si target
				// kdyz uz to prislo sem, tak je jasne, ze ta odpoved byla pro me, takze si ji muzu ulozit a je to ok
				arpCache.updateArpCache(packet.targetIpAddress, packet.targetMacAddress, iface);
				newArpReply = true;
				// TODO: domyslet, zda bych nemel reagovat i na ARP_REQUEST, pac se taky muzu dozvedet neco zajimavyho..
				break;
		}
	}

	private void handleArpBuffer() {

		// TODO: hrat si na casova razitka
		long now = System.currentTimeMillis();

		for (SendItem m : storeBuffer) {
			MacAddress mac = arpCache.getMacAdress(m.dst);
			if (mac != null) {
				// TODO: obslouzit
//					netMod.ethernetLayer.sendPacket(m., null, mac);
				// vyndat z bufferu
			} else {
				// zkontrolovat casove razitko, pokud je starsi nez, tak smaznout && poslat zpatky 'destination host unreachable'
				// TODO: kdyz mi neprijde zadna odpoved, tak se NIKDY neodesle 'destination host unreachable', takze budem muset implementovat asi nejakej budik, kterej me zbudi za 5s.
//				if (now - zaznam.vratCas() > 10000) {
			}
		}
		newArpReply = false;
	}

	private void handleReceiveIpPacket(IpPacket packet, EthernetInterface iface) {
		// odnatovat
		// je pro me?
		//		ANO - predat vejs
		//		NE - zaroutovat a predat do ethernetove vrstvy
	}

	public void sendPacket(L4Packet packet, IpAddress dst) {
		sendBuffer.add(new SendItem(packet, dst));
		worker.wake();
	}

	private void handleSendPacket(L4Packet packet, IpAddress dst) {

		// 1) zaroutuj - zjisti odchozi rozhrani
		// 2) zanatuj - packetFilter
		// 3) zjisti MAC adresu z ARP cache - je=OK, neni=vygenerovat ARP request a vlozit do storeBuffer + touch na sendItem, ktera bude v storeBuffer

		// 1
//		routingTable.findRecord(dst);


		// 2
//		packetFilter.

		// 3
//		MacAddress mac = arpCache.getMacAdress(nextHop);
//		if (mac == null) { // posli ARP request a dej do fronty
//			ArpPacket arpPacket = new ArpPacket(dst, mac, dst);
//		}


		throw new UnsupportedOperationException("Not yet implemented");
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
	 * Sets IpAddress with NetMask to given interface.
	 * There is no other way to set IP address to interface.
	 *
	 * Reason for this method is that we might to add some actions after setting address in future.
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
	 * Return true if targetIpAddress is on my NetworkIface and isUp
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
