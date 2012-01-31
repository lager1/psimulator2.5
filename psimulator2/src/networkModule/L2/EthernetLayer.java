/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.ipAddresses.IpAddress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import dataStructures.MacAddress;
import networkModule.NetMod;
import physicalModule.Switchport;

/**
 * Tady bude veskera implementace ethernetu a to jak pro switch, tak i router.
 * @author neiss
 */
public class EthernetLayer extends L2layer {

    public EthernetLayer(NetMod networkModule) {
        super(networkModule);
    }

    public void doMyWork() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void receivePacket(L2Packet packet, Switchport swport) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendPacket(L3Packet packet, L2Interface iface, MacAddress target) {
		assert iface.getClass()==EthernetInterface.class;
		EthernetInterface etherIface=(EthernetInterface)iface;
		Switchport port = etherIface.getSwitchport(target);
		
	}



}
