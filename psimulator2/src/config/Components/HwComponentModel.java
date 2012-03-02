package config.Components;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public final class HwComponentModel implements PositionInterface, NameInterface, AbstractComponentModel {

    /**
     * LinkedHashMap of EthInterfaces that component owns. Key is the
     * ethInterface ID.
     */
    
    private Map<Integer, EthInterfaceModel> interfacesMap;
    // -------------------------------------------------------
    /**
     * Device name.
     */
    private String deviceName;
    /**
     * X position of component in Default zoom
     */
    private int defaultZoomXPos;
    /**
     * Y position of component in Default zoom
     */
    private int defaultZoomYPos;
    /**
     * Type of component
     */
    private HwTypeEnum hwType;
    /**
     * Id of component
     */
    private Integer id;
	
	/**
	 * Nastaveni pocitace pro potreby simulatoru.
	 */
	private DeviceSettings devSettings;

    public HwComponentModel(Integer id, HwTypeEnum hwType, String deviceName, List<EthInterfaceModel> ethInterfaces, 
			int defaultZoomXPos, int defaultZoomYPos, DeviceSettings devSettings) {

        // add values to variables
        this.id = id;
        this.hwType = hwType;
        this.deviceName = deviceName;
        this.defaultZoomXPos = defaultZoomXPos;
        this.defaultZoomYPos = defaultZoomYPos;
		this.devSettings = devSettings;

        // add interfaces to map
        this.setInterfacesAsList(ethInterfaces);
    }

    public HwComponentModel() {
        this.interfacesMap = new LinkedHashMap<Integer, EthInterfaceModel>();
    }
    
    @XmlAttribute @XmlID
    public String getIDAsString(){
        return String.valueOf(this.id);
    }
    
    public void setIDAsString(String id){
        this.id = Integer.valueOf(id);
    }

    /**
     * Gets first avaiable ethInterface, if no avaiable null is renturned
     *
     * @return
     */
    public EthInterfaceModel getFirstFreeInterface() {
        for (EthInterfaceModel ei : interfacesMap.values()) {
            if (!ei.hasCable()) {
                return ei;
            }
        }
        return null;
    }

    /**
     * finds out whether component has any free EthInterface
     *
     * @return
     */
    public boolean hasFreeInterace() {
        for (EthInterfaceModel ei : interfacesMap.values()) {
            if (!ei.hasCable()) {
                return true;
            }
        }
        return false;
    }

    public Object[] getInterfacesNames() {
        Object[] list = new Object[interfacesMap.size()];

        int i = 0;
        for (EthInterfaceModel ei : interfacesMap.values()) {
            list[i] = ei.getName();
            i++;
        }
        return list;
    }

    public EthInterfaceModel getEthInterface(Integer id) {
        return interfacesMap.get(id);
    }

    public EthInterfaceModel getEthInterfaceAtIndex(int index) {
        List<EthInterfaceModel> list = new ArrayList<EthInterfaceModel>(interfacesMap.values());
        return list.get(index);
    }

    public int getEthInterfaceCount() {
        return interfacesMap.size();
    }

    /**
     * Gets X position of component in default zoom
     *
     * @return
     */
    @Override
    public int getDefaultZoomXPos() {
        return defaultZoomXPos;
    }

    /**
     * Gets Y position of component in default zoom
     *
     * @return
     */
    @Override
    public int getDefaultZoomYPos() {
        return defaultZoomYPos;
    }

    /**
     * Sets X position of component in default zoom
     *
     * @param defaultZoomXPos
     */
    @Override
    public void setDefaultZoomXPos(int defaultZoomXPos) {
        this.defaultZoomXPos = defaultZoomXPos;
    }

    /**
     * Sets Y position of component in default zoom
     *
     * @param defaultZoomYPos
     */
    @Override
    public void setDefaultZoomYPos(int defaultZoomYPos) {
        this.defaultZoomYPos = defaultZoomYPos;
    }

    /**
     * Sets name of component
     *
     * @param name
     */
    @Override
    public void setName(String name) {
        this.deviceName = name;
    }

    /**
     * Gets name of component
     *
     * @return
     */
    @Override
    public String getName() {
        return this.deviceName;
    }

    /**
     * Returns collection of interfaces
     */
    public Collection getEthInterfaces() {
        return interfacesMap.values();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @XmlTransient // do not store as a map, we need a reference to this object for JAXB, storing as List
    public Map<Integer, EthInterfaceModel> getInterfacesMap() {
        return interfacesMap;
    }
    
    public void setInterfacesMap(Map<Integer, EthInterfaceModel> interfacesMap) {
        this.interfacesMap = interfacesMap;
    }

    @XmlElement(name = "interface")
    public List<EthInterfaceModel> getInterfacesAsList() {
        return new ArrayList<EthInterfaceModel>(this.interfacesMap.values());
    }

    public void setInterfacesAsList(List<EthInterfaceModel> ethInterfaces) {

        this.interfacesMap = new LinkedHashMap<Integer, EthInterfaceModel>();

        for (EthInterfaceModel eth : ethInterfaces) {
            interfacesMap.put(eth.getId(), eth);
        }

    }

    @Override
    public HwTypeEnum getHwType() {
        return hwType;
    }

    @Override
    public void setHwType(HwTypeEnum hwType) {
        this.hwType = hwType;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

	public DeviceSettings getDevSettings() {
		return devSettings;
	}

	public void setDevSettings(DeviceSettings devSettings) {
		this.devSettings = devSettings;
	}
    
	
    
}
