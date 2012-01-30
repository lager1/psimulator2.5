/*
 * Erstellt am 27.10.2011.
 */
package networkModule.L2;

import dataStructures.ipAddresses.IpAddress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import networkModule.NetMod;
import physicalModule.AbstractSwitchport;

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
	public void receivePacket(L2Packet packet, AbstractSwitchport iface) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendPacket(L3Packet packet, AbstractSwitchport iface, IpAddress nextHop) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
