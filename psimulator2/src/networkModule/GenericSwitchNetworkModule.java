/*
 * Erstellt am 27.10.2011.
 * TODO: implementovat
 */

package networkModule;

import networkDataStructures.L2Packet;
import networkDevice.AbstractNetworkDevice;
import physicalModule.AbstractNetworkInterface;

/**
 * Implementation of network module of generic simple switch.
 * @author neiss
 */
public class GenericSwitchNetworkModule extends SwitchNetworkModule  {

    public GenericSwitchNetworkModule(AbstractNetworkDevice device) {
        super(device);
    }

    @Override
    public void acceptPacket(L2Packet packet, AbstractNetworkInterface iface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
