/*
 * Vytvoreno 31.1.2012
 */
package networkModule.L2;

import dataStructures.EthernetPacket;
import dataStructures.MacAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Representace ethernetovyho interface (sitovy karty) se switchovaci tabulkou. Spolecny pro switch i router. Ma jmeno,
 * mac adresu, muze mit vic switchportu, ma ethernetovou switchovaci tabulku.
 *
 * TODO: U switchovaci tabulky se zatim neresi vyprseni zaznamu.
 * TODO: Neni zadnej konstruktor.
 *
 * Switchovaci tabulka nic nedela, kdyz ma interface jen jeden switchport (kvuli zrychleni).
 *
 * @author neiss
 */
public class EthernetInterface {

	public final String name;
	protected MacAddress mac;
	private final Map<MacAddress, SwitchTableItem> switchingTable = new HashMap<MacAddress, SwitchTableItem>();
	private final Map<Integer,SwitchportSettings> switchpors = new HashMap<Integer, SwitchportSettings>();
	/**
	 * Je-li povoleno switchovani, napr. u routeru defualtne zakazano.
	 */
	protected boolean switchingEnabled = false;
	private final EthernetLayer etherLayer;

// Konstruktor:
	public EthernetInterface(String name, MacAddress mac, EthernetLayer etherLayer) {
		this.name = name;
		this.mac = mac;
		this.etherLayer = etherLayer;
	}


	/**
	 * Returns mac address of this interface.
	 * @return 
	 */
	public MacAddress getMac() {
		return mac;
	}


	/**
	 * Da switchport, pres kterej se smeruje na zadanou mac adresu.
	 * @param mac
	 * @return 
	 */
	public SwitchportSettings getSwitchport(MacAddress mac) {
		if (switchpors.size() == 1) {
			return switchpors.get(0);
		} else {
			SwitchTableItem item = switchingTable.get(mac);
			if (item != null) {
				return item.swportSett;
			} else {
				return null;
			}
		}
	}

	public void addSwitchTableItem(MacAddress mac, SwitchportSettings swportSett) {
		if (switchpors.size() == 1) {
			// nic se nedela
		} else {
			if (switchingTable.containsKey(mac)) {	// kontrola staryho zaznamu
				switchingTable.remove(mac);
			}
			switchingTable.put(mac, new SwitchTableItem(swportSett, System.nanoTime() / (10 ^ 9)));
		}
	}

	/**
	 * Odesle paket na vsechny switchporty rozhrani.
	 * @param p 
	 */
	protected void dispatchPacketOnAllSwitchports(EthernetPacket p){
		for(SwitchportSettings switchports : switchpors.values()){
			if(switchports.isUp){
				etherLayer.getNetMod().getPhysicMod().sendPacket(p, switchports.switchportNumber);
			}
		}
	}
	
	
	
	
		
	
	


// Polozka switchovaci tabulky:
	private class SwitchTableItem {
		public SwitchTableItem(SwitchportSettings swportSett, long time) {
			this.swportSett = swportSett;
			this.time = time;
		}
		public SwitchportSettings swportSett;
		/**
		 * Systemovej cas (ve vterinach), kdy byl zaznam pridan. Slouzi jako casove razitko pro vyprseni zaznamu.
		 */
		public long time;
	}
}
