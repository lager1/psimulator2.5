/*
 * created 25.1.2012
 */

package dataStructures;

/**
 * Represents Ethernet II frames.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class EthernetPacket extends L2Packet {

	MacAddress src;
	MacAddress dst;
	Ethertype ethertype;

	@Override
	public int getSize() {
		int sum = 0;
		// TODO: pridat velikost tohoto paketu
		sum += 1000; // max je 1500 B
		return sum + (data != null ? data.getSize() : 0);
	}
}
