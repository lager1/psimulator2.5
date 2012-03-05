/*
 * created 5.3.2012
 */
package config.Components.simulatorConfig;

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