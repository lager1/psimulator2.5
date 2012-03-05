/*
 * Erstellt am 2.3.2012.
 */

package config.Components.simulatorConfig;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * Ukladaci struktura pro routovaci tabulku.
 * @author Tomas Pitrinec
 */
public class RoutingTableConfig {

	private List<Record> records;

	@XmlElement(name = "routingTableItem")
	public List<Record> getRecords() {
		return records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}





	/**
	 * Ukladaci struktura pro jeden zaznam.
	 */
	public class Record {
		private String destination;
		private String interfaceName;
		private String gateway;

		public String getDestination() {
			return destination;
		}

		public String getGateway() {
			return gateway;
		}

		public String getInterfaceName() {
			return interfaceName;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}

		public void setGateway(String gateway) {
			this.gateway = gateway;
		}

		public void setInterfaceName(String interfaceName) {
			this.interfaceName = interfaceName;
		}


	}

}
