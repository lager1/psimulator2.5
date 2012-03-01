/*
 * created 1.2.2012
 */

package networkModule.L4;

import dataStructures.L4Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import networkModule.Layer;
import networkModule.NetMod;

/**
 * Implementace transportni vrstvy sitovyho modulu.
 * Nebezi v vlastnim vlakne, je to vlastne jen rozhrani mezi aplikacema a 3. vrstvou.
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TcpIpLayer extends Layer {

	private final List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());

	public TcpIpLayer(NetMod netMod) {
		super(netMod);
	}

	public void receivePacket(L4Packet packet) {
//		receiveBuffer.add(new ReceiveItem(packet));
//		worker.wake();
	}

	private class ReceiveItem {

		final L4Packet packet;
//		final EthernetInterface iface;

		public ReceiveItem(L4Packet packet) {
			this.packet = packet;
		}
	}
}
