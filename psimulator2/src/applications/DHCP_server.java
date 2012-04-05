/*
 * Erstellt am 3.4.2012.
 */

package applications;

import dataStructures.DhcpConfiguration;
import dataStructures.packets.IpPacket;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.DhcpPacket;
import dataStructures.packets.UdpPacket;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Tomas Pitrinec
 */
public class DHCP_server extends Application {

	// konfigurace:
	DhcpConfiguration config;


	public DHCP_server(String name, Device device) {
		super(name, device);
		port = 67;
	}

	@Override
	public void doMyWork() {
		if(!buffer.isEmpty()){
			handlePacket( buffer.remove(0));
		}
	}

	private void handlePacket(PacketItem item) {
		IpPacket ip = item.packet;
		try {
			UdpPacket udp = (UdpPacket) ip.data;
			DhcpPacket dhcp = (DhcpPacket) udp.getData();
			dhcp.getSize();	// abych si overil, ze to neni null
		} catch (Exception ex) {
			log(Logger.INFO, "DHCP serveru prisel spatnej paket.", ip);
			return;
		}

//		if(dhcp)
	}

	@Override
	public String getDescription() {
		return device.getName()+"_DHCP_server";
	}

	@Override
	protected void atStart() {
		// tady bude muset bejt nejaky nacitani konfigurace
	}

	@Override
	protected void atExit() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void atKill() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private void log(int logLevel, String msg, Object obj) {
		Logger.log(this, logLevel, LoggingCategory.DHCP, msg, obj);
	}

	public class DhcpServerConfiguration {

		IPwithNetmask subnetAndNetmask;

		/**
		 * First address, that can be assigned.
		 */
		IpAddress rangeStart;
		/**
		 * Last address, that can be assigned.
		 */
		IpAddress rangeEnd;
		IpAddress routers;
		IpAddress broadcast;

		IpAddress nextAddress;

		public DhcpServerConfiguration(IPwithNetmask subnetAndNetmask, IpAddress rangeStart,
				IpAddress rangeEnd, IpAddress routers, IpAddress broadcast) {
			this.subnetAndNetmask = subnetAndNetmask;
			this.rangeStart = rangeStart;
			this.rangeEnd = rangeEnd;
			this.routers = routers;
			this.broadcast = broadcast;
			nextAddress = rangeStart;
		}

		public IPwithNetmask getNextAddress() {
			IPwithNetmask vratit = new IPwithNetmask(nextAddress, subnetAndNetmask.getMask());
			if (nextAddress.equals(rangeEnd)) {	// TODO tady bych mel zjistit, jak to opravdu funguje
				log(Logger.INFO , "Vycerpan rozsah ip adres.", null);
				nextAddress = rangeStart;
			} else {
				nextAddress = IpAddress.nextAddress(nextAddress);
			}
			return vratit;
		}




	}

}
