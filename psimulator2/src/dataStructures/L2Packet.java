/*
 * Erstellt am 26.10.2011.
 */

package dataStructures;

/**
 *
 * @author neiss
 */
public abstract class L2Packet {

    L3Packet data;

	public int getSize() {
		int sum = 0;
		// TODO: pridat velikost tohoto paketu

		return sum + (data != null ? data.getSize() : 0);
	}

}
