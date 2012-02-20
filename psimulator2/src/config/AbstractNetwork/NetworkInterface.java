package config.AbstractNetwork;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkInterface implements Serializable{
    
    
    private int ID;
    
    
    private NetworkDevice device;
    
    private String interfaceName;
    private String ipAddress;
    private String macAddress;

    public NetworkInterface(int ID, NetworkDevice device, String interfaceName, String ipAddress, String macAddress) {
        this.ID = ID;
        this.interfaceName = interfaceName;
        this.device = device;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }

    public NetworkInterface() {
    }
    
    @XmlAttribute @XmlID
    public String getIDAsString(){
        return String.valueOf(ID);
    }
    
    public void setIDAsString(String id){
        ID = Integer.valueOf(id);
    }

    public int getID() {
        return ID;
    }

    @XmlAttribute @XmlIDREF
    public NetworkDevice getDevice() {
        return device;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    // ---------------------------------------------------------------
    // Martin Svihlik nasledujici metody nepotrebuje
    
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public void setDevice(NetworkDevice device) {
        this.device = device;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}
