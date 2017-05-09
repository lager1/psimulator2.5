/*
 * created 1.2.2012
 */

package dataStructures.packets.L3.STP;

import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Represents STP Topology change notification BPDU
 *
 * @author Peter Bábics <babicpe1@fit.cvut.cz>
 */
public class TopologyChangeNotificationBPDU extends BaseBPDU {

    public final short version;
    public final byte type;


    public TopologyChangeNotificationBPDU(short version, byte type) {
        super(version, type);
        this.version = version;
        this.type = type;
        countSize();
    }

    @Override
    public L3PacketType getType() {
        return L3PacketType.STP;
    }

    @Override
    public String toString() {
        return "STP: Topology Change Notification BPDU: " +
                " Version: " + this.version;

    }

    @Override
    protected final void countSize() {
        this.size = 4;
    }

    @Override
    public String getEventDesc() {
        String s = "=== STP === \n" +
                " Version: " + this.version +  "\n" +
                " Type: Topology Change Notification BPDU";
        return s;
    }

    @Override
    public PacketType getPacketEventType() {
        return PacketType.STP;
    }
}
