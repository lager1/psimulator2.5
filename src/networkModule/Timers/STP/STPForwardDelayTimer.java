package networkModule.Timers.STP;

import networkModule.L2.Stp.StpPortStates;
import networkModule.L2.Stp.StpSwitchportState;
import networkModule.STP;
import shared.Timer.ITimerCallable;
import shared.Timer.Timer;
import shared.Timer.TimerEntry;

/**
 * Class representing Forward delay timer
 *
 * Implementation based on chapter 8.7.5 Forward Delay Timer expiry of IEEE 802.1d - 1998
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPForwardDelayTimer implements ITimerCallable {
    private StpSwitchportState state = null;

    public STPForwardDelayTimer(StpSwitchportState state) {
        this.state = state;
    }

    @Override
    public void timerExpired(TimerEntry entry) {
        System.out.println("["  + state.bridgeState.device.getName() + "][Port #" + state.portIdentifier + "] Forward delay timer expired  State: " + state.state);

        if (state.state == StpPortStates.PORT_LISTENING)
            state.state = StpPortStates.PORT_LEARNING;

        if (state.state == StpPortStates.PORT_LEARNING) {
            state.state = StpPortStates.PORT_FORWARDING;

            Boolean callTopologyChange = false;
            for (StpSwitchportState portState : state.bridgeState.portsStates.values()) {
                if (portState.designatedBridge == state.bridgeState.bridgeIdentifier &&
                        portState.designatedPort == portState.portIdentifier &&
                        portState.changeDetectionEnabled == true) {
                    callTopologyChange = true;
                    break;
                }
            }
            if (callTopologyChange)
                STP.topologyChangeDetection(state.bridgeState);
        }
    }
}
