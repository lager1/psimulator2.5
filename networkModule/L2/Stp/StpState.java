package networkModule.L2.Stp;

import dataStructures.BridgeIdentifier;
import device.Device;

import java.util.Map;
import java.util.concurrent.locks.Lock;


/**
 * Class representing Switch stp state
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class StpState {


    public BridgeIdentifier designatedRoot;
    public int rootPathCost;
    public int rootPort;
    public short maxAge;
    public short helloTime;
    public short forwardDelay;
    public final BridgeIdentifier bridgeIdentifier;
    public final int bridgeMaxAge;
    public final int bridgeHelloTime;
    public final int bridgeForwardDelay;
    public boolean topologyChange;
    public boolean topologyChangeDetected;

    public final Device device;

    public StpState(BridgeIdentifier bridgeIdentifier, short bridgeMaxAge, short bridgeHelloTime, short bridgeForwardDelay, Map<Integer, StpSwitchportState> portsStates, Device device) {

        this.rootPathCost = 0;
        this.rootPort = -1;
        this.maxAge = bridgeMaxAge;
        this.helloTime = bridgeHelloTime;
        this.forwardDelay = bridgeForwardDelay;
        this.designatedRoot = bridgeIdentifier;

        this.portsStates = portsStates;
        this.topologyChange = false;
        this.topologyChangeDetected = false;

        this.device = device;

        this.bridgeIdentifier = bridgeIdentifier;
        this.bridgeMaxAge = bridgeMaxAge;
        this.bridgeHelloTime = bridgeHelloTime;
        this.bridgeForwardDelay = bridgeForwardDelay;
    }

    public Map<Integer, StpSwitchportState> portsStates;

}
