/*
 * Erstellt am 1.3.2012.
 */
package config.configTransformer;


import device.Device;
import networkModule.NetMod;
import physicalModule.PhysicMod;
import psimulator2.Psimulator;
import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;
import networkModule.L3.nat.NatTable;
import networkModule.L3.NetworkInterface;
import networkModule.L3.nat.Pool;
import networkModule.SimpleSwitchNetMod;
import networkModule.TcpIpNetMod;
import physicalModule.Cable;
import physicalModule.SimulatorSwitchport;
import physicalModule.Switchport;
import shared.Components.*;
import shared.Components.simulatorConfig.*;
import shared.Components.simulatorConfig.DeviceSettings.NetworkModuleType;

// TODO: PC nemuze mit 2 rozhrani stejnyho jmena !!!

/**
 *
 * @author Tomas Pitrinec
 * @author Stanislav Rehak
 */
public class Loader implements Loggable {

	Psimulator s = Psimulator.getPsimulator();
	/**
	 * odkladaci mapa mezi ID a cislama switchportu
	 * Klicem je id z konfiguraku, hodnotou je prirazeny cislo switchportu
	 * Pozor, pouziva se pro vsechny pocitace (tedy predpoklada se
	 */
	private Map<Integer, Integer> switchporty = new HashMap<>();	// odkladaci mapa mezi ID a cislama switchportu
	private final NetworkModel networkModel;

	/**
	 * Mnozina idecek, slouzi ke kontrolovani, je-li kazdy ID unikatni (kdyby se nejak rozbil konfigurak, treba pres svnko.
	 */
	private Set<Integer> idecka = new HashSet<>();
	/**
	 * Mnozina jmen, slouzi ke kontrolovani, zda jeden Device ma unikatni jmena sitovych rozhrani.
	 */
	private Set<String> names = new HashSet<>();





	public Loader(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}

