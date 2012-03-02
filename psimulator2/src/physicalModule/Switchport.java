/*
 * Erstellt am 26.10.2011.
 */
package physicalModule;

import dataStructures.L2Packet;



/**
 * Represents abstract physical switchport.
 * Sends and receives packet through cable.
 *
 * It is not running in its own thread, thread of PhysicMod handles it.
 *
 * @author neiss, haldyr
 */
public abstract class Switchport {

	protected PhysicMod physicMod;
	
	/**
	 * Unique number in PhysicMod.
	 */
	public final int number;
	
	/**
	 * ID takove, jake je v konfiguracnim souboru.
	 */
	public final int configID;


	public Switchport(PhysicMod physicMod, int number, int configID) {
		this.physicMod = physicMod;
		this.number = number;
		this.configID = configID;
	}
	

	/**
	 * Try to send packet through this interface.
	 * It just adds packet to buffer (if capacity allows) and notifies connected cable that it has work to do.
	 */
	protected abstract void sendPacket(L2Packet packet);

	/**
	 * Receives packet from cable and pass it to physical module.
	 */
	protected abstract void receivePacket(L2Packet packet);

	/**
	 * Returns true if buffer is empty.
	 */
	public abstract boolean isEmptyBuffer();

	/**
	 * Remove packet form buffer and return it, decrements size of buffer. Synchronised via buffer. Throws exception when this method
	 * is called and no packet is in buffer.
	 *
	 * @return
	 */
	public abstract L2Packet popPacket();


// ----------------------------- zatim neni treba -----------------------------
//	/**
//	 * For comparison of two interfaces
//	 * porovnavani rozhrani podle tohodlec divnyho UUID, asi nejjednodussi metoda, co me napadla
//	 */
//	protected UUID hash = UUID.randomUUID();
//	/**
//	 * Uniq UUID (something like hash, randomly generated)
//	 * @return
//	 */
//	public UUID getHash() {
//		return hash;
//	}
//	/**
//	 * Compare ifaces by hash
//	 * @param obj
//	 * @return true if both interfaces has the same UUID (= the are the same interfaces on the same netw. device)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof Switchport) {
//			Switchport iface = (Switchport) obj;
//			if (this.getHash().equals(iface.getHash())) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		int mhash = 3;
//		mhash = 71 * mhash + (this.hash != null ? this.hash.hashCode() : 0);
//		return mhash;
//	}
}
