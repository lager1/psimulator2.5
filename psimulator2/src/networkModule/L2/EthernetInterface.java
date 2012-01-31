/*
 * Vytvoreno 31.1.2012
 */
package networkModule.L2;

import dataStructures.MacAddress;
import java.util.List;
import java.util.Map;
import physicalModule.Switchport;
import physicalModule.SimulatorSwitchport;

/**
 * Representace ethernetovyho interface (sitovy karty) se switchovaci tabulkou. Spolecny pro switch i router. Ma jmeno,
 * mac adresu, muze mit vic switchportu, ma ethernetovou switchovaci tabulku. TODO: U switchovaci tabulky se zatim
 * neresi vyprseni zaznamu. Switchovaci tabulka nic nedela, kdyz ma interface jen jeden switchport (kvuli zrychleni).
 *
 * @author neiss
 */
public class EthernetInterface {

	private String name;
	private MacAddress mac;
	private Map<MacAddress, SwitchTableItem> switchingTable;
	private List<Switchport> switchports;

	private class SwitchTableItem {

		public SwitchTableItem(Switchport switchport, long time) {
			this.switchport = switchport;
			this.time = time;
		}
		public Switchport switchport;
		/**
		 * Systemovej cas (ve vterinach), kdy byl zaznam pridan. Slouzi jako casove razitko pro vyprseni zaznamu.
		 */
		public long time;
	}

	public MacAddress getMac() {
		return mac;
	}

	public String getName() {
		return name;
	}

	public Switchport getSwitchport(MacAddress mac) {
		if (switchports.size() == 1) {
			return switchports.get(0);
		} else {
			SwitchTableItem item = switchingTable.get(mac);
			if (item != null) {
				return item.switchport;
			} else {
				return null;
			}
		}
	}

	public void addSwitchTableItem(MacAddress mac, Switchport swport) {
		if (switchports.size() == 1) {
			return;
		} else {
			if (switchingTable.containsKey(mac)) {	// kontrola staryho zaznamu
				switchingTable.remove(mac);
			}
			switchingTable.put(mac, new SwitchTableItem(swport, System.nanoTime() / (10 ^ 9)));
		}
	}
}
