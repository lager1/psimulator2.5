/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import device.AbstractDevice;

import physicalModule.PhysicMod;

import networkModule.L2.EthernetLayer;
import networkModule.L3.IPLayer;
import networkModule.L4.TcpIpLayer;

/**
 * Síťový modul pro počítač, tedy včetně rozhraní pro aplikace.
 * Dedi od SimpleSwitchNetMod, tedy se v tyhle tride resi spis komunikace nahoru.
 * @author neiss
 */
public class TcpIpNetMod extends SimpleSwitchNetMod {
	
	
	public final IPLayer ipLayer = new IPLayer(this);
	public final TcpIpLayer tcpipLayer = new TcpIpLayer(this);

	public TcpIpNetMod(EthernetLayer ethernetLayer, AbstractDevice device, PhysicMod physicMod) {
		super(ethernetLayer, device, physicMod);
	}

    //tady budou muset bejt metody pro posilani dat a pro registraci aplikaci, tedy komunikaci s aplikacema

	

	@Override
	public boolean isSwitch() {
		return false;
	}
}
