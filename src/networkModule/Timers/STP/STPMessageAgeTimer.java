package networkModule.Timers.STP;

import networkModule.L2.Stp.StpState;
import networkModule.L2.Stp.StpSwitchportState;
import networkModule.STP;
import networkModule.SwitchNetworkModule;
import shared.Timer.ITimerCallable;
import shared.Timer.Timer;
import shared.Timer.TimerEntry;

import java.util.Date;

/**
 * Class representing Message age timer
 *
 * Implementation based on chapter 8.7.5 - Forward Delay Timer expiry of IEEE 802.1d - 1998
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPMessageAgeTimer implements ITimerCallable {
    private StpSwitchportState state = null;
    private SwitchNetworkModule module = null;

    public STPMessageAgeTimer(StpSwitchportState state, SwitchNetworkModule module) {
        this.state = state;
        this.module = module;
    }

    @Override
    public void timerExpired(TimerEntry entry) {
        System.out.println("["  + module.getDevice().getName() + "][Port #" + state.portIdentifier + "] Message age timer expired entry: " + entry + "  @ " + (new Date()).getTime());
        StpState bridgeState = state.bridgeState;
        Boolean wasRoot = bridgeState.designatedRoot == bridgeState.bridgeIdentifier;
        STP.becomeDesignatedPort(state, bridgeState);
        STP.configurationUpdate(bridgeState);
        STP.portStateSelection(bridgeState);
        if (!wasRoot &&
                bridgeState.designatedRoot == bridgeState.bridgeIdentifier)
        {
            synchronized (bridgeState){
                bridgeState.maxAge = new Integer(bridgeState.bridgeMaxAge).shortValue();
                bridgeState.helloTime = new Integer(bridgeState.bridgeHelloTime).shortValue();
                bridgeState.forwardDelay = new Integer(bridgeState.bridgeForwardDelay).shortValue();
                STP.topologyChangeDetection(bridgeState);
                Timer.deactivateTimer(bridgeState.device.getName() + "#StpTopologyChangeNotificationTimer");
                STP.configurationBPDUGeneration(bridgeState);
            }
        }
    }
}
