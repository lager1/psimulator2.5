/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import networkDataStructures.IPAdress;
import networkDataStructures.L2Packet;
import networkDataStructures.L3Packet;
import physicalModule.AbstractNetworkInterface;

/**
 *
 * @author neiss
 */
public abstract class L2layer extends Layer {

    public L2layer(NetworkModule networkModule) {
        super(networkModule);
    }

    /**
     * Prijima pakety od nizsi vrstvy.
     */
    public abstract void receivePacket(L2Packet packet, AbstractNetworkInterface iface);

    /**
     * Prijima pakety od vyssi vrstvy.
     */
    public abstract void vyridPacket(L3Packet packet, AbstractNetworkInterface iface,IPAdress nextHop);
    




}
