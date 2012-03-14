/*
 * Vytvoreno 31.1.2012
 */
package networkModule.L2;

import dataStructures.EthernetPacket;
import dataStructures.MacAddress;
import java.util.HashMap;
import java.util.Map;
import logging.Loggable;
import physicalModule.Switchport;

/**
 * Representace ethernetovyho interface (sitovy karty) se switchovaci tabulkou. Spolecny pro switch i router. Ma jmeno,
 * mac adresu, muze mit vic switchportu, ma ethernetovou switchovaci tabulku.
 *
 * TODO: Zjistit, jak dlouho je platnej zaznam ve switchovaci tabulce, zatim nastaveno na 20 s.
 * Switchovaci tabulka nic nedela, kdyz ma interface jen jeden switchport (kvuli zrychleni).
 *
 * @author neiss
 */
public class EthernetInterface implements Loggable {

	public final String name;
	protected MacAddress mac;
	private final Map<MacAddress, SwitchTableItem> switchingTable = new HashMap<>();
	/**
	 * Seznam prirazenejch switchportu. Je dulezity, aby to bylo private, pridavat se musi jen v metode
	 * addSwitchportSettings, aby se v tom SwitchportSettings nastavilo assignedInterface.
	 */
	private final Map<Integer, SwitchportSettings> switchports = new HashMap<>();
	/**
	 * Je-li povoleno switchovani, napr. u routeru defualtne zakazano.
	 */
	public boolean switchingEnabled = false;
	private final EthernetLayer etherLayer;

	private static int switchTableTimeout = 20;




// Konstruktor a veci pri buildeni: -------------------------------------------------------------------------

	public EthernetInterface(String name, MacAddress mac, EthernetLayer etherLayer) {
		this.name = name;
		this.mac = mac;
		this.etherLayer = etherLayer;
	}

	/**
	 * Prida switchport.
	 *
	 * @param s
	 */
	public void addSwitchportSettings(SwitchportSettings s) {
		s.assignedInterface = this;
		switchports.put(s.switchportNumber, s);
	}



// funkce k sitovy komunikaci: ----------------------------------------------------------------------------------


	public void addSwitchTableItem(MacAddress mac, SwitchportSettings swportSett) {
		if (switchports.size() == 1) {
			// nic se nedela
		} else {
			if (switchingTable.containsKey(mac)) {	// kontrola staryho zaznamu
				switchingTable.remove(mac);
			}
			switchingTable.put(mac, new SwitchTableItem(swportSett, System.currentTimeMillis()));
		}
	}

	/**
	 * Da switchport, pres kterej se smeruje na zadanou mac adresu. (smerovani)
	 *
	 * @param mac
	 * @return
	 */
	public SwitchportSettings getSwitchport(MacAddress mac) {
		if (switchports.size() == 1) {
			return switchports.get(0);
		} else {
			SwitchTableItem item = switchingTable.get(mac);
			if (item == null || item.isOutdated()) {
				return null;
			} else {
				return item.swportSett;
			}
		}
	}

	/**
	 * Odesle zadanej packet na vsechny switchporty rozhrani krome switchportu incoming.
	 * @param p
	 * @param incoming switchport, na nejz paket prisel, tam uz se znova neposila
	 */
	void transmitPacketOnAllSwitchports(EthernetPacket p, SwitchportSettings incoming) {
		for (SwitchportSettings switchport : switchports.values()) {
			if (switchport.isUp && switchport != incoming) {
				etherLayer.getNetMod().getPhysicMod().sendPacket(p, switchport.switchportNumber);
			}
		}
	}




// gettry a zjistovaci fce vetsinou mimo sitovou komunikaci ----------------------------------------------------------

	@Override
	public String getDescription() {
		return etherLayer.getNetMod().getDevice().getName()+": EtherIface "+name;
	}



	/**
	 * Returns mac address of this interface.
	 *
	 * @return
	 */
	public MacAddress getMac() {
		return mac;
	}



	/**
	 * Returns true, if the interface is on the cable connected to some other interface.
	 *
	 * @return
	 */
	public boolean isConnected() {
		for (SwitchportSettings swportsett : switchports.values()) {
			Switchport swport = etherLayer.physicMod.getSwitchports().get(swportsett.switchportNumber);
			if (swport.isConnected()) {	// swport by nemel bejt nikdy null, protoze na zacatku behu ethernetovy vrstvy se vola metoda exploreHardware
				return true;
			}
		}
		return false;
	}




// Polozka switchovaci tabulky: ----------------------------------------------------------------------------------
	private class SwitchTableItem {

		public SwitchportSettings swportSett;

		/**
		 * Systemovej cas (v ms), kdy byl zaznam pridan. Slouzi jako casove razitko pro vyprseni zaznamu.
		 */
		public long time;

		public SwitchTableItem(SwitchportSettings swportSett, long time) {
			this.swportSett = swportSett;
			this.time = time;
		}

		public boolean isOutdated(){
			if(System.currentTimeMillis() > time+switchTableTimeout*1000){
				return true;
			}
			return false;
		}

	}
}
