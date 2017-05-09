package networkModule.filters.Device;

import device.Device;
import networkModule.L2.Stp.StpPortStates;
import networkModule.L2.Stp.StpState;
import networkModule.L2.Stp.StpSwitchportState;
import networkModule.STP;
import networkModule.filters.DeviceFilter;
import networkModule.filters.GlobalFilter;

/**
 * Class used for disabling unconnected ports
 *
 * Hook is called right after all cables were connected
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class STPDeviceFilter extends DeviceFilter
{
    protected final StpState state;

    public STPDeviceFilter(StpState state)
    {
        this.state = state;
    }

    @Override
    public Boolean afterLoad(Device device)
    {
        for (StpSwitchportState portState : state.portsStates.values())
        {
            if (!device.physicalModule.isSwitchportConnected(portState.portIdentifier))
            {
                System.out.println("[" + device.getName() + "][Port #" + portState.portIdentifier + "] Disabling port");
                portState.state = StpPortStates.PORT_DISABLED;
            }
        }
        STP.portStateSelection(state);
        return super.afterLoad(device);
    }
}
