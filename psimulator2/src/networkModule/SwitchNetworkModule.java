/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import networkDataStructures.L2Packet;
import networkDevice.AbstractNetworkDevice;
import physicalModule.AbstractNetworkInterface;

/**
 *
 * @author neiss
 */
public abstract class SwitchNetworkModule extends NetworkModule{

    public SwitchNetworkModule(AbstractNetworkDevice device) {
        super(device);
    }

    

    public abstract void acceptPacket(L2Packet packet, AbstractNetworkInterface iface);

}
