/*
 * Erstellt am 27.10.2011.
 */
package networkModule;

import dataStructures.ipAdresses.IpAdress;
import dataStructures.L2Packet;
import dataStructures.L3Packet;
import physicalModule.AbstractInterface;

/**
 *
 * @author neiss
 */
public class SimpleL2Layer extends L2layer {

    public SimpleL2Layer(NetMod networkModule) {
        super(networkModule);
    }

    public void doMyWork() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void receivePacket(L2Packet packet, AbstractInterface iface) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void acceptPacket(L3Packet packet, AbstractInterface iface, IpAdress nextHop) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
