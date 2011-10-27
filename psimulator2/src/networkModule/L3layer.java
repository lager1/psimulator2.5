/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import java.util.List;
import networkDataStructures.L2Packet;
import physicalModule.AbstractNetworkInterface;

/**
 *
 * @author neiss
 */
public abstract class L3layer extends Layer {

    public L3layer(NetworkModule networkModule) {
        super(networkModule);
    }

    /**
     * Prijima pakety od linkovy vrstvy.
     */
    public abstract void receivePacket();




}
