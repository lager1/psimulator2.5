/*
 * Erstellt am 26.10.2011.
 */
package physicalModule;

import dataStructures.L2Packet;



/**
 * Represents physical network interface.
 * Sends and receives packet through cabel.
 *
 * It is not running in its own thread, thread of PhysicMod handles it.
 *
 * @author neiss
 */
public abstract class AbstractInterface {

	protected String name;
	protected Cabel cabel;
	protected PhysicMod physicMod;


	public AbstractInterface(String name, Cabel cabel, PhysicMod physicMod) {
		this.name = name;
		this.cabel = cabel; // tady
		this.physicMod = physicMod;
	}

	public AbstractInterface(String name, PhysicMod physicMod) {
		this.name = name;
		this.physicMod = physicMod;
	}

	public String getName() {
		return name;
	}

	public Cabel getCabel() {
		return cabel;
	}

	public void setCabel(Cabel cabel) { // TODO: predelat, u kabelu se to musi taky nastavit!
		assert cabel != null;
		this.cabel = cabel; // tady
	}

	/**
	 * Try to send packet through this interface.
	 * It just adds packet to buffer (if capacity allows) and notifies connected cabel that it has work to do.
	 */
	public abstract void sendPacket(L2Packet packet);

	/**
	 * Receives packet from cabel and pass it to physical module.
	 */
	public abstract void receivePacket(L2Packet packet);

	/**
	 * Return true if buffer is empty.
	 * Synchronied via buffer.
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
//	 * TODO: porovnavani rozhrani podle tohodlec divnyho UUID, asi nejjednodussi metoda, co me napadla
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
//		if (obj instanceof AbstractInterface) {
//			AbstractInterface iface = (AbstractInterface) obj;
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
