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
}
