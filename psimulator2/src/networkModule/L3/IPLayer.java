/*
 * created 31.1.2012
 */

package networkModule.L3;

import dataStructures.L2Packet;
import dataStructures.L4Packet;
import networkModule.NetMod;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TODO: pridat paketovy filtr (NAT, ..).
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IPLayer extends L3layer {

	public IPLayer(NetMod netMod) {
		super(netMod);
	}

	@Override
	public void receivePacket(L2Packet packet) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendPacket(L4Packet packet) {
		// tady bude resit mam zaznam v ARP cache?	Ne, dej do odkladiste a obsluz pozdeji && vygeneruj ARP request
		//											Ano, predej spodni vrstve
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void doMyWork() {
		// tady bude muset resit

		throw new UnsupportedOperationException("Not supported yet.");
	}

}
