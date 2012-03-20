/*
 * created 20.3.2012
 */
package dataStructures;

import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
interface EventDescriptive {

	public String getEventDesc();
	public PacketType getPacketEventType();
}
