package shared.Components;

import java.util.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import shared.Components.simulatorConfig.DeviceSettings;

/**
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 * @author lager1
 */
public final class HwComponentModel implements PositionInterface, NameInterface, AbstractComponentModel {

    /**
     * LinkedHashMap of EthInterfaces that component owns. Key is the
     * ethInterface ID.
     */
    @XmlElement
    private Map<Integer, EthInterfaceModel> interfacesMap;
    // -------------------------------------------------------
    /**
     * Device name.
     */
    private String name;
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
    /**
     * real inteface name
     */
    private String realInterface;

    /**
     * @var Bridge priority
     */
    private Integer bridgePriority;

    /**
     * @var Bridge Message Max age
     */
    private Integer maxAge;

    /**
     * @var Bridge Forward delay
     */
    private Integer forwardDelay;

    /**
     * @var Enabling Spanning tree protocol
     */
    private Boolean stpEnabled;

    /**
     * @var Bridge Hello time
     */
    private Integer helloTime;

    public HwComponentModel(Integer id, HwTypeEnum hwType, String deviceName, ArrayList<EthInterfaceModel> ethInterfaces,
                            int defaultZoomXPos, int defaultZoomYPos) {

        // add values to variables
        this.id = id;
        this.hwType = hwType;
        this.name = deviceName;
        this.defaultZoomXPos = defaultZoomXPos;
        this.defaultZoomYPos = defaultZoomYPos;

        // add interfaces to map
        this.setInterfacesAsList(ethInterfaces);
    }

    public HwComponentModel() {
        this.interfacesMap = new LinkedHashMap<Integer, EthInterfaceModel>();
    }

    @XmlAttribute
    @XmlID
    public String getIDAsString() {
        return String.valueOf(this.id);
    }

    public void setIDAsString(String id) {
        this.id = Integer.valueOf(id);
    }

    /**
     * Gets first available ethInterface, if no available null is renturned
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

    /**
     * Gets interface names as array of Objects
     *
     * @return
     */
    public Object[] getInterfacesNames() {
        Object[] list = new Object[interfacesMap.size()];

        int i = 0;
        for (EthInterfaceModel ei : interfacesMap.values()) {
            list[i] = ei.getName();
            i++;
        }
        return list;
    }

    /**
     * Gets ethInterface with specified ID
     *
     * @param id
     * @return
     */
    public EthInterfaceModel getEthInterface(Integer id) {
        return interfacesMap.get(id);
    }

    /**
     * Gets ethInterface at specified index
     *
     * @param index
     * @return
     */
    public EthInterfaceModel getEthInterfaceAtIndex(int index) {
        List<EthInterfaceModel> list = new ArrayList<EthInterfaceModel>(interfacesMap.values());
        return list.get(index);
    }

    /**
     * Gets ethInterfaces count
     *
     * @return
     */
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
        this.name = name;
    }

    /**
     * Gets name of component
     *
     * @return
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns collection of interfaces
     */
    public Collection<EthInterfaceModel> getEthInterfaces() {
        return interfacesMap.values();
    }


    @XmlTransient // do not store as a map, we need a reference to this object for JAXB, storing as List
    public Map<Integer, EthInterfaceModel> getInterfacesMap() {
        return interfacesMap;
    }


    public void setInterfacesMap(LinkedHashMap<Integer, EthInterfaceModel> interfacesMap) {
        this.interfacesMap = interfacesMap;
    }

    @XmlTransient
    public ArrayList<EthInterfaceModel> getInterfacesAsList() {
        return new ArrayList<EthInterfaceModel>(this.interfacesMap.values());
    }

    public void setInterfacesAsList(ArrayList<EthInterfaceModel> ethInterfaces) {
        this.interfacesMap = new LinkedHashMap<Integer, EthInterfaceModel>();

        for (EthInterfaceModel eth : ethInterfaces) {
            interfacesMap.put(eth.getId(), eth);
        }

    }

    /**
     * Gets bridge priority (STP)
     *
     * @return Bridge priority
     */
    public Integer getBridgePriority()
    {
        return bridgePriority;
    }

    /**
     * Sets bridge priority (STP)
     *
     * @param bridgePriority
     */
    public void setBridgePriority(Integer bridgePriority)
    {
        this.bridgePriority = bridgePriority;
    }

    /**
     * Get bridge Message max age
     *
     * @return Bridge Max age
     */
    public Integer getMaxAge()
    {
        return maxAge;
    }

    /**
     * Sets bridge Maximal message age
     *
     * @param maxAge
     */
    public void setMaxAge(Integer maxAge)
    {
        this.maxAge = maxAge;
    }

    /**
     * Gets bridge forward delay
     * @return Bridge forward delay
     */
    public Integer getForwardDelay()
    {
        return forwardDelay;
    }

    /**
     * Sets Bridge forward delay
     *
     * @param forwardDelay
     */
    public void setForwardDelay(Integer forwardDelay)
    {
        this.forwardDelay = forwardDelay;
    }

    /**
     * Gets STP protocol status
     *
     * @return true if STP enabled otherwise false
     */
    public Boolean getStpEnabled()
    {
        return stpEnabled;
    }

    /**
     * Sets STP protocol status
     *
     * @param stpEnabled
     */
    public void setStpEnabled(Boolean stpEnabled)
    {
        this.stpEnabled = stpEnabled;
    }

    /**
     *  Gets bridge hello time
     *
     * @return bridge hello time
     */
    public Integer getHelloTime()
    {
        return helloTime;
    }

    /**
     * Sets Bridge hello time
     *
     * @param helloTime
     */
    public void setHelloTime(Integer helloTime)
    {
        this.helloTime = helloTime;
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

    /**
     * Removes interface with specified ID
     *
     * @param eth interface object object
     */
    public void removeInterface(EthInterfaceModel eth) {
        interfacesMap.remove(eth.getId());

        // unbind eth interface and component
        eth.setHwComponent(null);
    }

    /**
     * Adds interface in parameter to interfaces map
     *
     * @param eth
     */
    public void addInterface(EthInterfaceModel eth) {
        // bind eth interface and component together
        eth.setHwComponent(this);

        interfacesMap.put(eth.getId(), eth);
    }

    /**
     * Returns minimum interface count for component
     *
     * @return
     */
    public int getMinInterfaceCount() {
        switch (hwType) {
            case CISCO_ROUTER:
            case CISCO_SWITCH:
            case LINUX_ROUTER:
            case LINUX_SWITCH:
                return 2;
            case END_DEVICE_NOTEBOOK:
            case END_DEVICE_PC:
            case END_DEVICE_WORKSTATION:
            case REAL_PC:
                return 1;
            default:
                return 0;
        }
    }
}
>>>>>>> 16b526d (adapt new repo structure)
