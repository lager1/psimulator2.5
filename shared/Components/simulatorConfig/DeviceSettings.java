/*
 * Erstellt am 2.3.2012.
 */

package shared.Components.simulatorConfig;


/**
 * Sem se bude davat nastaveni Device, ktery je jen pro simulator, aby neprekazelo Martinoj Svihlikoj.
 *
 * @author Tomas Pitrinec
 */
public class DeviceSettings {

    private NetworkModuleType netModType;
    private RoutingTableConfig routingTabConfig;
    private NatConfig natConfig;
    private Boolean stpEnabled;

    public NatConfig getNatConfig() {
        return natConfig;
    }

    public void setNatConfig(NatConfig natConfig) {
        this.natConfig = natConfig;
    }

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

    public Boolean getStpConfig() {
        return stpEnabled;
    }

    public void setStpConfig(Boolean stpEnabled) {
        this.stpEnabled = stpEnabled;
    }

    public enum NetworkModuleType {
        tcp_ip_netmod,
        simple_switch_netMod,
    }

}
