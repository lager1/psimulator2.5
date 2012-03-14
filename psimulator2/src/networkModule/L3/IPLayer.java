/*
 * created 31.1.2012
 */
package networkModule.L3;

import dataStructures.*;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.L3.RoutingTable.Record;
import networkModule.L4.IcmpHandler;
import networkModule.TcpIpNetMod;
import psimulator2.Psimulator;
import utils.SmartRunnable;
import utils.Util;
import utils.Wakeable;
import utils.WorkerThread;

/**
 * Represents IP layer of ISO/OSI model.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class IPLayer implements SmartRunnable, Loggable, Wakeable {

	protected final WorkerThread worker;
	/**
	 * ARP cache table.
	 */
	protected final ArpCache arpCache = new ArpCache();
	/**
	 * Packet filter. Controls NAT, packet dropping, ..
	 */
	protected final PacketFilter packetFilter = new PacketFilter(this);
	private final List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());
	private final List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	/**
	 * Zde budou pakety, ktere je potreba odeslat, ale nemam ARP zaznam, takze byla odeslana ARP request, ale jeste
	 * nemam odpoved. Obsluhovat me bude doMyWork(). Neni potreba miti synchronizaci, protoze sem leze jen vlakno z
	 * doMyWork().
	 */
	protected final List<StoreItem> storeBuffer = new LinkedList<>();
	/**
	 * Routing table with record.
	 */
	public final RoutingTable routingTable = new RoutingTable();
	/**
	 * Link to network module.
	 */
	protected final TcpIpNetMod netMod;
	/**
	 * When some ARP reply arrives this is set to true so doMyWork() can process storeBuffer. After processing
	 * storeBuffer it is set to false.
	 */
	private transient boolean newArpReply = false;
	private final Map<String, NetworkInterface> networkIfaces = new HashMap<>();
	/**
	 * Waiting time [ms] for ARP requests.
	 */
	private long arpTTL = 10_000;
	/**
	 * Default TTL values.
	 */
	public int ttl = 255;
	/**
	 * Packet forwarding flag. <br />
	 * TODO: port_forward ulozit do filesystemu a nacitat od tam tud
	 */
	protected boolean ip_forward = true;
	/**
	 * Constructor of IP layer.
	 * Empty routing table is also created.
	 * @param netMod
	 */
	public IPLayer(TcpIpNetMod netMod) {
		this.netMod = netMod;
		this.worker = new WorkerThread(this);
	}

	/**
	 * Getter for cisco & linux listing.
	 *
	 * @return
	 */
	public HashMap<IpAddress, ArpCache.ArpRecord> getArpCache() {
		return arpCache.getCache();
	}

	/**
	 * Getter for Cisco commands.
	 * @return
	 */
	public NatTable getNatTable() {
		return packetFilter.getNatTable();
	}

	public IcmpHandler getIcmpHandler() {
		return netMod.transportLayer.icmphandler;
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

	/**
	 * Called from doMyWork() and from plaform-specific IPLayeres.
	 *
	 * @param packet
	 * @param iface incomming ethernet interface
	 */
	protected void handleReceivePacket(L3Packet packet, EthernetInterface iface) {
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
				Logger.log(this, Logger.WARNING, LoggingCategory.IP_LAYER, "Unsupported L3 type: " + packet.getType()+", zahazuji packet: ", packet);
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

	/**
	 * Process storeBuffer which is for packets without known MAC nextHop.
	 */
	private void handleStoreBuffer() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.IP_LAYER, "handleStoreBuffer(), size: "+storeBuffer.size(), null);

		long now = System.currentTimeMillis();

		List<StoreItem> remove = new ArrayList<>();

		for (StoreItem m : storeBuffer) {

			if (now - m.timeStamp >= arpTTL) { // vice jak arpTTL [s] stare se smaznou, tak se posle zpatky DHU
				remove.add(m);
				Logger.log(this, Logger.INFO, LoggingCategory.NET, "Vyprsel timout ve storeBufferu, zahazuju tento paket. Pak poslu zpatky DHU.", m.packet);
				getIcmpHandler().sendDestinationHostUnreachable(m.packet.src, m.packet);
				continue;
			}

			MacAddress mac = arpCache.getMacAdress(m.nextHop);
			if (mac != null) {
				// obslouzit
				Logger.log(this, Logger.INFO, LoggingCategory.NET, "Uz mi prisla ARPem MAC adresa nexthopu, tak vybiram ze storeBufferu packet a posilam ho.", m.packet);
				netMod.ethernetLayer.sendPacket(m.packet, m.out, mac);

				// vyndat z bufferu
				remove.add(m);
			}

			Logger.log(this, Logger.DEBUG, LoggingCategory.ARP, "Tento zaznam jeste nevyprsel a ani neprisla odpoved, stari: "+(now - m.timeStamp)+", maze se az: "+arpTTL, null);
		}

		// vymazani proslych ci obslouzenych zaznamu
		for (StoreItem m : remove) {
			storeBuffer.remove(m);
		}

		newArpReply = false;
	}

	/**
	 * Handles packet: is it for me?, routing, decrementing TTL, postrouting, MAC address finding.
	 *
	 * @param packet
	 * @param iface incomming EthernetInterface, can be null
	 */
	private void handleReceiveIpPacket(IpPacket packet, EthernetInterface iface) {
		// odnatovat
		NetworkInterface ifaceIn = findIncommingNetworkIface(iface);
		packet = packetFilter.preRouting(packet, ifaceIn);

		// je pro me?
		if (isItMyIpAddress(packet.dst)) { // TODO: cisco asi pravdepovodne se nejdriv podiva do RT, a asi tam bude muset byt zaznam na svoji IP, aby se to dostalo nahoru..
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
		Record record = routingTable.findRoute(packet.dst);
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

	/**
	 * Vezme packet, pusti se na nej postRouting, zjisti MAC cile a preda ethernetovy vrstve.
	 *
	 * @param packet co chci odeslaat
	 * @param record zaznam z RT
	 * @param ifaceIn prichozi rozhrani, null pokud odesilam novy paket
	 */
	protected void processPacket(IpPacket packet, Record record, NetworkInterface ifaceIn) {
		// zanatuj
		packet = packetFilter.postRouting(packet, ifaceIn, record.rozhrani); // prichozi rozhrani je null, protoze zadne takove neni

		// zjistit nexHopIp
		// kdyz RT vrati record s branou, tak je nextHopIp record.brana
		// kdyz je record bez brany, tak je nextHopIp uz ta hledana IP adresa
		IpAddress nextHopIp = packet.dst;
		if (record.brana != null) {
			nextHopIp = record.brana;
		}

		// zjisti MAC adresu z ARP cache - je=OK, neni=vygenerovat ARP request a vlozit do storeBuffer + touch na sendItem, ktera bude v storeBuffer
		MacAddress nextHopMac = arpCache.getMacAdress(nextHopIp);
		if (nextHopMac == null) { // posli ARP request a dej do fronty
			ArpPacket arpPacket = new ArpPacket(record.rozhrani.ipAddress.getIp(), record.rozhrani.getMacAddress(), nextHopIp);

			Logger.log(this, Logger.INFO, LoggingCategory.ARP, "Nemohu odeslat IpPacket na adresu " + packet.dst + ", protoze neznam MAC adresu nextHopu, takze posilam ARP request na rozhrani "
					+ record.rozhrani.name, arpPacket); // packet tu nemsi by
			netMod.ethernetLayer.sendPacket(arpPacket, record.rozhrani.ethernetInterface, MacAddress.broadcast());

			storeBuffer.add(new IPLayer.StoreItem(packet, record.rozhrani.ethernetInterface, nextHopIp));
			Psimulator.getPsimulator().budik.registerWake(this, arpTTL);
			return;
		}

		// kdyz to doslo az sem, tak muzu odeslat..
		Logger.log(this, Logger.INFO, LoggingCategory.NET, "Posilam paket.", packet);
		netMod.ethernetLayer.sendPacket(packet, record.rozhrani.ethernetInterface, nextHopMac);

	}

	/**
	 * Method for sending packet from layer 4 with system default TTL.
	 *
	 * @param packet data to be sent
	 * @param dst destination address
	 */
	public void sendPacket(L4Packet packet, IpAddress dst) {
		sendPacket(packet, dst, this.ttl);
	}

	/**
	 * Method for sending packet from layer 4.
	 *
	 * @param packet data to be sent
	 * @param dst destination address
	 * @param ttl Time To Live value
	 */
	public void sendPacket(L4Packet packet, IpAddress dst, int ttl) {
		sendBuffer.add(new SendItem(packet, dst, ttl));
		worker.wake();
	}

	/**
	 * Method for sending from IPLayer and IcmpHandler only! <br />
	 * In this method everything is in the same thread. <br />
	 * Don't use it from Layer 4!
	 *
	 * @param packet data to be sent
	 * @param dst destination address
	 */
	public abstract void handleSendPacket(L4Packet packet, IpAddress dst);

	@Override
	public void doMyWork() {
//		Logger.log(this, Logger.DEBUG, LoggingCategory.IP_LAYER, "doMyWork()", null);

		// prochazet jednotlivy buffery a vyrizovat jednotlivy pakety
		while (!sendBuffer.isEmpty() || !receiveBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.IP_LAYER, "doMyWork() cyklus receiveBuffer", null);
				ReceiveItem m = receiveBuffer.remove(0);
				handleReceivePacket(m.packet, m.iface);
			}

			if (!sendBuffer.isEmpty()) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.IP_LAYER, "doMyWork() cyklus sendBuffer", null);
				SendItem m = sendBuffer.remove(0);
				handleSendPacket(m.packet, m.dst); // bude se obsluhovat platform-specific
			}

			if (newArpReply && !storeBuffer.isEmpty()) { // ten boolean tam je proto, aby se to neprochazelo v kazdym cyklu
				// bude obskoceno vzdy, kdyz se tam neco prida, tak snad ok
				handleStoreBuffer();
			}
		}

		if (!storeBuffer.isEmpty()) { // ?
			// bude obskoceno vzdy, kdyz se tam neco prida, tak snad ok
			handleStoreBuffer();
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
	public void changeIpAddressOnInterface(NetworkInterface iface, IPwithNetmask ipAddress) {
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
	protected boolean isItMyIpAddress(IpAddress targetIpAddress) {
		for (NetworkInterface iface : networkIfaces.values()) {
			if (iface.ipAddress != null && iface.ipAddress.getIp().equals(targetIpAddress) && iface.isUp) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if packet is ICMP REQUEST.
	 * zatim nemazat!s
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
		return Util.zarovnej(netMod.getDevice().getName(), Util.deviceNameAlign) + " IPLayer";
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
			Logger.log(this, Logger.WARNING, LoggingCategory.IP_LAYER, "Nenazeleno NetworkIface, ktere by bylo spojeno s EthernetInterface, ze ktereho prave prisel packet!!!", null);
		}
		return iface;
	}

	/**
	 * Fill the routing table during simulator loading from addresses on interfaces.
	 * It should be called only if in the configuration file does'n exist any routing table configuration.
	 *
	 * Naplni routovaci tabulku dle adres na rozhranich, tak jak to dela linux.
	 * Volat jenom pri konfiguraci, nebyla-li routovaci tabulka ulozena.
	 */
	public void updateNewRoutingTable() {
		if (routingTable.size() != 0) {
			Logger.log(this, Logger.WARNING, LoggingCategory.IP_LAYER, "Spustena metoda updateNewRoutingTable, ktera je urcena pro vyplneni routovaci tabulky dle rozhrani, nebyla-li zadana v konfiguraku. "
					+ "Tabulka vsak neni prazdna.", null);
			return;
		}
		for (NetworkInterface iface : getNetworkIfaces()) {
			if (iface.getIpAddress() != null) {
				routingTable.addRecord(iface.ipAddress.getNetworkNumber(), iface);
			}
		}
	}

	/**
	 * Getter for Saver.
	 * @return
	 */
	public Collection<NetworkInterface> getNetworkIfaces() {
		return networkIfaces.values();
	}

	/**
	 * Return interface with name or null iff there is no such interface.
	 * @param name
	 * @return
	 */
	public NetworkInterface getNetworkInteface(String name) {
		return networkIfaces.get(name);
	}

	/**
	 * Return interface with name (ignores case) or null iff there is no such interface.
	 * @param name
	 * @return
	 */
	public NetworkInterface getNetworkIntefaceIgnoreCase(String name) {
		for (NetworkInterface iface : networkIfaces.values()) {
			if (iface.name.equalsIgnoreCase(name)) {
				return iface;
			}
		}
		return null;
	}

	/**
	 * Returns interfaces as collection sorted by interfaces name.
	 * Uzitecny pro vypisy u prikazu.
	 * @return
	 */
	public Collection<NetworkInterface> getSortedNetworkIfaces() {
		List<NetworkInterface> ifaces = new ArrayList<>(networkIfaces.values());
		Collections.sort(ifaces);
		return ifaces;
	}

	@Override
	public void wake() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.IP_LAYER, "vzbudil me Budik", null);
		newArpReply = true;
		this.worker.wake();
	}

	private class SendItem {

		final L4Packet packet;
		final IpAddress dst;
		final int ttl;

		public SendItem(L4Packet packet, IpAddress dst, int ttl) {
			this.packet = packet;
			this.dst = dst;
			this.ttl = ttl;
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
