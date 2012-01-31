/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import dataStructures.L2Packet;
import device.AbstractDevice;
import networkModule.L2.L2layer;
import physicalModule.Switchport;

//TODO: napsat javadoc
/**
 * Nejabstraknejsi ze sitovejch modulu.
 * Interface representující síťový modul počítače bez rozhraní pro aplikační vrstvu, prakticky
 * tedy použitelnej jen pro switch.
 * Síťový modul zajišťuje síťovou komunikaci na 2.,3. a 4. vrstvě ISO/OSI modelu.
 * @author neiss
 */
public abstract class NetMod {

    protected AbstractDevice device;
    protected L2layer linkLayer;

    public NetMod(AbstractDevice device) {
        this.device = device;
    }

    public AbstractDevice getDevice() {
        return device;
    }

	/**
	 * Implementovat synchronizovane!
	 * @param packet
	 * @param iface
	 */
    public abstract void receivePacket(L2Packet packet, Switchport iface);
}
