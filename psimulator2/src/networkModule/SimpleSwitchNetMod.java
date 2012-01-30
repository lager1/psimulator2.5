/*
 * Erstellt am 27.10.2011.
 * TODO: implementovat
 */

package networkModule;

import dataStructures.L2Packet;
import device.AbstractDevice;
import networkModule.L2.EthernetLayer;
import physicalModule.AbstractSwitchport;

/**
 * Implementation of network module of generic simple switch.
 * @author neiss
 */
public class SimpleSwitchNetMod extends NetMod  {

    public SimpleSwitchNetMod(AbstractDevice device) {
        super(device);
        linkLayer = new EthernetLayer(this);
    }

    /**
     * Prijimani od fysickyho modulu.
     * @param packet
     * @param iface
     */
    @Override
    public void receivePacket(L2Packet packet, AbstractSwitchport iface) {
        linkLayer.receivePacket(packet, iface);
    }
}
