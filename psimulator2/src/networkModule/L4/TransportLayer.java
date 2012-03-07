/*
 * created 1.2.2012
 */

package networkModule.L4;

import dataStructures.IpPacket;
import dataStructures.L3Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.Layer;
import networkModule.NetMod;
import networkModule.TcpIpNetMod;

/**
 * Implementace transportni vrstvy sitovyho modulu.
 * Nebezi v vlastnim vlakne, je to vlastne jen rozhrani mezi aplikacema a 3. vrstvou.
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TransportLayer extends Layer implements Loggable {

//	private final List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());

	public final IcmpHandler icmphandler;

	public TransportLayer(TcpIpNetMod netMod) {
		super(netMod);
		this.icmphandler = new IcmpHandler(netMod, netMod.ipLayer);

	}

	public void receivePacket(IpPacket packet) {
		if (packet.data == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Prisel mi sem paket, ktery nema zadna L4 data! Zahazuji packet:", packet);
			return;
		}

		switch (packet.data.getType()) {
			case ICMP:
				icmphandler.handleReceivedIcmpPacket(packet);
				break;

			case TCP:
				Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "TCP handler neni implementovan! Zahazuji packet:", packet);
				break;

			case UDP:
				Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "UDP handler neni implementovan! Zahazuji packet:", packet);
				break;
			default:
				Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Prisel mi sem paket neznameho L4 typu: Zahazuji packet:", packet);
		}




//		receiveBuffer.add(new ReceiveItem(packet));
//		worker.wake();
	}

	@Override
	public String getDescription() {
		return netMod.getDevice().getName() + ": TcpIpLayer";
	}

//	private class ReceiveItem {
//
//		final L3Packet packet;
////		final EthernetInterface iface;
//
//		public ReceiveItem(L3Packet packet) {
//			this.packet = packet;
//		}
//	}
}
