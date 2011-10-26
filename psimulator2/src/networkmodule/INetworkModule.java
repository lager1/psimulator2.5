/*
 * Erstellt am 26.10.2011.
 */

package networkmodule;

import networkDataStructures.L2Packet;
import physicalModule.AbstractNetworkInterface;

//TODO: napsat javadoc
/**
 * Interface representující síťový modul počítače bez rozhraní pro aplikační vrstvu, prakticky
 * tedy použitelnej jen pro switch.
 * Síťový modul zajišťuje síťovou komunikaci na 2.,3. a 4. vrstvě ISO/OSI modelu.
 * @author neiss
 */
public interface INetworkModule {

    public void acceptPacket(L2Packet packet, AbstractNetworkInterface iface );

}
