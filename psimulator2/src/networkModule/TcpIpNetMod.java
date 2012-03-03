/*
 * Erstellt am 26.10.2011.
 */

package networkModule;

import device.Device;


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

	/**
	 * Konstruktor sitovyho modulu. 
	 * Predpoklada uz hotovej pocitac a fysickej modul, protoze zkouma jeho nastaveni.
	 * @param device 
	 */
	public TcpIpNetMod(Device device) {
		super(device);
	}

    //tady budou muset bejt metody pro posilani dat a pro registraci aplikaci, tedy komunikaci s aplikacema

	

	@Override
	public boolean isSwitch() {
		return false;
	}
}
