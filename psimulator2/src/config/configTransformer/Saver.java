/*
 * created 5.3.2012
 */
package config.configTransformer;

import device.Device;
import networkModule.L3.NetworkInterface;
import networkModule.L3.RoutingTable;
import networkModule.TcpIpNetMod;
import psimulator2.Psimulator;
import shared.Components.EthInterfaceModel;
import shared.Components.HwComponentModel;
import shared.Components.NetworkModel;
import shared.Components.simulatorConfig.DeviceSettings;
import shared.Components.simulatorConfig.RoutingTableConfig;

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
			}

			// TODO: saveNatTable();
		}
	}

	/**
	 * Saves IP address with netmask and isUp of all interfaces of device. Saves also NetworkModuleType.
	 *
	 * @param hwComponentModel
	 * @param device
	 */
	private void saveInterfaces(HwComponentModel hwComponentModel, Device device) {
		if (!(device.getNetworkModule() instanceof TcpIpNetMod)) { // TODO: instanceof nahradit typem.
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
