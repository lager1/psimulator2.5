/*
 * created 1.2.2012
 */

package dataStructures.packets.L3.STP;

import dataStructures.BridgeIdentifier;
import shared.SimulatorEvents.SerializedComponents.PacketType;

import java.util.Random;

/**
 * Represents STP Configuration BPDU
 *
 * @author Peter Bábics <babicpe1@fit.cvut.cz>
 */
public class ConfigurationBPDU extends BaseBPDU {

    public final byte flags;
    public final BridgeIdentifier rootIdentifier;
    public final int rootPathCost;
    public final BridgeIdentifier bridgeIdentifier;
    public final short portIdentifier;
    public final short messageAge;
    public final short maxAge;
    public final short helloTime;
    public final short forwardDelay;

    public final Long rand;

    public ConfigurationBPDU(short version, byte type, byte flags, BridgeIdentifier rootIdentifier, int rootPathCost, BridgeIdentifier bridgeIdentifier, short portIdentifier, short messageAge, short maxAge, short helloTime, short forwardDelay) {
        super(version, type);
        this.flags = flags;
        this.rootIdentifier = rootIdentifier;
        this.rootPathCost = rootPathCost;
        this.bridgeIdentifier = bridgeIdentifier;
        this.portIdentifier = portIdentifier;
        this.messageAge = messageAge;
        this.maxAge = maxAge;
        this.helloTime = helloTime;
        this.forwardDelay = forwardDelay;

        this.rand = (new Random()).nextLong();

        countSize();
    }

    @Override
    public L3PacketType getType() {
        return L3PacketType.STP;
    }

    @Override
    public String toString() {
        return "STP: Configuration BPDU: " +
                " Version: " + this.version + "\n" +
                " Flags: " + this.flags + "\n" +
                " Root Identifier: " + this.rootIdentifier + "\n" +
                " Root Path Cost: " + this.rootPathCost + "\n" +
                " Bridge Identifier: " + this.bridgeIdentifier + "\n" +
                " Port Identifier: " + this.portIdentifier + "\n" +
                " Message Age: " + this.messageAge + "\n" +
                " Max Age: " + this.maxAge + "\n" +
                " Hello Time: " + this.helloTime + "\n" +
                " Forward Delay: " + this.forwardDelay;

    }

    @Override
    protected final void countSize() {
        this.size = 35;
    }

    @Override
    public String getEventDesc() {
        String s = "=== STP === \n" +
                " Version: " + this.version + "\n" +
                " Type: Configuration BPDU" + "\n" +
                " Flags: " + this.flags + "\n" +
                " Root Identifier: " + this.rootIdentifier + "\n" +
                " Root Path Cost: " + this.rootPathCost + "\n" +
                " Bridge Identifier: " + this.bridgeIdentifier +  "\n" +
                " Port Identifier: " + this.portIdentifier +  "\n" +
                " Message Age: " + this.messageAge +  "\n" +
                " Max Age: " + this.maxAge +  "\n" +
                " Hello Time: " + this.helloTime +  "\n" +
                " Forward Delay: " + this.forwardDelay;
        return s;
    }

    @Override
    public PacketType getPacketEventType() {
        return PacketType.STP;
    }
}
