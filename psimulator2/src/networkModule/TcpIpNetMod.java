/*
 * Erstellt am 26.10.2011.
 */

package networkModule;
//TODO: prejmenovat, ud2lat javadoc

import dataStructures.L2Packet;
import device.AbstractDevice;

import physicalModule.PhysicMod;

import networkModule.L2.EthernetLayer;
import networkModule.L3.IPLayer;
import networkModule.L4.TcpIpLayer;
import physicalModule.Switchport;

/**
 * Síťový modul pro počítač, tedy včetně rozhraní pro aplikace.
 * @author neiss
 */
public class TcpIpNetMod extends NetMod {


    public TcpIpNetMod(AbstractDevice device, PhysicMod physicMod) {
		super(device, physicMod);
    }

	public final EthernetLayer ethernetLayer = new EthernetLayer(this);
	public final IPLayer ipLayer = new IPLayer(this);
	public final TcpIpLayer tcpipLayer = new TcpIpLayer(this);
    

    //tady budou muset bejt metody pro posilani dat a pro registraci aplikaci, tedy komunikaci s aplikacema

	@Override
	public void receivePacket(L2Packet packet, int switchportNumber) {
		throw new UnsupportedOperationException("Not supported yet.");
	}


}
