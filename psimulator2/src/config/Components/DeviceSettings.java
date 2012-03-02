/*
 * Erstellt am 2.3.2012.
 */

package config.Components;

import config.Components.simulatorConfig.RoutingTableConfig;

/**
 *
 * @author Tomas Pitrinec
 */
public class DeviceSettings {
	
	private NetworkModuleType netModType;
	private RoutingTableConfig routingTabConfig;

	public NetworkModuleType getNetModType() {
		return netModType;
	}

	public RoutingTableConfig getRoutingTabConfig() {
		return routingTabConfig;
	}

	public void setNetModType(NetworkModuleType netModType) {
		this.netModType = netModType;
	}

	public void setRoutingTabConfig(RoutingTableConfig routingTabConfig) {
		this.routingTabConfig = routingTabConfig;
	}
	
	
	
	
	
	
	public enum NetworkModuleType{
		tcp_ip_netmod,
		simple_switch_netMod,
	}

}
