package networkModule.Timers.STP;


import dataStructures.MacAddress;
import dataStructures.packets.L2.EthernetPacket;
import dataStructures.packets.L2Packet;
import dataStructures.packets.L3.STP.ConfigurationBPDU;
import dataStructures.packets.L3Packet;
import networkModule.L2.Stp.StpState;
import networkModule.L2.Stp.StpSwitchportState;
import networkModule.STP;
import networkModule.SwitchNetworkModule;
import shared.Timer.ITimerCallable;
import shared.Timer.Timer;
import shared.Timer.TimerEntry;

/**
 * Class representing Hold  timer
 *
 * Implementation based on chapter 8.7.8 Hold Timer expiry of IEEE 802.1d - 1998
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPHoldTimer implements ITimerCallable {

    private StpSwitchportState state = null;
    private StpState bridgeState = null;
    private SwitchNetworkModule module = null;

    public STPHoldTimer(StpSwitchportState state, StpState bridgeState, SwitchNetworkModule module) {
        this.state = state;
        this.bridgeState = bridgeState;
        this.module = module;
    }

    @Override
    public void timerExpired(TimerEntry entry) {
        System.out.println("[" + module.getDevice().getName() + "][Port #" + state.portIdentifier + "] " + "STP Hold Timer expired");
        if (state.configurationPending)
            STP.transmitConfigurationBPDU(bridgeState, state);
        Timer.deactivateTimer(entry.getEntryName());
    }
}
