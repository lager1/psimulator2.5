/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import dataStructures.L2Packet;
import device.AbstractDevice;
import physicalModule.AbstractInterface;

/**
 *
 * @author neiss
 */
public abstract class SwitchNetworkModule extends NetworkModule{

    public SwitchNetworkModule(AbstractDevice device) {
        super(device);
    }



    public abstract void acceptPacket(L2Packet packet, AbstractInterface iface);

}
