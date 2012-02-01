/*
 * created 31.1.2012
 */

package networkModule.L3;

import dataStructures.*;
import dataStructures.ipAddresses.IpAddress;
import exceptions.UnsupportedL3Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import networkModule.L2.EthernetInterface;
import networkModule.L4.TcpIpLayer;
import networkModule.Layer;
import networkModule.NetMod;
import networkModule.TcpIpNetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents IP layer of ISO/OSI model.
 *
 * TODO: pridat paketovy filtr + routovaci tabulku
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer implements SmartRunnable {

	private static class SendItem {
		L4Packet packet;
		IpAddress dst;

		public SendItem(L4Packet packet, IpAddress dst) {
			this.packet = packet;
			this.dst = dst;
		}
	}

	private static class ReceiveItem {
		L3Packet packet;
		EthernetInterface iface;

		public ReceiveItem(L3Packet packet, EthernetInterface iface) {
			this.packet = packet;
			this.iface = iface;
		}
	}

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
	 */
	private final List<SendItem> arpBuffer = Collections.synchronizedList(new LinkedList<SendItem>());

	private final RoutingTable routingTable = new RoutingTable();
	private final TcpIpNetMod netMod;

	public IPLayer(TcpIpNetMod netMod) {
		this.netMod = netMod;
	}

	public HashMap<IpAddress,ArpCache.ArpRecord> getArpCache() {
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
				throw new UnsupportedL3Type("Unsupported L3 type: "+packet.getType());
		}
	}

	private void handleReceiveArpPacket(ArpPacket packet, EthernetInterface iface) {
		// tady se bude resit update cache + reakce
		switch(packet.operation) {
			case ARP_REQUEST:
				// ulozit si odesilatele
				arpCache.updateArpCache(packet.senderIpAddress, packet.senderMacAddress, iface);
				// jsem ja target? Ano -> poslat ARP reply
//				if (packet.targetIpAddress.equals() ) {
					// poslat ARP reply
//				ArpPacket arp = new ArpPacket(packet.senderIpAddress, packet.senderMacAddress, null, iface.getMac()); // TODO: target IP address
//				netMod.ethernetLayer.sendPacket(arp, iface, packet.senderMacAddress);
//				}
				break;

			case ARP_REPLY:
				// ulozit si target
				// kdyz uz to prislo sem, tak je jasne, ze ta odpoved byla pro me, takze si ji muzu ulozit a je to ok
				arpCache.updateArpCache(packet.targetIpAddress, packet.targetMacAddress, iface);
				break;
		}



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
		// 3) zjisti MAC adresu z ARP cache - je=OK, neni=vygenerovat ARP request a vlozit do arpBuffer

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

			if (!arpBuffer.isEmpty()) {
				// TODO: domyslet, KDY bude obskoceno !!!
			}
		}
	}

}
