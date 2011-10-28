/*
 * Erstellt am 27.10.2011.
 */
package networkModule;

import networkDataStructures.IpAdress;
import networkDataStructures.L2Packet;
import networkDataStructures.L3Packet;
import physicalModule.AbstractNetworkInterface;

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
	public void receivePacket(L2Packet packet, AbstractNetworkInterface iface) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void vyridPacket(L3Packet packet, AbstractNetworkInterface iface, IpAdress nextHop) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
