/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import networkDataStructures.L2Packet;
import networkDevice.AbstractNetworkDevice;
import physicalModule.AbstractNetworkInterface;

//TODO: napsat javadoc
/**
 * Interface representující síťový modul počítače bez rozhraní pro aplikační vrstvu, prakticky
 * tedy použitelnej jen pro switch.
 * Síťový modul zajišťuje síťovou komunikaci na 2.,3. a 4. vrstvě ISO/OSI modelu.
 * @author neiss
 */
public abstract class NetworkModule {

    protected AbstractNetworkDevice device;

    public NetworkModule(AbstractNetworkDevice device) {
        this.device = device;
    }

    public AbstractNetworkDevice getDevice() {
        return device;
    }
	
	/**
	 * Implementovat synchronizovane!
	 * @param packet
	 * @param iface 
	 */
    public abstract void acceptPacket(L2Packet packet, AbstractNetworkInterface iface );

}
