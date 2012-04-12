/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.packets.L2Packet;
import device.Device;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.NetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 *
 * TODO: PhysicMod: pak nejak poresit velikosti bufferu
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public class PhysicMod implements SmartRunnable, Loggable{


	/**
	 * List of interfaces.
	 */
	private Map<Integer,Switchport> switchports = new HashMap<>();
	/**
	 * Queue for incomming packets from cabels.
	 */
	private final List<BufferItem> receiveBuffer = Collections.synchronizedList(new LinkedList<BufferItem>());
	/**
	 * Queue for incomming packets from network module.
	 */
	private final List<BufferItem> sendBuffer = Collections.synchronizedList(new LinkedList<BufferItem>());
	/**
	 * Working thread.
	 */
	private WorkerThread worker;

	/**
	 * Odkaz na PC.
	 */
	public final Device device;

	private boolean ladiciVypisovani = true;


// Konstruktory a vytvareni modulu: ----------------------------------------------------------------------------------------------

	public PhysicMod(Device device) {
		this.device = device;
		this.worker = new WorkerThread(this);
	}


	/**
	 * Pridani switchportu
	 * @param number cislo switchportu
	 * @param realSwitchport je-li switchport realnym rozhranim (tzn. vede k realnymu pocitaci)
	 * @param configID id z konfigurace - tedy ID u EthInterfaceModel
	 */
	public void addSwitchport(int number, boolean realSwitchport, int configID) {
		Switchport swport;
		if (!realSwitchport) {
			swport = new SimulatorSwitchport(this,number, configID);
		} else {
			swport =new RealSwitchport(this,number,configID);
		}
		switchports.put(swport.number, swport);
	}


// Verejny metody na posilani paketu - komunikace s ostatnima vrstvama ------------------------------------------------

	/**
	 * Adds incoming packet from cabel to the buffer. Sychronized via buffer. Wakes worker.
	 *
	 * @param packet to receive
	 * @param iface which receives packet
	 */
	public void receivePacket(L2Packet packet, Switchport iface) {
		receiveBuffer.add(new BufferItem(packet, iface));
		worker.wake();
	}

	/**
	 * Adds incoming packet from network module to the buffer and then try to send it via cabel. Sychronized via buffer.
	 * Wakes worker.
	 *
	 * @param packet to send via physical module
	 * @param iface through it will be send
	 */
	public void sendPacket(L2Packet packet, int switchportNumber) {
		Switchport swport = switchports.get(switchportNumber);
		if(swport == null){
			Logger.log(this, Logger.ERROR, LoggingCategory.PHYSICAL,"K odeslani bylo zadano cislo switchportu, ktery neexistuje, prosuvih!", packet);
		}
		sendBuffer.add(new BufferItem(packet, swport));
		worker.wake();
	}


// Samotna prace a ruzny dulezity gettry rozhrani k ostatnim vrstvam (fasada): -----------------------------------------

	@Override
	public void doMyWork() {

		while (!receiveBuffer.isEmpty() || !sendBuffer.isEmpty()) {
			if (!receiveBuffer.isEmpty()) {
				BufferItem m = receiveBuffer.remove(0);
				getNetMod().receivePacket(m.packet, m.switchport.number);
			}

			if (!sendBuffer.isEmpty()) {
				BufferItem m = sendBuffer.remove(0);
				m.switchport.sendPacket(m.packet);
			}
		}
	}


	/**
	 * Returns numbers of switchports.
	 * Uses Network Module to explore network hardware afer start.
	 * @return
	 */
	public List<Integer> getNumbersOfPorts(){
		List<Integer> vratit = new LinkedList<>();
		for(Switchport swport: switchports.values()){
			vratit.add(swport.number);
		}
		return vratit;
	}

	/**
	 * Pro prikaz rnetconn, kterej se switchportama manipuluje. Nepouzivat v sitovym modulu.
	 * @return
	 */
	public Map<Integer, Switchport> getSwitchports() {
		return switchports;
	}

	public boolean isSwitchportConnected (int switchportNumber){
		Switchport swport = switchports.get(switchportNumber);
		if(swport==null) return false;
		else return swport.isConnected();
	}



// Pomocny metody a zkratky: -------------------------------------------------------------------------------------------

	@Override
	public String getDescription() {
		return device.getName()+": PhysicMod";
	}

	private NetMod getNetMod(){
		return device.getNetworkModule();
	}


	private class BufferItem {

		L2Packet packet;
		Switchport switchport;

		public BufferItem(L2Packet packet, Switchport switchport) {
			this.packet = packet;
			this.switchport = switchport;
		}
	}

}
