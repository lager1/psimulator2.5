/*
 * Erstellt am 26.10.2011.
 */

package networkmodule;

import networkDataStructures.L2Packet;
import physicalModule.AbstractNetworkInterface;

/**
 * 
 * @author neiss
 */
public interface INetworkModule {

    public void acceptPacket(L2Packet packet, AbstractNetworkInterface iface );

}
