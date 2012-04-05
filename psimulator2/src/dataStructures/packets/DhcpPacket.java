/*
 * Erstellt am 4.4.2012.
 */

package dataStructures.packets;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Implementace dhcp paketu, jsou tu jen veci, ktery jsou v simulatrou opravdu potreba.
 * @author Tomas Pitrinec
 */
public class DhcpPacket implements PacketData {

	DhcpType type;
	int transaction_id;
	IPwithNetmask assignedClientAddress;
	IpAddress broadcast;




	@Override
	public int getSize() {
		return 300; // vyzkoumano wiresharkem
	}

	@Override
	public String getEventDesc() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PacketType getPacketEventType() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public enum DhcpType{
		// client:
		DISCOVER,
		REQUEST,

		//server:
		OFFER,
		ACK,
		NAK,

		// ostatni zatim nedelam
	}

}
