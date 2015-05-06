package networkModule.filters;

import dataStructures.packets.L2.EthernetPacket;
import device.Device;
import networkModule.L2.EthernetInterface;
import physicalModule.Switchport;


/**
 * Abstract Class for L1 filter
 *
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class AbstractFilter implements IFilter {

    public Boolean filterInbound(EthernetPacket packet, EthernetInterface intrfc, Switchport port, int switchportNumber, Device device) {
        return true;
    }

    public Boolean filterOutbound(EthernetPacket packet, EthernetInterface intrfc, Switchport port, int switchportNumber, Device device) {
        return true;
    }
}
