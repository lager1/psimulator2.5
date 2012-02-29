package config.Components;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class EthInterfaceModel implements NameInterface,AbstractComponentModel {
    
    /**
     * Component that this interface belongs to
     */
    private HwComponentModel hwComponent;
    /**
     * Cable that is connected to this interface
     */
    private CableModel cable;
    
    // -------------------------------------------------------
    /**
     * Name.
     */
    private String interfaceName;
    /**
     * Ip address of this eth interface
     */
    private String ipAddress;
    /**
     * Mac address of this eth interface
     */
    private String macAddress;
    
        /**
     * Type of component
     */
    private HwTypeEnum hwType;
    /**
     * Id of component
     */
    private Integer id;


    public EthInterfaceModel(Integer id, HwTypeEnum hwType, HwComponentModel hwComponent, CableModel cable, 
            String ipAddress, String macAddress, String interfaceName) {
        
        // assign variables
        this.id = id;
        this.hwType=hwType;
        this.hwComponent = hwComponent;
        this.cable = cable;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.interfaceName = interfaceName;
    }

    public EthInterfaceModel() {
    }
    
    @XmlAttribute @XmlID
    public String getIDAsString(){
        return String.valueOf(this.id);
    }
    
    public void setIDAsString(String id){
        this.id = Integer.valueOf(id);
    }

    /**
     * Returns cable that is connected to interface
     * @return 
     */
    @XmlIDREF
    public CableModel getCable() {
        return cable;
    }

    /**
     * Sets cable to interface
     * @param cable 
     */
    public void setCable(CableModel cable) {
        this.cable = cable;
    }
    
    /**
     * Removes cable from interface
     */
    public void removeCable(){
        this.cable = null;
    }
 
    /**
     * Finds out if there is cable connected. 
     * @return true if it is, false if it isn't
     */ 
    public boolean hasCable(){
        if(cable == null){
            return false;
        }else{
            return true;
        }
    }
    
    /**
     * Gets hwComponent that this interface belong to
     * @return 
     */
    @XmlAttribute @XmlIDREF
    public HwComponentModel getHwComponent() {
        return hwComponent;
    }
    
    /**
     * Sets hw component to this interface
     * @param hwComponent 
     */
    public void setHwComponent(HwComponentModel hwComponent){
        this.hwComponent = hwComponent;
    }
    
    /**
     * Gets ip address
     * @return 
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets ip address
     * @param ipAddress 
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets mac address
     * @return 
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets mac address
     * @param macAddress 
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Returns name of interface
     * @return 
     */
    @Override
    public String getName() {
        return interfaceName;
    }

    @Override
    public void setName(String name) {
        this.interfaceName = name;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
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

    
}
