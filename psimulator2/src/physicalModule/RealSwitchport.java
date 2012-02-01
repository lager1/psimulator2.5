/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physicalModule;

import dataStructures.L2Packet;

/**
 * TODO implementovat
 * @author neiss
 */
public class RealSwitchport extends Switchport{
	
	protected RealSwitchport(int id, PhysicMod physicMod) {
		super(id,physicMod);
	}

	@Override
	protected void sendPacket(L2Packet packet) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void receivePacket(L2Packet packet) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isEmptyBuffer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public L2Packet popPacket() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	
	
}
