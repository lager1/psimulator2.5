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
public class L2GenericLayer extends L2layer {

    public L2GenericLayer(NetworkModule networkModule) {
        super(networkModule);
    }

    @Override
    protected void doMyWork() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void receivePacket(L2Packet packet, AbstractInterface iface) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void vyridPacket(L3Packet packet, AbstractInterface iface, IpAdress nextHop) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
