package networkModule.L2.Stp;

import dataStructures.BridgeIdentifier;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class representing Switch stp state
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class StpSwitchportState {
    public StpSwitchportState(short portIdentifier) {

        this.portIdentifier = portIdentifier;
        configurationPending = false;
        topologyChangeAck = false;
        changeDetectionEnabled = true;
        state = StpPortStates.PORT_BLOCKING;
        pathCost = 1;
    }

    public StpState bridgeState;
    public final short portIdentifier;
    public StpPortStates state;
    public int pathCost;
    public BridgeIdentifier designatedRoot;
    public int designatedCost;
    public BridgeIdentifier designatedBridge;
    public short designatedPort;

    public Boolean topologyChangeAck;
    public Boolean configurationPending;
    public Boolean changeDetectionEnabled;

    public Boolean hold = false;

    public String getTimerPrefix() {
        return bridgeState.device.getName() + "#Port_" + portIdentifier;
    }
}