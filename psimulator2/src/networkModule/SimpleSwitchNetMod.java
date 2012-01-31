/*
 * Erstellt am 27.10.2011.
 * TODO: implementovat
 */

package networkModule;

import dataStructures.L2Packet;
import device.AbstractDevice;
import networkModule.L2.EthernetLayer;
import physicalModule.Switchport;

/**
 * Implementation of network module of generic simple switch.
 * @author neiss
 */
public class SimpleSwitchNetMod extends NetMod  {
	
	protected EthernetLayer linkLayer;

    public SimpleSwitchNetMod(AbstractDevice device) {
        super(device);
        linkLayer = new EthernetLayer(this);
    }

    /**
     * Prijimani od fysickyho modulu.
     * @param packet
     * @param swport
     */
    @Override
    public void receivePacket(L2Packet packet, Switchport swport) {
        linkLayer.receivePacket(packet, swport);
    }
}
