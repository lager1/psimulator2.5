package networkModule.Timers.STP;

import networkModule.L2.Stp.StpState;
import networkModule.L2.Stp.StpSwitchportState;
import shared.Timer.ITimerCallable;
import shared.Timer.TimerEntry;

/**
 * Timer Callback useful for debugging STP Protocol
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPDebugOutput implements ITimerCallable
{
    private StpState state;

    public STPDebugOutput(StpState state)
    {
        this.state = state;
    }

    @Override
    public void timerExpired(TimerEntry entry)
    {
        String prefix = "[" + state.device.getName() + "]" ;
        System.out.println(prefix + " is " + (state.designatedRoot == state.bridgeIdentifier ? "Root Bridge" : "Designated Bridge"));
        System.out.println("\t" + prefix + " Root Path Cost: " + state.rootPathCost);
        System.out.println("\t" + prefix + " Root Port: " + state.rootPort);
        System.out.println("\t" + prefix + " Root bridge identifier: " + state.designatedRoot);
        System.out.println("\t" + prefix + " Bridge identifier: " + state.bridgeIdentifier);

        for (StpSwitchportState portState : state.portsStates.values())
        {
            String portType = state.rootPort == portState.portIdentifier ? "Root Port" : ((portState.designatedPort == portState.portIdentifier && portState.designatedBridge == portState.bridgeState.bridgeIdentifier ? "Designed Port" : "Blocked Port"));
            System.out.println("\t[Port #" + portState.portIdentifier + "] Is: " + portType + " State: " + portState.state + " Designated Cost: " + portState.designatedCost + " Root Bridge: " + portState.designatedRoot + " Designated bridge: " + portState.designatedBridge);
        }
    }
}
