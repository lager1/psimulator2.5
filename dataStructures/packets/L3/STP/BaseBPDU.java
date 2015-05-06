/*
 * created 1.2.2012
 */

package dataStructures.packets.L3.STP;

import dataStructures.packets.L3Packet;
import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * Represents abstract STP packet.
 *
 * @author Peter Bábics <babicpe1@fit.cvut.cz>
 */
public class BaseBPDU extends L3Packet {

    public final short version;
    public final byte type;


    public BaseBPDU(short version, byte type) {
        super(null);
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
        return "STP: Abstract BPDU: " +
                " Version: " + this.version;

    }

    @Override
    protected void countSize() {
        this.size = 4;
    }

    @Override
    public String getEventDesc() {
        String s = "=== STP === \n" +
                " Version: " + this.version +
                " Type: Abstract BPDU";
        return s;
    }

    @Override
    public PacketType getPacketEventType() {
        return PacketType.STP;
    }
}
