/*
 * Erstellt am 1.3.2012.
 */

package config.configTransformer;

import config.Components.*;
import static config.Components.HwTypeEnum.*;
import config.Components.NetworkModel;
import config.Components.simulatorConfig.DeviceSettings;
import device.Device;
import networkModule.NetMod;
import physicalModule.PhysicMod;
import psimulator2.Psimulator;
import static config.Components.simulatorConfig.DeviceSettings.NetworkModuleType.*;
import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import java.util.HashMap;
import java.util.Map;
import networkModule.L2.EthernetInterface;
import networkModule.L3.NetworkInterface;
import networkModule.SimpleSwitchNetMod;
import networkModule.TcpIpNetMod;
import physicalModule.Cable;
import physicalModule.SimulatorSwitchport;
import physicalModule.Switchport;

/**
 *
 * @author neiss
 */
public class Loader {

	Psimulator s = Psimulator.getPsimulator();

	private Map<Integer,Integer> switchporty = new HashMap<Integer, Integer>();	// odkladaci mapa mezi ID a cislama switchportu


	/**
	 * Metoda slouzi k nahravani konfigurace z Martinova modelu.
	 * @param network
	 */
	public void loadFromModel(NetworkModel network){

		for ( HwComponentModel device: network.getHwComponents() ){
			s.devices.add(createDevice(device));
		}

		connectCables(network);
	}

	/**
	 * Metoda na vytvoreni jednoho pocitace (device).
	 * @param model
	 * @return
	 */
	private Device createDevice(HwComponentModel model) {

		// vytvoreni samotnyho pocitace:
		Device pc = new Device(model.getId(), model.getDeviceName(), prevedTyp(model.getHwType()));

		// vytvoreni fysickyho modulu
		PhysicMod pm = pc.physicalModule;
		//buildeni switchportu:
		int cislovaniSwitchportu = 0;
		for (EthInterfaceModel ifaceModel : model.getInterfacesAsList()) {
			pm.addSwitchport(cislovaniSwitchportu, false, ifaceModel.getId());	//TODO: neresi se tu realnej switchport
			switchporty.put(ifaceModel.getId(), cislovaniSwitchportu);
			cislovaniSwitchportu++;
		}

		// nastaveni sitovyho modulu
		NetMod nm = createNetMod(model, pc);
		pc.setNetworkModule(nm);

		return pc;
	}




	/**
	 * Metoda na prevedeni Martinova typu pocitace na nas typ.
	 * @param t
	 * @return
	 */
	private Device.DeviceType prevedTyp(HwTypeEnum t) {
		Device.DeviceType type;
		if ((t == LINUX_ROUTER) || (t == END_DEVICE_NOTEBOOK) || (t == END_DEVICE_PC) || t == END_DEVICE_WORKSTATION) {
			type = Device.DeviceType.linux_computer;
		} else if (t == CISCO_ROUTER) {
			type = Device.DeviceType.cisco_router;
		} else if (t == CISCO_SWITCH || t == LINUX_SWITCH) {
			type = Device.DeviceType.simple_switch;
		} else {
			throw new LoaderException("Unknown or forbidden type of network device.");
		}
		return type;
	}


	/**
	 * Vytvareni sitovyho modulu. Predpoklada jiz kompletni fysickej modul.
	 * @param model konfigurace pocitace
	 * @param pc odkaz na uz hotovej pocitac
	 * @return
	 */
	private NetMod createNetMod(HwComponentModel model, Device pc) {

		DeviceSettings.NetworkModuleType netModType = model.getDevSettings().getNetModType();	// zjisteni typu modulu

		if (netModType == tcp_ip_netmod) {	// modul je pro router
			TcpIpNetMod nm = new TcpIpNetMod(pc);	// vytvoreni sitovyho modulu, pri nem se

			//nahrani interfacu:
			for (EthInterfaceModel ifaceModel : model.getInterfacesAsList()) {

				EthernetInterface ethInterface = new EthernetInterface
						(ifaceModel.getName(), new MacAddress(ifaceModel.getMacAddress()), nm.ethernetLayer); // vytvoreni novyho rozhrani
				int cisloSwitchportu = switchporty.get(ifaceModel.getId());	// zjistim si z odkladaci mapy, ktery cislo switchportu mam priradit
				ethInterface.addSwitchportSettings(nm.ethernetLayer.getSwitchport(cisloSwitchportu));	// samotny prirazeni switchportu
				nm.ethernetLayer.ifaces.add(ethInterface);	// pridani interfacu do ethernetovy vrstvy

				IPwithNetmask ip = null;
				if (ifaceModel.getIpAddress() != null) {
					ip = new IPwithNetmask(ifaceModel.getIpAddress(), 24, true);
				}

				NetworkInterface netInterface = new NetworkInterface(ifaceModel.getName(), ip, ethInterface, ifaceModel.isIsUp());
				nm.ipLayer.addNetworkInterface(netInterface);
			}

			return nm;
		} else if (netModType == simple_switch_netMod) {
			return new SimpleSwitchNetMod(pc);
		} else {
			throw new LoaderException("Unknown or forbidden type of network module.");
		}

	}

	/**
	 * Projde vsechny kabely a spoji nase sitovy prvky.
	 * @param network
	 */
	private void connectCables(NetworkModel network) {
		for (CableModel cableModel : network.getCables()) {
			Cable cable = new Cable(cableModel.getId(), cableModel.getDelay());

			SimulatorSwitchport swportFirst = findSwitchportFor(cableModel.getComponent1(), cableModel.getInterface1());
			cable.setFirstInterface(swportFirst);
			cable.setFirstDeviceId(cableModel.getComponent1().getId());

			SimulatorSwitchport swportSecond = findSwitchportFor(cableModel.getComponent2(), cableModel.getInterface2());
			cable.setSecondInterface(swportSecond);
			cable.setSecondDeviceId(cableModel.getComponent1().getId());
		}
	}

	/**
	 * Najde switchport, ktery odpovida zadanemu zarizeni a rozhrani.
	 * @param component1
	 * @param interface1
	 * @return
	 */
	private SimulatorSwitchport findSwitchportFor(HwComponentModel component1, EthInterfaceModel interface1) {
		for (Device device : s.devices) {
			if (device.id == component1.getId()) {
				for (Switchport swp : device.physicalModule.getSwitchports().values()) {
					if (swp instanceof SimulatorSwitchport && swp.configID == interface1.getId()) {
						return (SimulatorSwitchport)swp;
					}
				}
			}
		}

		throw new LoaderException(String.format("Nepodarilo se najit Device s id=%d a k nemu SimulatorSwichport s id=%d", component1.getId(), interface1.getId()));
	}
}
