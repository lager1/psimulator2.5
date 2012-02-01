/*
 * created 31.1.2012
 */

package networkModule.L3;

import dataStructures.EthernetPacket;
import dataStructures.L2Packet;
import dataStructures.L4Packet;
import dataStructures.ipAddresses.IpAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import networkModule.Layer;
import networkModule.NetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * TODO: pridat paketovy filtr (NAT, ..).
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer extends Layer implements SmartRunnable {

	private static class SendItem {
		L4Packet packet;
		IpAddress dst;

		public SendItem(L4Packet packet, IpAddress dst) {
			this.packet = packet;
			this.dst = dst;
		}
	}

	protected WorkerThread worker = new WorkerThread(this);

	/**
	 * ARP cache table.
	 */
	private ArpCache arpCache = new ArpCache();

	private List<L2Packet> receiveBuffer = Collections.synchronizedList(new LinkedList<L2Packet>());
	private List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	/**
	 * Zde budou pakety, ktere je potreba odeslat, ale nemam ARP zaznam, takze byla odeslana ARP request, ale jeste nemam odpoved.
	 * Obsluhovat me bude doMyWork().
	 */
	private List<SendItem> arpBuffer = Collections.synchronizedList(new LinkedList<SendItem>());

	public IPLayer(NetMod netMod) {
		super(netMod);
	}

	public HashMap<IpAddress,ArpCache.ArpRecord> getArpCache() {
		return arpCache.getCache();
	}

	public void receivePacket(L2Packet packet) {
		receiveBuffer.add(packet);
		worker.wake();
	}

	private void handleReceivePacket(L2Packet apacket) {
		EthernetPacket packet = (EthernetPacket) apacket;
		switch (packet.getEthertype()) {
			case ARP:
				// tady se bude resit zda je to ARP packet, update cache + reakce

				break;

			case IPv4:

				break;

			default:
				// TODO: ?
		}
	}

	public void sendPacket(L4Packet packet, IpAddress dst) {
		sendBuffer.add(new SendItem(packet, dst));
		worker.wake();
	}

	private void handleSendPacket(L4Packet packet, IpAddress dst) {
		// tady bude resit mam zaznam v ARP cache?	Ne, dej do odkladiste a obsluz pozdeji && vygeneruj ARP request
		//											Ano, predej spodni vrstve (a routuj)
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public void doMyWork() {

		// prochazet jednotlivy buffery a vyrizovat jednotlivy pakety
		while (!sendBuffer.isEmpty() || !receiveBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				L2Packet packet = receiveBuffer.remove(0);
				handleReceivePacket(packet);
			}

			if (!sendBuffer.isEmpty()) {
				SendItem m = sendBuffer.remove(0);
				handleSendPacket(m.packet, m.dst);
			}

			if (!arpBuffer.isEmpty()) {
				// domyslet, aby KDY pracovalo !!!
			}
		}
	}

}
