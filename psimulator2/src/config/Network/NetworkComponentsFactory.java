package config.Network;

import java.util.*;


/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkComponentsFactory {

    public NetworkComponentsFactory() {
    }

    public NetworkModel createEmptyNetworkModel() {
        LinkedHashMap<Integer, HwComponentModel> componentsMap = new LinkedHashMap<Integer, HwComponentModel>();
        LinkedHashMap<Integer, CableModel> cablesMap = new LinkedHashMap<Integer, CableModel>();
        long lastEditTimestamp = 0L;
        Integer id = new Integer(-1);
        NetworkCounterModel networkCounterModel = createEmptyNetworkCounter();

        NetworkModel networkModel = new NetworkModel(componentsMap, cablesMap, lastEditTimestamp, id, networkCounterModel);
        
        return networkModel;
    }
    
    public NetworkCounterModel createEmptyNetworkCounter(){
        
        int nextId = 0;
        int nextMacAddress = 0;
        Map<HwTypeEnum, Integer> nextNumberMap = new EnumMap<HwTypeEnum, Integer>(HwTypeEnum.class);
        
        for (HwTypeEnum hwTypeEnum : HwTypeEnum.values()) {
            nextNumberMap.put(hwTypeEnum, new Integer(0));
        }
        
        NetworkCounterModel networkCounterModel = new NetworkCounterModel(nextId, nextMacAddress, nextNumberMap);
        
        return networkCounterModel;
    }

    public CableModel createCable(HwTypeEnum hwType, HwComponentModel component1, HwComponentModel component2, EthInterfaceModel interface1, EthInterfaceModel interface2) {
        Integer id = GeneratorSingleton.getInstance().getNextId();

        int delay;

        // set delay according to type
        switch (hwType) {
            case CABLE_ETHERNET:
                delay = 10;
                break;
            case CABLE_OPTIC:
            default:
                delay = 2;
                break;
        }

        CableModel cable = new CableModel(id, hwType, component1, component2, interface1, interface2, delay);

        return cable;
    }

    public HwComponentModel createHwComponent(HwTypeEnum hwType, int interfacesCount, int defaultZoomXPos, int defaultZoomYPos) {
        // generate ID for HwComponent
        Integer id = GeneratorSingleton.getInstance().getNextId();

        // generate device name for HwComponent
        String deviceName = GeneratorSingleton.getInstance().getNextDeviceName(hwType);

        // generate names for interface
        List<String> ethInterfaceNames = GeneratorSingleton.getInstance().getInterfaceNames(hwType, interfacesCount);

        // create interfaces
        List<EthInterfaceModel> ethInterfaces = new ArrayList<EthInterfaceModel>();

        for (int i = 0; i < interfacesCount; i++) {
            ethInterfaces.add(createEthInterface(ethInterfaceNames.get(i), hwType));
        }

        // create hw component
        HwComponentModel hwComponent = new HwComponentModel(id, hwType, deviceName, ethInterfaces, defaultZoomXPos, defaultZoomYPos);

        // set HwComponent to ethInterfaces
        for (EthInterfaceModel ethInterface : ethInterfaces) {
            ethInterface.setHwComponent(hwComponent);
        }

        return hwComponent;
    }

    private EthInterfaceModel createEthInterface(String interfaceName, HwTypeEnum hwType) {
        String macAddress;
        String ipAddress;

        // do not generate MAC for switches and real pc
        switch (hwType) {
            case LINUX_SWITCH:
            case CISCO_SWITCH:
            case REAL_PC:
                macAddress = "";
                ipAddress = "";
                break;
            default:
                macAddress = GeneratorSingleton.getInstance().getNextMacAddress();
                ipAddress = "";
                break;
        }

        Integer interfaceId = GeneratorSingleton.getInstance().getNextId();
        HwComponentModel hwComponentModel = null;
        CableModel cable = null;

        EthInterfaceModel ethInterfaceModel = new EthInterfaceModel(interfaceId, hwType, hwComponentModel, cable, ipAddress, macAddress, interfaceName);

        return ethInterfaceModel;
    }
}
