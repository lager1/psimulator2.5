/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.EthernetPacket;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import dataStructures.MacAddress;
import java.util.*;
import networkModule.Layer;
import networkModule.NetMod;
import physicalModule.Switchport;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Tady bude veskera implementace ethernetu a to jak pro switch, tak i router.
 * TODO: vubec neni hotovy
 * @author neiss
 */
public class EthernetLayer extends Layer implements SmartRunnable{
	
	
	private class SendItem {
		L3Packet packet; EthernetInterface iface; MacAddress target;
		public SendItem(L3Packet packet, EthernetInterface iface, MacAddress target) {
			this.packet = packet;
			this.iface = iface;
			this.target = target;
		}
	}
	
	private class ReceiveItem {
		L2Packet packet; Switchport swport;
		public ReceiveItem(L2Packet packet, Switchport swport) {
			this.packet = packet;
			this.swport = swport;
		}
	}
	
	protected WorkerThread worker = new WorkerThread(this);
	private List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	private List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());
	//private 
	
	
	
	/**
	 * Konstruktor.
	 * @param networkModule 
	 */
    public EthernetLayer(NetMod networkModule) {
        super(networkModule);
    }
	
	private void handleSendPacket(L3Packet packet, EthernetInterface iface, MacAddress target){
		
		Switchport port = iface.getSwitchport(target);
		EthernetPacket p = new EthernetPacket(iface.getMac(),target,packet.getType(),packet);
		netMod.getDevice().getPhysicalModule().sendPacket(null, port);
	}
	
	private void handleReceivePacket(L2Packet packet, Switchport swport){
		//EthernetInterface iface = 
		
	}
	
	
	
	
	
	
    public void doMyWork() {
        while ( ! (sendBuffer.isEmpty() && receiveBuffer.isEmpty() ) ) {
			if( ! sendBuffer.isEmpty()){
				SendItem it = sendBuffer.remove(0);
				handleSendPacket(it.packet, it.iface, it.target);
			}
			if( ! receiveBuffer.isEmpty()){
				ReceiveItem it = receiveBuffer.remove(0);
				handleReceivePacket(it.packet, it.swport);
			}
		}
    }

	public void receivePacket(L2Packet packet, Switchport swport) {
		receiveBuffer.add(new ReceiveItem(packet, swport));
		worker.wake();
	}

	public void sendPacket(L3Packet packet, EthernetInterface iface, MacAddress target) {
		sendBuffer.add(new SendItem(packet, iface, target));
		worker.wake();
	}



}
