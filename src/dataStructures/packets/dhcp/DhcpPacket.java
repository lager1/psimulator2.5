/*
 * Erstellt am 4.4.2012.
 */

package dataStructures.packets.dhcp;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.PacketData;
import java.util.HashMap;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Implementace dhcp paketu, jsou tu jen veci, ktery jsou v simulatrou opravdu potreba.
 * @author Tomas Pitrinec
 * @author Michal Horacek
 */
public class DhcpPacket implements PacketData {

    public final DhcpPacketType type;
    public final int transaction_id;
    public final IpAddress serverIdentifier;

    public final IPwithNetmask ipToAssign;
    public final IpAddress broadcast;
    public final MacAddress clientMac;

    public final HashMap<String, String> options;

    public DhcpPacket(DhcpPacketType type, int transaction_id, IpAddress serverIdentifier, IPwithNetmask ipToAssign,
            IpAddress broadcast, MacAddress clientMac, HashMap<String, String> options) {
        this.type = type;
        this.transaction_id = transaction_id;
        this.serverIdentifier = serverIdentifier;
        this.ipToAssign = ipToAssign;
        this.broadcast = broadcast;
        this.clientMac = clientMac;
        this.options = options;
    }

    @Override
    public int getSize() {
        return 300; // vyzkoumano wiresharkem
    }

    @Override
    public String getEventDesc() {
        String s = "=== DHCP === \n";
        s += "type: "+type+"  ";
        s += "serverID: "+serverIdentifier+"  ";
        s += "YIP: "+ipToAssign+"  ";
        s += "clientMac: "+clientMac+"  ";
        return s;
    }

    @Override
    public PacketType getPacketEventType() {
        return PacketType.DHCP;
    }

}
