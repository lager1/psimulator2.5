/*
 * Erstellt am 26.10.2011.
 */
package physicalModule;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import dataStructures.L2Packet;


/**
 *
 * @author neiss
 */
public abstract class AbstractInterface {

	private String name;
	private Cabel cabel;
	/**
	 * For comparison of two interfaces
	 * TODO: porovnavani rozhrani podle tohodlec divnyho UUID, asi nejjednodussi metoda, co me napadla
	 */
	private UUID hash = UUID.randomUUID();

	public AbstractInterface(String name, Cabel cabel) {
		this.name = name;
		this.cabel = cabel;
	}

	public AbstractInterface(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Cabel getCabel() {
		return cabel;
	}

	/**
	 * Uniq UUID (something like hash, randomly generated)
	 * @return
	 */
	public UUID getHash() {
		return hash;
	}

	public void plugInCable(Cabel cabel) { // TODO: predelat, u kabelu se to musi taky nastavit!
		this.cabel = cabel;
	}

	/**
	 * Try to send packet thgroug this interfaces
	 * @return true if packet was delivered to the interface at the end of cable
	 */
	public boolean sendPacket(L2Packet packet) {
		return cabel.transportPacket(packet, this);
	}

	/**
	 * Compare ifaces by hash
	 * @param obj
	 * @return true if both interfaces has the same UUID (= the are the same interfaces on the same netw. device)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractInterface) {
			AbstractInterface iface = (AbstractInterface) obj;
			if (this.getHash().equals(iface.getHash())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int mhash = 3;
		mhash = 71 * mhash + (this.hash != null ? this.hash.hashCode() : 0);
		return mhash;
	}
}
