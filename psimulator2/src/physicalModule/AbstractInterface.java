/*
 * Erstellt am 26.10.2011.
 */
package physicalModule;

import dataStructures.L2Packet;



/**
 * Represents physical network interface.
 * Sends and receives packet through cable.
 *
 * It is not running in its own thread, thread of PhysicMod handles it.
 *
 * @author neiss
 */
public abstract class AbstractInterface {

	protected String name;
	protected PhysicMod physicMod;
	/**
	 * Link to cable's connector.
	 * Until cable is not connected ti is null.
	 */
	protected Connector connector;

	public AbstractInterface(String name, Connector connector, PhysicMod physicMod) {
		this.name = name;
		this.connector = connector;
		this.physicMod = physicMod;
	}

	public AbstractInterface(String name, PhysicMod physicMod) {
		this.name = name;
		this.physicMod = physicMod;
	}

	public String getName() {
		return name;
	}

	/**
	 * Try to send packet through this interface.
	 * It just adds packet to buffer (if capacity allows) and notifies connected cable that it has work to do.
	 */
	public abstract void sendPacket(L2Packet packet);

	/**
	 * Receives packet from cable and pass it to physical module.
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
