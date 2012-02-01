/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import dataStructures.L2Packet;
import device.AbstractDevice;
import physicalModule.PhysicMod;
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
	protected PhysicMod physicMod;

	public NetMod(AbstractDevice device, PhysicMod physicMod) {
		assert device != null;
		assert physicMod != null;
		this.device = device;
		this.physicMod = physicMod;
	}
	
    public AbstractDevice getDevice() {
        return device;
    }

	public PhysicMod getPhysicMod() {
		return physicMod;
	}

	/**
	 * Implementovat synchronizovane!
	 * @param packet
	 * @param swport
	 */
    public abstract void receivePacket(L2Packet packet, int  switchportNumber);
}
