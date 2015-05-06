package networkModule.Timers.STP;

import networkModule.L2.Stp.StpState;
import networkModule.STP;
import shared.Timer.ITimerCallable;
import shared.Timer.Timer;
import shared.Timer.TimerEntry;

/**
 * Class representing Topology Change Notification Timer
 *
 * Implementation based on chapter 8.7.6 Topology Change Notification Timer Expiry of IEEE 802.1d - 1998
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPTopologyChangeNotificationTimer implements ITimerCallable {
    private StpState state = null;

    public STPTopologyChangeNotificationTimer(StpState state) {
        this.state = state;
    }


    @Override
    public void timerExpired(TimerEntry entry) {
        System.out.println("[" + state.device.getName() + "] " + "STP Topology Change Timer Notification expired");

        STP.transmitTopologyChangeNotificationBPDU(state);
        Timer.activateTimer(state.device.getName() + "#StpTopologyChangeNotificationTimer");
    }
}
