/*
 * created 31.1.2012
 */

package networkModule.L3;

import dataStructures.EthernetPacket;
import dataStructures.L2Packet;
import dataStructures.L4Packet;
import networkModule.NetMod;

/**
 * TODO: pridat paketovy filtr (NAT, ..).
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer extends L3layer {

	ArpCache arpCache = new ArpCache();

	public IPLayer(NetMod netMod) {
		super(netMod);
	}

	@Override
	public void receivePacket(L2Packet apacket) {
		assert apacket.getClass() == EthernetPacket.class;
		EthernetPacket packet = (EthernetPacket) apacket;


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


		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendPacket(L4Packet packet) {
		// tady bude resit mam zaznam v ARP cache?	Ne, dej do odkladiste a obsluz pozdeji && vygeneruj ARP request
		//											Ano, predej spodni vrstve
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void doMyWork() {
		// prochazet jednotlivy buffery a vyrizovat jednotlivy pakety

		throw new UnsupportedOperationException("Not supported yet.");
	}

}
