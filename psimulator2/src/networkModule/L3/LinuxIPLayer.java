/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.IpPacket;
import dataStructures.L4Packet;
import dataStructures.ipAddresses.IpAddress;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.TcpIpNetMod;

/**
 * Linux-specific IPLayer.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class LinuxIPLayer extends IPLayer {

	public LinuxIPLayer(TcpIpNetMod netMod) {
		super(netMod);
		this.ttl = 60;
	}

	@Override
	public void handleSendPacket(L4Packet packet, IpAddress dst) {

		if (isItMyIpAddress(dst)) {
			IpPacket p = new IpPacket(dst, dst, this.ttl, packet);

			handleReceivePacket(p, null); // rovnou ubsluz v mem vlakne
			return;
		}

		RoutingTable.Record record = routingTable.findRoute(dst);
		if (record == null) { // kdyz nemam zaznam na v RT, tak zahodim
			Logger.log(this, Logger.INFO, LoggingCategory.NET, "Zahazuji tento packet, protoze nejde zaroutovat", packet);
			return;
		}

		IpPacket p = new IpPacket(record.rozhrani.getIpAddress().getIp(), dst, this.ttl, packet);

		processPacket(p, record, null);
	}
}
