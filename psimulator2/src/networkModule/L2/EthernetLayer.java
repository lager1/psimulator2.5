/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.EthernetPacket;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import dataStructures.MacAddress;
import java.awt.image.RescaleOp;
import java.util.*;
import networkModule.NetMod;
import physicalModule.Switchport;

/**
 * Tady bude veskera implementace ethernetu a to jak pro switch, tak i router.
 * @author neiss
 */
public class EthernetLayer extends L2layer {
	
	
	private class SendItem {
		L3Packet packet; L2Interface iface; MacAddress target;
		public SendItem(L3Packet packet, L2Interface iface, MacAddress target) {
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
	
	private List<SendItem> sendBuffer = Collections.synchronizedList(new LinkedList<SendItem>());
	private List<ReceiveItem> receiveBuffer = Collections.synchronizedList(new LinkedList<ReceiveItem>());
	
	
	
	/**
	 * Konstruktor.
	 * @param networkModule 
	 */
    public EthernetLayer(NetMod networkModule) {
        super(networkModule);
    }
	
	private void handleSendPacket(L3Packet packet, L2Interface iface, MacAddress target){
		assert iface.getClass()==EthernetInterface.class; //je to ethernetova vrstva takze chci ethernetovy rozhrani
		EthernetInterface etherIface=(EthernetInterface)iface;
		
		Switchport port = etherIface.getSwitchport(target);
		EthernetPacket p = new EthernetPacket(etherIface.getMac(),target,packet.getType(),packet);
		netMod.getDevice().getPhysicalModule().sendPacket(null, port);
	}
	
	private void handleReceivePacket(L2Packet packet, Switchport swport){
		
	}
	
	
	
	
	
	
    public void doMyWork() {
        while ( ! (sendBuffer.isEmpty() && receiveBuffer.isEmpty() ) ) {
			if( ! sendBuffer.isEmpty()){
				SendItem it = sendBuffer.remove(0);
				handleSendPacket(it.packet, it.iface, it.target);
			}
			if( ! receiveBuffer.isEmpty()){
				ReceiveItem it = receiveBuffer.remove(0);
				
			}
		}
    }

	@Override
	public void receivePacket(L2Packet packet, Switchport swport) {
		receiveBuffer.add(new ReceiveItem(packet, swport));
		worker.wake();
	}

	@Override
	public void sendPacket(L3Packet packet, L2Interface iface, MacAddress target) {
		sendBuffer.add(new SendItem(packet, iface, target));
		worker.wake();
	}



}
