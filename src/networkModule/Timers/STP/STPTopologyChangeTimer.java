package networkModule.Timers.STP;

import networkModule.L2.Stp.StpState;
import networkModule.STP;
import shared.Timer.ITimerCallable;
import shared.Timer.Timer;
import shared.Timer.TimerEntry;

/**
 * Class representing Topology Change Timer
 *
 * Implementation based on chapter 8.7.7 Topology Change Timer Expiry of IEEE 802.1d - 1998
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPTopologyChangeTimer implements ITimerCallable {
    private StpState state = null;

    public STPTopologyChangeTimer(StpState state) {
        this.state = state;
    }


    @Override
    public void timerExpired(TimerEntry entry) {
        System.out.println("[" + state.device.getName() + "] " + "STP Topology Change Timer expired");

        state.topologyChangeDetected = false;
        state.topologyChange = false;
    }
}
