/*
 * Erstellt am 1.3.2012.
 */

package config.configTransformer;

import config.Components.*;
import static config.Components.HwTypeEnum.*;
import config.Components.NetworkModel;
import device.Device;
import networkModule.NetMod;
import physicalModule.PhysicMod;
import psimulator2.Psimulator;
import static config.Components.DeviceSettings.NetworkModuleType.*;
import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import java.util.HashMap;
import java.util.Map;
import networkModule.L2.EthernetInterface;
import networkModule.L3.NetworkInterface;
import networkModule.SimpleSwitchNetMod;
import networkModule.TcpIpNetMod;
import physicalModule.Switchport;

/**
 *
 * @author neiss
 */
public class Loading {

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

	}

	/**
	 * Metoda na vytvoreni jednoho pocitace (device).
	 * @param model
	 * @return
	 */
	private Device createDevice(HwComponentModel model){

		// vytvoreni samotnyho pocitace:
		Device pc = new Device(model.getId(), model.getDeviceName(), prevedTyp(model.getHwType()) );

		// vytvoreni fysickyho modulu
		PhysicMod pm = pc.physicalModule;
		//buildeni switchportu:
		int cislovaniSwitchportu = 0;
		for (EthInterfaceModel ifaceModel : model.getInterfacesAsList()){
			pm.addSwitchport(cislovaniSwitchportu,false, ifaceModel.getId());	//TODO: neresi se tu realnej switchport
			switchporty.put(ifaceModel.getId(), cislovaniSwitchportu);
			cislovaniSwitchportu++;
		}

		// nastaveni sitovyho modulu
		NetMod nm = createNetMod(model,pc);
		pc.setNetworkModule(nm);

		return pc;
	}




	/**
	 * Metoda na prevedeni Martinova typu pocitace na nas typ.
	 * @param t
	 * @return
	 */
	private Device.DeviceType prevedTyp(HwTypeEnum t){
		Device.DeviceType type;
		if( (t == LINUX_ROUTER) || (t == END_DEVICE_NOTEBOOK) || (t==END_DEVICE_PC) || t==END_DEVICE_WORKSTATION ){
			type=Device.DeviceType.linux_computer;
		} else if (t==CISCO_ROUTER) {
			type = Device.DeviceType.cisco_router;
		} else if (t==CISCO_SWITCH || t==LINUX_SWITCH ){
			type=Device.DeviceType.simple_switch;
		} else {
			throw new LoadingException("Unknown or forbidden type of network device.");
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
			TcpIpNetMod nm = new TcpIpNetMod(pc);

			//nahrani interfacu:
			for (EthInterfaceModel ifaceModel : model.getInterfacesAsList()) {

				EthernetInterface ethInterface = new EthernetInterface
						(ifaceModel.getName(), new MacAddress(ifaceModel.getMacAddress()), nm.ethernetLayer); // vytvoreni novyho rozhrani
				int cisloSwitchportu = switchporty.get(ifaceModel.getId());	// zjistim si z odkladaci mapy, ktery cislo switchportu mam priradit
				ethInterface.addSwitchportSettings(nm.ethernetLayer.getSwitchport(cisloSwitchportu));	// samotny prirazeni switchportu
				nm.ethernetLayer.ifaces.add(ethInterface);	// pridani interfacu do ethernetovy vrstvy

				if (ifaceModel.getIpAddress() != null) {
					// nm.ipLayer.setIpAddressOnInterface(nm.ipLayer., null);	TODO: dodelat nastavovani IP na NetworkInterface z konfigurace
				}
			}

			return nm;
		} else if (netModType == simple_switch_netMod) {
			return new SimpleSwitchNetMod(pc);
		} else {
			throw new LoadingException("Unknown or forbidden type of network module.");
		}

	}

}
