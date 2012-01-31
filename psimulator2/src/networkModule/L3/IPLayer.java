/*
 * created 31.1.2012
 */

package networkModule.L3;

import dataStructures.L2Packet;
import dataStructures.L4Packet;
import dataStructures.ipAddresses.IpAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import networkModule.NetMod;

/**
 * TODO: pridat paketovy filtr (NAT, ..).
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer extends L3layer {

	private ArpCache arpCache = new ArpCache();

	private List<L4Packet> sendBuffer = Collections.synchronizedList(new LinkedList<L4Packet>());
	private List<L2Packet> receiveBuffer = Collections.synchronizedList(new LinkedList<L2Packet>());

	public IPLayer(NetMod netMod) {
		super(netMod);
	}

	public HashMap<IpAddress,ArpCache.ArpRecord> getArpCache() {
		return arpCache.getCache();
	}

	@Override
	public void receivePacket(L2Packet packet) {
		receiveBuffer.add(packet);
		worker.wake();

//		EthernetPacket epacket = (EthernetPacket) packet;

//		switch (packet.getEthertype()) {
//			case ARP:
//				// tady se bude resit zda je to ARP packet, update cache + reakce
//
//				break;
//
//			case IPv4:
//
//				break;
//
//			default:
//		}
	}

	@Override
	public void sendPacket(L4Packet packet) {
		sendBuffer.add(packet);
		worker.wake();

		// tady bude resit mam zaznam v ARP cache?	Ne, dej do odkladiste a obsluz pozdeji && vygeneruj ARP request
		//											Ano, predej spodni vrstve
	}

	public void doMyWork() {

		// prochazet jednotlivy buffery a vyrizovat jednotlivy pakety
		while (!sendBuffer.isEmpty() || !receiveBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {

			}

			if (!sendBuffer.isEmpty()) {

			}
		}
	}

}
