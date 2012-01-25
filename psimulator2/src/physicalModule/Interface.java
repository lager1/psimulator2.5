/*
 * created 24.1.2012
 */

package physicalModule;

import dataStructures.L2Packet;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Interface extends AbstractInterface {

	public Interface(String name, Cabel cabel) {
		super(name, cabel);
	}

	public Interface(String name) {
		super(name);
	}

	@Override
	public boolean sendPacket(L2Packet packet) {
		return cabel.transportPacket(packet, this);
	}


}
