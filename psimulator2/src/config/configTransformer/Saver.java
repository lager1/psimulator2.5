/*
 * created 5.3.2012
 */

package config.configTransformer;

import config.Components.EthInterfaceModel;
import config.Components.HwComponentModel;
import config.Components.NetworkModel;
import config.Components.simulatorConfig.DeviceSettings;
import device.Device;
import networkModule.L3.NetworkInterface;
import networkModule.TcpIpNetMod;
import psimulator2.Psimulator;

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

	public void saveToModel() {
		for (Device device : s.devices) {
			HwComponentModel hwComponentModel = networkModel.getHwComponentModelById(device.configID);

			saveInterfaces(hwComponentModel, device);

//			saveRoutingTable();

//			saveNatTable();
		}
	}

	/**
	 * Saves IP address with netmask and isUp of all interfaces of device.
	 * Saves also NetworkModuleType.
	 * @param hwComponentModel
	 * @param device
	 */
	private void saveInterfaces(HwComponentModel hwComponentModel, Device device) {
		if (!(device.getNetworkModule() instanceof TcpIpNetMod)) { // zatim se uklada sitovy modul jen tehdy, pokud to je TcpIpNetMod, navic se uklada zatim jen IPLayer
			hwComponentModel.getDevSettings().setNetModType(DeviceSettings.NetworkModuleType.simple_switch_netMod);
			return;
		}

		TcpIpNetMod netMod = (TcpIpNetMod) device.getNetworkModule();
		hwComponentModel.getDevSettings().setNetModType(DeviceSettings.NetworkModuleType.tcp_ip_netmod);

		for (NetworkInterface iface : netMod.ipLayer.getNetworkIfaces()) {
			EthInterfaceModel ethIfaceModel = hwComponentModel.getEthInterface(iface.configID);
			ethIfaceModel.setIpAddress(iface.getIpAddress().toString());
			ethIfaceModel.setIsUp(iface.isUp);
		}
	}
}
