/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import dataStructures.L2Packet;
import device.Device;
import physicalModule.PhysicMod;


//TODO: napsat javadoc anglicky.
/**
 * Nejabstraknejsi ze sitovejch modulu. Interface representující síťový modul počítače bez rozhraní pro aplikační
 * vrstvu, prakticky tedy použitelnej jen pro switch. Síťový modul zajišťuje síťovou komunikaci na 2.,3. a 4. vrstvě
 * ISO/OSI modelu.
 *
 * @author neiss
 */


public abstract class NetMod {

    protected Device device;

	public NetMod(Device device) {
		assert device != null;
		this.device = device;
	}
	
    public Device getDevice() {
        return device;
    }

	public PhysicMod getPhysicMod() {
		return device.physicalModule;
	}

	/**
	 * Implementovat synchronizovane!
	 * @param packet
	 * @param swport
	 */
    public abstract void receivePacket(L2Packet packet, int  switchportNumber);
	
	/**
	 * Returns true, if device is switch and have only link layer.
	 * @return 
	 */
	public abstract boolean isSwitch();
	
}