	/**
	 * Metoda slouzi k nahravani konfigurace z Martinova modelu.
	 *
	 */
	public void loadFromModel() {

		try {	// chytaji se tady vsechny vyjimky vyhozeny z konfigurace

			for (HwComponentModel device : networkModel.getHwComponents()) {
				registerID(device.getId());
				if (device.getHwType() == shared.Components.HwTypeEnum.REAL_PC) {
					continue;
				}

				s.devices.add(createDevice(device));
			}


			connectCables();

		} catch (Exception ex) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Spatna konfigurace, byla hozena vyjimka: ", ex);
			Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "Spatny konfiguracni soubor. Koncim. " + ex.toString(), null);
		}
	}

	/**
	 * Metoda na vytvoreni jednoho pocitace (device).
	 *
	 * @param model
	 * @return
	 */
	private Device createDevice(HwComponentModel model) {

		// vytvoreni samotnyho pocitace:
		Device pc = new Device(model.getId(), model.getName(), prevedTyp(model.getHwType()));
//		System.out.printf("device: id: %s name: %s, type: %s \n", model.getId(), model.getDeviceName(), model.getHwType());

		// vytvoreni fysickyho modulu:
		PhysicMod pm = pc.physicalModule;
		//buildeni switchportu:
		int cislovaniSwitchportu = 0;
//		System.out.printf("  pocet rozhrani: %d\n", model.getEthInterfaceCount());
//		System.out.println("  "+model.getInterfacesMap().toString());
		names = new HashSet<>();
		for (EthInterfaceModel ifaceModel : (Collection<EthInterfaceModel>) model.getEthInterfaces()) { // prochazim interfacy a pridavam je jako switchporty
			registerID(ifaceModel.getId());
			registerName(ifaceModel.getName(), model);
//			System.out.println("jedu");
			pm.addSwitchport(cislovaniSwitchportu, false, ifaceModel.getId());	//TODO: neresi se tu realnej switchport
			switchporty.put(ifaceModel.getId(), cislovaniSwitchportu);
//			System.out.println("Pridal jem switchport pocitaci  s id="+ifaceModel.getId()+" s cislem "+cislovaniSwitchportu);
			cislovaniSwitchportu++;
//			System.out.print("X "+ifaceModel.getId());
		}

		// nastaveni sitovyho modulu
		NetMod nm = createNetMod(model, pc);
		pc.setNetworkModule(nm);

		return pc;
	}

	/**
	 * Metoda na prevedeni Martinova typu pocitace na nas typ.
	 *
	 * @param t
	 * @return
	 */
	private Device.DeviceType prevedTyp(HwTypeEnum t) {
		Device.DeviceType type;
		if ((t == HwTypeEnum.LINUX_ROUTER) || (t == HwTypeEnum.END_DEVICE_NOTEBOOK) || (t == HwTypeEnum.END_DEVICE_PC) || t == HwTypeEnum.END_DEVICE_WORKSTATION) {
			type = Device.DeviceType.linux_computer;
		} else if (t == HwTypeEnum.CISCO_ROUTER) {
			type = Device.DeviceType.cisco_router;
		} else if (t == HwTypeEnum.CISCO_SWITCH || t == HwTypeEnum.LINUX_SWITCH) {
			type = Device.DeviceType.simple_switch;
		} else {
			throw new LoaderException("Unknown or forbidden type of network device: " + t);
		}
		return type;
	}

	/**
	 * Vytvareni sitovyho modulu. Predpoklada jiz kompletni fysickej modul.
	 *
	 * @param model konfigurace pocitace
	 * @param pc odkaz na uz hotovej pocitac
	 * @return
	 */
	private NetMod createNetMod(HwComponentModel model, Device pc) {

		DeviceSettings.NetworkModuleType netModType;

		if (model.getDevSettings() != null) {
			netModType = model.getDevSettings().getNetModType();	// zjisteni typu modulu z konfiguraku
		} else { // neni ulozeno v konfiguraci, o jaky typ modulu se jedna
			switch (pc.type) {
				case cisco_router:
				case linux_computer:
					netModType = NetworkModuleType.tcp_ip_netmod;
					break;
				case simple_switch:
					netModType = NetworkModuleType.simple_switch_netMod;
					break;
				default:
					throw new AssertionError();
			}
		}

		if (netModType == DeviceSettings.NetworkModuleType.tcp_ip_netmod) {	// modul je pro router
			return createTcpIpNetMod(model, pc);
		} else if (netModType == DeviceSettings.NetworkModuleType.simple_switch_netMod) {
			return createSimpleSwitchNetMod(model, pc);
		} else {
			throw new LoaderException("Unknown or forbidden type of network module.");
		}

	}

	/**
	 * Metoda vytvori sitovej model routeru. Ke kazdymu switchportu priradi jeden interface, pojmenuje je, pripadne
	 * nastavi adresy, vytvori a nastavi routovaci tabulku a nat.
	 *
	 * @param model
	 * @param pc
	 * @return
	 */
	private TcpIpNetMod createTcpIpNetMod(HwComponentModel model, Device pc) {
		TcpIpNetMod nm = new TcpIpNetMod(pc);	// vytvoreni sitovyho modulu, pri nem se

		//nahrani interfacu:
		for (EthInterfaceModel ifaceModel : model.getInterfacesAsList()) {	// pro kazdy rozhrani

			EthernetInterface ethInterface = nm.ethernetLayer.addInterface(ifaceModel.getName(), new MacAddress(ifaceModel.getMacAddress()));
			// -> pridani novyho rozhrani ethernetovy vrstve, interface si jeste podrzim, abych mu moh pridavat switchporty
			int cisloSwitchportu = switchporty.get(ifaceModel.getId());	// zjistim si z odkladaci mapy, ktery cislo switchportu mam priradit
			ethInterface.addSwitchportSettings(nm.ethernetLayer.getSwitchport(cisloSwitchportu));	// samotny prirazeni switchportu

			IPwithNetmask ip = null;
			if (ifaceModel.getIpAddress() != null && !ifaceModel.getIpAddress().equals("")) {
				ip = new IPwithNetmask(ifaceModel.getIpAddress(), 24, true);
			}

			NetworkInterface netInterface = new NetworkInterface(ifaceModel.getId(), ifaceModel.getName(), ip, ethInterface, ifaceModel.isIsUp());
			nm.ipLayer.addNetworkInterface(netInterface);
		}

		//nahrani osatnich nastaveni sitovyho modulu:

		// nastaveni routovaci tabulky:
		if (model.getDevSettings() != null) {	// network modul uz byl nekdy ulozenej, nahrava se z neho

			for(Record record: model.getDevSettings().getRoutingTabConfig().getRecords()) { //pro vsechny zaznamy
				IPwithNetmask adresat = new IPwithNetmask(record.getDestination(), 32, false);
				IpAddress brana = null;
				if (record.getGateway() != null) {
					brana = new IpAddress(record.getGateway());
				}
				NetworkInterface iface = nm.ipLayer.getNetworkInteface(record.getInterfaceName());
				nm.ipLayer.routingTable.addRecordWithoutControl(adresat, brana, iface);
			}
		} else {	// network modul jeste nebyl ulozenej, je cerstve vytvorenej Martinouvym simulatorem, je potreba tabulku donastavit dle rozhrani
			nm.ipLayer.updateNewRoutingTable();
		}

		// nastaveni NATu:
		if (model.getDevSettings() != null && model.getDevSettings().getNatConfig() != null) {
			NatConfig config = model.getDevSettings().getNatConfig();
			NatTable natTable = nm.ipLayer.getNatTable();

			// inside
			if (config.getInside() != null) {
				for (NetworkInterface iface : nm.ipLayer.getSortedNetworkIfaces()) {
					if (config.getInside().contains(iface.name)) {
						natTable.pridejRozhraniInside(iface);
					}
				}
			}

			// outside
			if (config.getOutside() != null) {
				NetworkInterface iface = nm.ipLayer.getNetworkInteface(config.getOutside());
				if (iface != null) {
					natTable.nastavRozhraniOutside(iface);
				}
			}

			// pool
			if (config.getPools() != null) {
				for (NatPoolConfig pool : config.getPools()) {
					IpAddress start = new IpAddress(pool.getStart());
					IpAddress end = new IpAddress(pool.getEnd());
					natTable.lPool.pridejPool(start, end, pool.getPrefix(), pool.getName());
				}
			}

			// poolAccess
			if (config.getPoolAccesses() != null) {
				for (NatPoolAccessConfig poolAcc : config.getPoolAccesses()) {
					natTable.lPoolAccess.pridejPoolAccess(poolAcc.getNumber(), poolAcc.getPoolName(), poolAcc.isOverload());
				}
			}

			// accessList
			if (config.getAccessLists() != null) {
				for (NatAccessListConfig acc : config.getAccessLists()) {
					IpNetmask mask = IpNetmask.maskFromWildcard(acc.getWildcard());
					IpAddress adr = new IpAddress(acc.getAddress());
					IPwithNetmask all = new IPwithNetmask(adr, mask);
					natTable.lAccess.pridejAccessList(all, acc.getNumber());
				}
			}

			// static rules
			if (config.getRules() != null) {
				for (StaticRule rule : config.getRules()) {
					natTable.pridejStatickePravidloLinux(new IpAddress(rule.getIn()), new IpAddress(rule.getOut()));
				}
			}
		}

		//TODO pripadne nejaky dalsi nastaveni 4. vrstvy?


		return nm;
	}

	/**
	 * Vytvori sitovej modul switche, uplne ignoruje jeho nastaveni z konfigurace (kdyby tam nejaky bylo).
	 *
	 * @param model
	 * @param pc
	 * @return
	 */
	private NetMod createSimpleSwitchNetMod(HwComponentModel model, Device pc) {
		SimpleSwitchNetMod nm = new SimpleSwitchNetMod(pc);
		EthernetInterface ethIface = nm.ethernetLayer.addInterface("switch_default", MacAddress.getRandomMac());
			// -> switchi se priradi jedno rozhrani a da se mu nahodna mac
		nm.ethernetLayer.addAllSwitchportsToGivenInterface(ethIface);
		ethIface.switchingEnabled = true;
		return nm;
	}

	/**
	 * Projde vsechny kabely a spoji nase sitovy prvky.
	 *
	 * @param network
	 */
	private void connectCables() {
		for (CableModel cableModel : networkModel.getCables()) {
			registerID(cableModel.getId());
			Cable cable = new Cable(cableModel.getId(), cableModel.getDelay());

			SimulatorSwitchport swportFirst = findSwitchportFor(cableModel.getComponent1(), cableModel.getInterface1());
			cable.setFirstSwitchport(swportFirst);
			cable.setFirstDeviceId(cableModel.getComponent1().getId());

			SimulatorSwitchport swportSecond = findSwitchportFor(cableModel.getComponent2(), cableModel.getInterface2());
			cable.setSecondSwitchport(swportSecond);
			cable.setSecondDeviceId(cableModel.getComponent1().getId());
		}
	}

	/**
	 * Najde switchport, ktery odpovida zadanemu zarizeni a rozhrani.
	 *
	 * @param component1
	 * @param interface1
	 * @return
	 */
	private SimulatorSwitchport findSwitchportFor(HwComponentModel component1, EthInterfaceModel interface1) {
		for (Device device : s.devices) {
			if (device.configID == component1.getId()) {
				for (Switchport swp : device.physicalModule.getSwitchports().values()) {
					if (swp instanceof SimulatorSwitchport && swp.configID == interface1.getId()) {
						return (SimulatorSwitchport) swp;
					}
				}
			}
		}

		throw new LoaderException(String.format("Nepodarilo se najit Device s id=%d a k nemu SimulatorSwichport s id=%d", component1.getId(), interface1.getId()));
	}

	@Override
	public String getDescription() {
		return getClass().getName();
	}

	private void registerID(int id){
		if(idecka.contains(id)){
			Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "V konfiguraku jsou 2 objekty se stejnym id = "+id, null);
		}
		idecka.add(id);
	}

	private void registerName(String name, HwComponentModel model){
		if(names.contains(name)){
			Logger.log(this, Logger.ERROR, LoggingCategory.NETWORK_MODEL_LOAD_SAVE, "V konfiguraku jsou 2 rozhrani na jednom prvku ("+model.getName()+") se stejnym jmenem: "+name, null);
		}
		names.add(name);
	}
}
