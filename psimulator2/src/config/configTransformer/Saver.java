/*
 * created 5.3.2012
 */
package config.configTransformer;

import device.Device;
import java.util.ArrayList;
import java.util.List;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.CiscoWrapperRT;
import networkModule.L3.CiscoWrapperRT.CiscoRecord;
import networkModule.L3.NetworkInterface;
import networkModule.L3.RoutingTable;
import networkModule.L3.nat.AccessList;
import networkModule.L3.nat.NatTable;
import networkModule.L3.nat.Pool;
import networkModule.L3.nat.PoolAccess;
import networkModule.TcpIpNetMod;
import psimulator2.Psimulator;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.Components.NetworkModel;
import shared.Components.simulatorConfig.*;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Saver {

	Psimulator s = Psimulator.getPsimulator();
	private final NetworkModel networkModel;

	public Saver(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}

	/**
	 * Ulozi nastaveni do netwrkModelu zadanyho v konstruktoru.
	 */
	public void saveToModel() {
		for (Device device : s.devices) {
			HwComponentModel hwComponentModel = networkModel.getHwComponentModelById(device.configID);

			if (hwComponentModel.getDevSettings() == null) {	// nema/li jeste simulatorovy nastaveni, musi se spustit
				hwComponentModel.setDevSettings(new DeviceSettings());
			}

			saveInterfaces(hwComponentModel, device);

			if (device.getNetworkModule().isStandardTcpIpNetMod()) {
				saveRoutingTable((TcpIpNetMod) (device.getNetworkModule()), hwComponentModel);

				saveNatTable((TcpIpNetMod) (device.getNetworkModule()), hwComponentModel);
			}
		}
	}

	/**
	 * Saves IP address with netmask and isUp of all interfaces of device. Saves also NetworkModuleType.
	 *
	 * @param hwComponentModel
	 * @param device
	 */
	private void saveInterfaces(HwComponentModel hwComponentModel, Device device) {
		if (!(device.getNetworkModule().isStandardTcpIpNetMod())) {
				// -> Zatim se uklada sitovy modul jen tehdy, pokud to je TcpIpNetMod, navic se uklada zatim jen IPLayer.
			hwComponentModel.getDevSettings().setNetModType(DeviceSettings.NetworkModuleType.simple_switch_netMod);
			return;
		}

		TcpIpNetMod netMod = (TcpIpNetMod) device.getNetworkModule();
		hwComponentModel.getDevSettings().setNetModType(DeviceSettings.NetworkModuleType.tcp_ip_netmod);

		for (NetworkInterface iface : netMod.ipLayer.getNetworkIfaces()) {
			EthInterfaceModel ethIfaceModel = hwComponentModel.getEthInterface(iface.configID);
			if (iface.getIpAddress() != null) {
				ethIfaceModel.setIpAddress(iface.getIpAddress().toString());
			}
			ethIfaceModel.setIsUp(iface.isUp);
		}
	}

	private void saveRoutingTable(TcpIpNetMod netMod, HwComponentModel model) {
		RoutingTableConfig rtc = new RoutingTableConfig();	// vytvorim novou prazdnou konfiguraci routovaci tabulky
		model.getDevSettings().setRoutingTabConfig(rtc);	// priradim tu novou konfiguraci do nastaveni pocitace
		RoutingTable rt = netMod.ipLayer.routingTable;

		if (netMod.ipLayer instanceof CiscoIPLayer) { // cisco uklada veci z wrapperu, ne obsah RT
			CiscoWrapperRT wrapper = ((CiscoIPLayer) netMod.ipLayer).wrapper;
			for (int i = 0; i < wrapper.size(); i++) {
				CiscoRecord record = wrapper.vratZaznam(i);
				if (record.getBrana() != null) { // adresa brana
					rtc.addRecord(record.getAdresat().toString(), null, record.getBrana().toString());
				} else { // adresa rozhrani
					rtc.addRecord(record.getAdresat().toString(), record.getRozhrani().name, null);
				}
			}
		} else {
			for (int i = 0; i < rt.size(); i++) {
				RoutingTable.Record radek = rt.getRecord(i);
				if (radek.brana != null) {
					rtc.addRecord(radek.adresat.toString(), radek.rozhrani.name, radek.brana.toString());
				} else {
					rtc.addRecord(radek.adresat.toString(), radek.rozhrani.name, null);
				}
			}
		}
	}

	private void saveNatTable(TcpIpNetMod netMod, HwComponentModel model) {
		NatConfig config = new NatConfig();
		model.getDevSettings().setNatConfig(config);

		NatTable natTable = netMod.ipLayer.getNatTable();

		// inside
		List<String> insides = new ArrayList<>();
		for (NetworkInterface iface : natTable.getInside()) {
			insides.add(iface.name);
		}
		config.setInside(insides);

		// outside
		config.setOutside(natTable.getOutside() != null ? natTable.getOutside().name : null);

		// pool
		List<NatPoolConfig> poolConfig = new ArrayList<>();
		for (Pool pool : natTable.lPool.getSortedPools()) {
			poolConfig.add(new NatPoolConfig(pool.name, pool.posledni() != null ? pool.prvni().toString() : null, pool.prvni() != null ? pool.posledni().toString() : null, pool.prefix));
		}
		config.setPools(poolConfig);

		// poolAccess
		List<NatPoolAccessConfig> pac = new ArrayList<>();
		for (PoolAccess pa : natTable.lPoolAccess.getSortedPoolAccess()) {
			pac.add(new NatPoolAccessConfig(pa.access, pa.poolName, pa.overload));
		}
		config.setPoolAccesses(pac);

		// accessList
		List<NatAccessListConfig> alc = new ArrayList<>();
		for (AccessList ac : natTable.lAccess.getList()) {
			alc.add(new NatAccessListConfig(ac.cislo, ac.ip.getIp().toString(), ac.ip.getMask().getWildcardRepresentation()));
		}
		config.setAccessLists(alc);

		// static rules
		List<StaticRule> rules = new ArrayList<>();
		for (NatTable.StaticRule rule : natTable.getStaticRules()) {
			rules.add(new StaticRule(rule.in.toString(), rule.out.toString()));
		}
		config.setRules(rules);
	}
}
