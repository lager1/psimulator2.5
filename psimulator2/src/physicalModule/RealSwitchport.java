/*
 * Erstellt am 20.3.2012.
 */
package physicalModule;

import dataStructures.L2Packet;
import java.util.ArrayList;
import java.util.List;
import logging.*;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

/**
 * This is switchport to connect to real interface.
 *
 * Switchport slouzici ke spojeni s realnym pocitacem. Posila a prijima pakety do a z realny site, k tomu ma Catcher a
 * Sender. Nebezi ve vlastnim vlakne, ale catcher a sender bezi.
 *
 * @author neiss
 */
public class RealSwitchport extends Switchport implements Loggable {

	private PacketCatcher catcher;
	private PacketSender sender;
	private boolean isStarted = false;
	private Pcap pcap;
	private String ifaceName;


// konstruktory a startovani (nestartuje se pri konstrukci ale az na vyzadani: --------------------------------------

	public RealSwitchport(PhysicMod physicMod, int number, int configID) {
		super(physicMod, number, configID);
		log(Logger.DEBUG,"Byl vytvoren realnej switchport c. "+number+" s configID "+configID, null);
	}

	/**
	 * Nastartuje spojeni s realnou siti
	 * @param ifaceName
	 * @return 0 - vsechno v poradku, </br> 1 nepodarilo se otevrit spojeni.
	 */
	public synchronized int start(String ifaceName){
		// ze vseho nejdriv se pokusim otevrit spojeni s realnym pocitacem
		pcap = otevriSpojeni(ifaceName);
		if(pcap == null){
			log(Logger.WARNING, "Nepodarilo se spojeni s realnym pocitacem.", null);
			return 1;
		} else {
			this.ifaceName = ifaceName;
			log(Logger.DEBUG, "Otevrel jsem spojeni na interfacu "+ifaceName, null);
		}

		// kdyz je spojeni otevreno, spoustim obsluhu:
		catcher = new PacketCatcher(pcap, this);
		isStarted = true;
		sender = new PacketSender(pcap, this);
		return 0;
	}




// metody zdedeny po Switchportu, ktery musej bejt implementovany: ---------------------------------------------------

	@Override
	protected void sendPacket(L2Packet packet) {
		if(isStarted){
			log(Logger.DEBUG, "Jdu poslat paket.", packet);
			sender.sendPacket(packet);
		} else {
			log(Logger.WARNING, "Doslo k pokusu odeslat paket, napojeni na realnou sit vsak neni funkcni.", null);
		}
	}

	@Override
	protected void receivePacket(L2Packet packet) {
		log(Logger.DEBUG, "Chytil jsem paket.", packet);
		physicMod.receivePacket(packet, this);
	}


	@Override
	public boolean isConnected() {
		return isStarted;
	}


	private Pcap otevriSpojeni(String ifaceName) {

		StringBuilder errbuf = new StringBuilder(); // For any error msgs

		List<PcapIf> alldevs = new ArrayList<>(); // Will be filled with NICs


		// listovani rozhrani, zkopiroval jsem to a az pak jsem zjistil, ze je to zbytecny, uz to tady ale nechavam:
		PcapIf iface = null;

		// nejdriv si najdu vsechny rozhrani:
		int r = Pcap.findAllDevs(alldevs, errbuf);
		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
			log(Logger.WARNING,"Can't read list of devices. Do you run psimulator as root?", null);
			return null;
		}
		log(Logger.DEBUG,"Network devices found.", null);

		// vyberu spravny rozhrani:
		for (PcapIf device : alldevs) {
			if(device.getName().equals(ifaceName)){
				iface = device;
			}
		}
		if(iface != null){
			log(Logger.DEBUG, "Found and selected iface "+iface.getName(), null);
		} else {
			log(Logger.WARNING, "Iface "+ifaceName+" not found. Connection with real network is not working!", null);
			return null;
		}


		// otevirani rozhrani:
		int snaplen = 64 * 1024;           // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000;           // 10 seconds in millis
		Pcap pcap =
				Pcap.openLive(iface.getName(), snaplen, flags, timeout, errbuf);

		if (pcap == null) {
			log(Logger.WARNING, "Error while opening device for capture: "+ errbuf.toString(), null);
		}

		return pcap;
	}


// ruzny pomocny a informativni metody: -------------------------------------------------------------------------------

	@Override
	public String getDescription() {
		return getDeviceName()+": RealSwitchport "+number;
	}

	private void log(int logLevel, String msg, Object obj){
		Logger.log(this, logLevel, LoggingCategory.REAL_NETWORK, msg, obj);
	}

	@Override
	public boolean isReal() {
		return true;
	}

	public String getIfaceName() {
		return ifaceName;
	}

	/**
	 * Zkratka pro logovani.
	 * @return
	 */
	public String getDeviceName(){
		return physicMod.device.getName();
	}





}
