/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.EthernetPacket;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import dataStructures.MacAddress;
import java.util.*;
import logging.*;
import networkModule.Layer;
import networkModule.NetMod;
import psimulator2.Psimulator;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Tady bude veskera implementace ethernetu a to jak pro switch, tak i router. TODO: vubec neni hotovy
 *
 * @author neiss
 */
public class EthernetLayer extends Layer implements SmartRunnable, Loggable {

	private boolean ladiciVypisovani = true;
	protected WorkerThread worker = new WorkerThread(this);
	protected Map<Integer, SwitchportSettings> switchports = new HashMap<Integer, SwitchportSettings>(); //TODO: naplnit
	private List<EthernetInterface> ifaces = new ArrayList<EthernetInterface>();
	private List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	private List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());

// Verejny metody pro sitovou komunikaci: ----------------------------------------------------------------------------------------------------
	public void receivePacket(L2Packet packet, int switchportNumber) {
		receiveBuffer.add(new ReceiveItem(packet, switchportNumber));
		worker.wake();
	}

	public void sendPacket(L3Packet packet, EthernetInterface iface, MacAddress target) {
		sendBuffer.add(new SendItem(packet, iface, target));
		worker.wake();
	}

	
// Ostatni verejny metody: --------------------------------------------------------------------------------------------------------------
	
	public void doMyWork() {
		while (!(sendBuffer.isEmpty() && receiveBuffer.isEmpty())) {
			if (!sendBuffer.isEmpty()) {
				SendItem it = sendBuffer.remove(0);
				handleSendPacket(it.packet, it.iface, it.target);
			}
			if (!receiveBuffer.isEmpty()) {
				ReceiveItem it = receiveBuffer.remove(0);
				handleReceivePacket(it.packet, it.switchportNumber);
			}
		}
	}
	
	public String getDescription() {
		return netMod.getDevice().getName()+": EthernetLayer";
	}

// Privatni metody resici sitovou komunikaci: ------------------------------------------------------------------------
	/**
	 * Obsluhuje pakety, ktery dala sitova vrstva k odeslani.
	 *
	 * @param packet
	 * @param iface
	 * @param target
	 */
	private void handleSendPacket(L3Packet packet, EthernetInterface iface, MacAddress target) {
		EthernetPacket p = new EthernetPacket(iface.getMac(), target, packet.getType(), packet);
		dispatchPacket(iface, p);
	}

	/**
	 *
	 * Obsluhuje pakety, ktery dostal sitovej modul od fysickyho.
	 *
	 * @param packet
	 * @param switchportNumber
	 */
	private void handleReceivePacket(L2Packet packet, int switchportNumber) {
		if (packet.getClass() != EthernetPacket.class) {	//kontrola spravnosti paketu
			Psimulator.getLogger().logg (this, Logger.ERROR, LoggingCategory.ETHERNET_LAYER,
					"Zahazuju paket, protoze neni tridy " + packet.getClass().getName(), null);
		}

		EthernetPacket p = (EthernetPacket) packet;

		SwitchportSettings swport = switchports.get(switchportNumber);
		if (swport == null) {
			Psimulator.getLogger().logg(this, Logger.ERROR, LoggingCategory.ETHERNET_LAYER, 
					("Prisel paket na switchport, o jehoz existenci nemam tuseni: switchport c.: " + switchportNumber), null);
		}
		//kontrola, bylo-li nalezeno rozhrani
		EthernetInterface iface = swport.assignedInterface;
		if (iface == null) {
			Psimulator.getLogger().logg(this, Logger.WARNING, LoggingCategory.ETHERNET_LAYER, "Nenalezeno interface ke switchportu, prusvih!", null);
		}

		iface.addSwitchTableItem(p.getSrc(), swport);
		if (p.getDst().equals(iface.getMac())) {	//pokud je paket pro me
			handlePacketForMe(p);
		} else {
			if (iface.switchingEnabled) {
				dispatchPacket(iface, p);
			} else {
				ladiciVypisovani("Na rozhrani " + iface.name + " zahazuju paket, protoze neni urcenej pro me a ja nemam povoleny switchovani.");
			}
		}

	}

	/**
	 * Stara se o samotny odeslani paketu.
	 *
	 * @param iface
	 * @param packet
	 */
	private void dispatchPacket(EthernetInterface iface, EthernetPacket packet) {
		SwitchportSettings swport = iface.getSwitchport(packet.getDst());
		if (swport == null) { // switchport nenalezen
			iface.dispatchPacketOnAllSwitchports(packet);	// interface to odesle na vsechny porty
		} else {
			netMod.getPhysicMod().sendPacket(packet, swport.switchportNumber); //odeslu to po tom najitym switchportu
		}
	}

	//TODO
	private void handlePacketForMe(EthernetPacket p) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	
	
// ostatni privatni metody: -----------------------------------------------------------------------------------------------------------
	private void ladiciVypisovani(String zprava) {
		if (ladiciVypisovani) {
			System.out.println("EthernetLayer: " + zprava);
		}
	}

	

// tridy pro polozky v bufferech: ------------------------------------------------------------------------------------------------------------
	
	private class SendItem {

		L3Packet packet;
		EthernetInterface iface;
		MacAddress target;

		public SendItem(L3Packet packet, EthernetInterface iface, MacAddress target) {
			this.packet = packet;
			this.iface = iface;
			this.target = target;
		}
	}

	private class ReceiveItem {

		L2Packet packet;
		int switchportNumber;

		public ReceiveItem(L2Packet packet, int switchportNumber) {
			this.packet = packet;
			this.switchportNumber = switchportNumber;
		}
	}

// Konstruktory a nastavovani pri startu: -------------------------------------------------------------------------------------------------
	public EthernetLayer(NetMod netMod) {
		super(netMod);
		exploreHardware();
		assignInterfacesToSwitchports();
	}

	/**
	 * Explores hardware. Pri startu projde vsechny switchporty fysickyho modulu a nacte si je.
	 */
	private void exploreHardware() {
		List<Integer> swportsNumbers = netMod.getPhysicMod().getNumbersOfPorts();
		for (int i : swportsNumbers) {
			switchports.put(i, new SwitchportSettings(i));
		}
	}

	/**
	 * Z konfigurace priradi interfacy ke switchportum. TODO: implementovat
	 */
	private void assignInterfacesToSwitchports() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
