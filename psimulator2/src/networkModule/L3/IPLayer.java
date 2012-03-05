/*
 * created 31.1.2012
 *
 * TODO:
 * doresit drobnosti (potreba vyzkoumat)
 * doresit logovani
 * doresit budik
 * dopsat tridu starajici se o ICMP - IcmpHandler
 *
 */
package networkModule.L3;

import dataStructures.*;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import exceptions.UnsupportedL3TypeException;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.L3.RoutingTable.Record;
import networkModule.TcpIpNetMod;
import psimulator2.Psimulator;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents IP layer of ISO/OSI model.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer implements SmartRunnable, Loggable {

	protected final WorkerThread worker;
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
	private final List<StoreItem> storeBuffer = new LinkedList<StoreItem>();
	/**
	 * Routing table with record.
	 */
	public final RoutingTable routingTable = new RoutingTable();
	/**
	 * Link to network module.
	 */
	private final TcpIpNetMod netMod;
	/**
	 * When some ARP reply arrives this is set to true so doMyWork() can process storeBuffer. After processing
	 * storeBuffer it is set to false.
	 */
	private boolean newArpReply = false;
	private final Map<String, NetworkInterface> networkIfaces = new HashMap<String, NetworkInterface>();
	/**
	 * Waiting time for ARP requests.
	 */
	private long arpTTL = 10000;

	/**
	 * Handles ICMP answers, sends ICMP packet if neeeded.
	 */
	private final IcmpHandler icmpHandler;

	/**
	 * Constructor of IP layer.
	 * Empty routing table is also created.
	 * @param netMod
	 */
	public IPLayer(TcpIpNetMod netMod) {
		this.netMod = netMod;
		this.icmpHandler = new IcmpHandler(netMod, this);
		this.worker = new WorkerThread(this);
	}
	/**
	 * Default TTL values.
	 */
	public int ttl = 255;

	/**
	 * Getter for cisco & linux listing.
	 *
	 * @return
	 */
	public HashMap<IpAddress, ArpCache.ArpRecord> getArpCache() {
		return arpCache.getCache();
	}

	/**
	 * Getter for Saver.
	 * @return
	 */
	public Collection<NetworkInterface> getNetworkIfaces() {
		return networkIfaces.values();
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

	/**
	 * Process storeBuffer which is for packets without known MAC nextHop.
	 */
	private void handleStoreBuffer() {

		long now = System.currentTimeMillis();

		List<StoreItem> remove = new ArrayList<StoreItem>();

		for (StoreItem m : storeBuffer) {

			if (now - m.timeStamp > arpTTL) { // vice jak arpTTL [s] stare se smaznou, tak se posle zpatky DHU
				remove.add(m);
				icmpHandler.sendDestinationHostUnreachable(m.packet.src, m.packet); // TODO: poslat zpatky DHU jen pokud je to ICMP REQ ?
				// TODO: kdyz mi neprijde zadna odpoved, tak se NIKDY neodesle 'destination host unreachable', takze budem muset implementovat asi nejakej budik, kterej me zbudi za 5s.
				continue;
			}

			MacAddress mac = arpCache.getMacAdress(m.nextHop);
			if (mac != null) {
				// obslouzit
				netMod.ethernetLayer.sendPacket(m.packet, m.out, mac);

				// vyndat z bufferu
				remove.add(m);
			}
		}

		// vymazani proslych ci obslouzenych zaznamu
		for (StoreItem m : remove) {
			storeBuffer.remove(m);
		}

		newArpReply = false;
	}

	private void handleReceiveIpPacket(IpPacket packet, EthernetInterface iface) {
		// odnatovat
		NetworkInterface ifaceIn = findIncommingNetworkIface(iface);
		packet = packetFilter.preRouting(packet, ifaceIn);

		// zpracovat paket
		handleSendIpPacket(packet, iface);
	}

	/**
	 * Handles packet: is it for me?, routing, decrementing TTL, postrouting, MAC address finding.
	 *
	 * @param packet
	 * @param iface incomming EthernetInterface, can be null
	 */
	private void handleSendIpPacket(IpPacket packet, EthernetInterface iface) {

		// je pro me?
		if (isItMyIpAddress(packet.dst)) {
			netMod.tcpipLayer.receivePacket(packet);
			return;
		}

		NetworkInterface ifaceIn = findIncommingNetworkIface(iface);

		// osetri TTL
		if (packet.ttl == 1) {
			// jestli je to ICMP, tak posli TTL expired
			// zaloguj zahozeni paketu
			Psimulator.getLogger().logg(this, Logger.IMPORTANT, LoggingCategory.NET, "Zahazuji tento packet, protoze vyprselo TTL", packet);
			icmpHandler.sendTimeToLiveExceeded(packet.src, packet);
			return;
		}

		// zaroutuj
		Record record = routingTable.findRoute(packet.dst);
		if (record == null) {
			if (isPacketIcmpRequest(packet)) { // nema to nahodou vracet DNU vzdy??
				icmpHandler.sendDestinationNetworkUnreachable(packet.src, packet);
			}
			Psimulator.getLogger().logg(this, Logger.IMPORTANT, LoggingCategory.NET, "Zahazuji tento packet, protoze nejde zaroutovat", packet);
			return;
		}

		// vytvor novy paket a zmensi TTL (kdyz je packet.src null, tak to znamena, ze je odeslan z toho sitoveho device
		//		a tedy IP adresa se musi vyplnit dle rozhrani, ze ktereho to poleze ven
		IpPacket p = new IpPacket(packet.src == null ? record.rozhrani.ipAddress.getIp() : packet.src, packet.dst, packet.ttl - 1, packet.data);

		// zanatuj
		p = packetFilter.postRouting(p, ifaceIn, record.rozhrani);

		// zjistit nexHopIp
		// kdyz RT vrati record s branou, tak je nextHopIp record.brana
		// kdyz je record bez brany, tak je nextHopIp uz ta hledana IP adresa
		IpAddress nextHopIp = p.dst;
		if (record.brana != null) {
			nextHopIp = record.brana;
		}

		// zjisti MAC adresu z ARP cache - je=OK, neni=vygenerovat ARP request a vlozit do storeBuffer + touch na sendItem, ktera bude v storeBuffer
		MacAddress nextHopMac = arpCache.getMacAdress(nextHopIp);
		if (nextHopMac == null) { // posli ARP request a dej do fronty
			ArpPacket arpPacket = new ArpPacket(record.rozhrani.ipAddress.getIp(), record.rozhrani.getMacAddress(), nextHopIp);
			netMod.ethernetLayer.sendPacket(arpPacket, record.rozhrani.ethernetInterface, MacAddress.broadcast()); // TODO: odeslat ARP req na vsechny rozhrani v dany siti?, asi NE

			storeBuffer.add(new StoreItem(p, record.rozhrani.ethernetInterface, nextHopIp));
			return;
		}

		// kdyz to doslo az sem, tak muzu odeslat..
		netMod.ethernetLayer.sendPacket(p, record.rozhrani.ethernetInterface, nextHopMac);
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

	protected void handleSendPacket(L4Packet packet, IpAddress dst) {
		IpPacket p = new IpPacket(null, dst, ttl, packet);
		handleSendIpPacket(p, null); // prichozi rozhrani je null, pac zadne takove neni
	}

	@Override
	public void doMyWork() {

		// prochazet jednotlivy buffery a vyrizovat jednotlivy pakety
		while (!sendBuffer.isEmpty() || !receiveBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				ReceiveItem m = receiveBuffer.remove(0);
				handleReceivePacket(m.packet, m.iface);
			}

			if (!sendBuffer.isEmpty()) {
				SendItem m = sendBuffer.remove(0);
				IpPacket p = new IpPacket(null, m.dst, ttl, m.packet);
				handleSendIpPacket(p, null); // prichozi je null, pac zadne takove neni
			}

			if (newArpReply && !storeBuffer.isEmpty()) {
				// bude obskoceno vzdy, kdyz se tam neco prida, tak snad ok
				handleStoreBuffer();
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
	public void setIpAddressOnInterface(NetworkInterface iface, IPwithNetmask ipAddress) {
		iface.ipAddress = ipAddress;
	}

	/**
	 * Adds Network interface to a list.
	 * This method is used only in loading configuration file.
	 * @param iface
	 */
	public void addNetworkInterface(NetworkInterface iface) {
		networkIfaces.put(iface.name, iface);
	}

	/**
	 * Returns true if targetIpAddress is on my NetworkInterface and isUp
	 *
	 * @param targetIpAddress
	 * @return
	 */
	private boolean isItMyIpAddress(IpAddress targetIpAddress) {
		for (NetworkInterface iface : networkIfaces.values()) {
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

	@Override
	public String getDescription() {
		return netMod.getDevice().getName() + ": IpLayer";
	}

	/**
	 * Returns NetworkInterface which belongs to the EthernetInterface inc. <br />
	 * Returns null iff inc is null.
	 * @param inc
	 * @return
	 */
	private NetworkInterface findIncommingNetworkIface(EthernetInterface inc) {
		if (inc == null) {
			return null;
		}
		NetworkInterface iface = getNetworkInteface(inc.name);
		if (iface == null) {
			throw new RuntimeException("Nenazeleno NetworkIface, ktere by bylo spojeno s EthernetInterface, ze ktereho prave prisel packet!");
		}
		return iface;
	}

	public NetworkInterface getNetworkInteface(String name) {
		return networkIfaces.get(name);
	}

	private class SendItem {

		final L4Packet packet;
		final IpAddress dst;

		public SendItem(L4Packet packet, IpAddress dst) {
			this.packet = packet;
			this.dst = dst;
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

	private class StoreItem {
		/**
		 * Packet to send.
		 */
		final IpPacket packet;
		/**
		 * Outgoing interface (gained from routing table).
		 */
		final EthernetInterface out;
		/**
		 * IP of nextHop (gained from routing table).
		 */
		final IpAddress nextHop;
		/**
		 * Time stamp - for handling old records (drop packet + send DHU).
		 */
		long timeStamp;

		/**
		 * Store item.
		 * @param packet packet to send
		 * @param out outgoing interface (gained from routing table)
		 * @param nextHop IP of nextHop
		 */
		public StoreItem(IpPacket packet, EthernetInterface out, IpAddress nextHop) {
			this.packet = packet;
			this.nextHop = nextHop;
			this.out = out;
			this.timeStamp = System.currentTimeMillis();
		}

		public void touch() {
			this.timeStamp = System.currentTimeMillis();
		}
	}
}
