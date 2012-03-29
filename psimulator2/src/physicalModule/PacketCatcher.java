/*
 * Erstellt am 20.3.2012.
 */

package physicalModule;

import dataStructures.*;
import dataStructures.ipAddresses.IpAddress;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.JProtocol;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Arp;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Icmp.Echo;
import org.jnetpcap.protocol.network.Ip4;

/**
 * Class for capturing packets from real network.
 * 
 * Trida na chytani paketu z realny site. Bezi v uplne vlastim vlakne!
 * @author Tomas Pitrinec
 */
public class PacketCatcher implements Runnable, Loggable {

	Pcap pcap;
	RealSwitchport swport;
	Thread myThread;


	public PacketCatcher(Pcap pcap, RealSwitchport swport) {
		this.pcap = pcap;
		this.swport = swport;

		// startovani, vse uz musi byt nacteno!
		myThread = new Thread(this, getDescription());
		log(Logger.DEBUG, "Jdu startovat.", null);
		myThread.start();
	}

	/**
	 * Metoda hlavni smycky, chyta pakety a nechava je zpracovat.
	 */
	@Override
	public void run() {

		// vytvorim jednoduchej packet handler, kterej vola moji fci:
		PcapPacketHandler packetHandler = new PcapPacketHandler() {
			@Override
			public void nextPacket(PcapPacket packet, Object user) {
				processLinkLayer(packet);
			}
		};

		// spustim nekonecnou smycku:
		pcap.loop(-1, packetHandler, null);
	}


	/**
	 * Translates ethernet II packets, than calls the translation of network layer and gives the L2Packet to the swport.
	 * @param packet
	 */
	private void processLinkLayer(PcapPacket packet){

		if (packet.hasHeader(JProtocol.ETHERNET_ID)) { // kdyz to ma ethernetovou hlavicku
			// nactu si hlavicku
			Ethernet ethHeader;
			ethHeader = new Ethernet();
			packet.getHeader(ethHeader);

			// nactu si mac adresy:
			MacAddress src=new MacAddress(ethHeader.source());
			MacAddress dst=new MacAddress(ethHeader.destination());

			// necham zpracovat paket vyssi vrstvy:
			L3Packet data = processNetLayer(packet);

			//vytvorim paket:
			EthernetPacket p;
			if(data!=null){
				p = new EthernetPacket (src,dst,data.getType(), data);	// typ se dava podle skutecnosti, ne podle toho, co bylo zadano v puvodnim paketu
			} else {
				p = new EthernetPacket (src,dst,L3Packet.L3PacketType.UNKNOWN);
			}

			//poslu paket:
			swport.receivePacket(p);

		} else {	// je to nejakej neznamej typ, loguju to jako info
			log(Logger.INFO, "Chytil jsem paket s neznamou hlavickou na linkovy vrstve.", null);
		}
	}

	/**
	 * Translates the packets on net layer, for now translates the ARP and IPv4 packets.
	 * @param packet
	 * @return
	 */
	private L3Packet processNetLayer(PcapPacket packet){
		L3Packet p = null;

		if (packet.hasHeader(JProtocol.ARP_ID)) {

			// nactu hlavicku:
			Arp arpHeader = new Arp();
			packet.getHeader(arpHeader);

			// zjistim parametry:
			Arp.OpCode opCode = arpHeader.operationEnum();	// typ (reply, request)

			IpAddress senderIP = new IpAddress(arpHeader.spa());	// vykoukal jsem to z nejakejch anotaci v javadocu
			MacAddress senderMac = new MacAddress(arpHeader.sha());
			IpAddress targetIP = new IpAddress(arpHeader.tpa());
			MacAddress targetMac = new MacAddress(arpHeader.tha());

			//vytvorim paket:
			if(opCode==Arp.OpCode.REPLY){
				p=new ArpPacket(senderIP, senderMac, targetIP, targetMac);
			} else {
				p=new ArpPacket(senderIP,senderMac,targetIP);
			}

		} else if (packet.hasHeader(JProtocol.IP4_ID)) {
			// nactu hlavicku:
			Ip4 ipHeader = new Ip4();
			packet.getHeader(ipHeader);

			// nactu dulezity data
			IpAddress src = IpAddress.createIpFromBits(ipHeader.sourceToInt());
			IpAddress dst = IpAddress.createIpFromBits(ipHeader.destinationToInt());
			int ttl = ipHeader.ttl();

			//necham zpracovat protokol vyssi vrstvy:
			L4Packet  data = processTransportLayer(packet);

			// nakonec ten paket vytvorim
			p = new IpPacket(src, dst, ttl, data);	// u ip se nijak neresi protokol vyssi vrstvy
		} else {
			// Here you can add translations of other headers on network layer.
			log(Logger.INFO, "Chytil jsem paket s neznamou hlavickou na sitovy vrstve.", null);
		}

		return p;
	}

	/**
	 * Translates the packets on transport layer, for now translates only ICMP echo/reply packets.
	 * @param packet
	 * @return
	 */
	private L4Packet processTransportLayer(PcapPacket packet) {
		L4Packet p = null;
		if(packet.hasHeader(JProtocol.ICMP_ID)){
			// ziskam hlavicku
			Icmp icmp = new Icmp();
			packet.getHeader(icmp);

			// ziskam typ a kod:
			int type = icmp.type();
			int code = icmp.code();

			if (icmp.hasSubHeader(Icmp.IcmpType.ECHO_REPLY_ID) || icmp.hasSubHeader(Icmp.IcmpType.ECHO_REQUEST_ID)) {	// je to request nebo reply
				Echo icmpEcho;
				if (icmp.hasSubHeader(Icmp.IcmpType.ECHO_REPLY_ID))	// i kdyz je to jedno, tak to musim takhle rozlisovat
					icmpEcho = new Icmp.EchoReply();
				else
					icmpEcho = new Icmp.EchoRequest();
				icmp.getSubHeader(icmpEcho);	// nactu si podhlavicku
				int identifier = icmpEcho.id();
				int sequence = icmpEcho.sequence();
				byte [] data = icmp.getPayload();
				try {
					p = new IcmpPacket(type, code, identifier, sequence, data);
				} catch (Exception ex) {
					log(Logger.INFO,ex.getMessage(), null);
				}
			} else {
				// Here you can add the translation of other ICMP packets.
			}
			icmp.getPayload();



		} else {
			// Here you can add translations of other headers on transport layer.
			log(Logger.INFO, "Chytil jsem paket s neznamou hlavickou na transportni vrstve.", null);
		}

		return p;
	}

	@Override
	public String getDescription() {
		return swport.getDescription()+": cather";
	}

	private void log(int logLevel, String msg, Object obj){
		Logger.log(this, logLevel, LoggingCategory.REAL_NETWORK, msg, obj);
	}



}
