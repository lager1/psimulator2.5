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
public abstract class L2layer extends Layer {

    public L2layer(NetworkModule networkModule) {
        super(networkModule);
    }

    /**
     * Prijima pakety od nizsi vrstvy.
     */
    public abstract void receivePacket(L2Packet packet, AbstractInterface iface);

    /**
     * Prijima pakety od vyssi vrstvy.
     */
    public abstract void vyridPacket(L3Packet packet, AbstractInterface iface,IpAdress nextHop);





}
