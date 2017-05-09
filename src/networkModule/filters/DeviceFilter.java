package networkModule.filters;

import device.Device;

/**
 * Abstract class from Device Filter
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class DeviceFilter implements IFilter
{
    public Boolean afterLoad(Device device) {
        return true;
    }
}
