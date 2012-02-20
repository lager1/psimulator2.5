package config.AbstractNetwork;

import config.AbstractNetwork.AdditionsSimulator.SimNetworkDevice;
import config.AbstractNetwork.AdditionsUI.UINetworkDevice;
import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkDevice implements Serializable {
    
    private int ID;
    private HwTypeEnum hwType;
    private String name;
    private List<NetworkInterface> interfaces;
    private int telnetPort;
    
    private UINetworkDevice uiAdds;
    private SimNetworkDevice simAdds;
    
    public NetworkDevice(int ID, HwTypeEnum hwType, String name, int x, int y) {
        this.ID = ID;
        this.hwType = hwType;
        this.name = name;
        
        uiAdds = new UINetworkDevice(x, y);
        
    }
    
    public NetworkDevice() {
    }
    
    @XmlAttribute
    @XmlID
    public String getIDAsString() {
        return String.valueOf(ID);
    }
    
    public void setIDAsString(String id) {
        ID = Integer.valueOf(id);
    }
    
    public int getID() {
        return ID;
    }
    
    public HwTypeEnum getHwType() {
        return hwType;
    }
    
    @XmlElement(name = "interface")
    @XmlIDREF
    public List<NetworkInterface> getInterfaces() {
        return interfaces;
    }
    
    public void setInterfaces(List<NetworkInterface> interfaces) {
        this.interfaces = interfaces;
    }
    
    public String getName() {
        return name;
    }
    
    public int getX() {
        return uiAdds.getX();
    }
    
    public int getY() {
        return uiAdds.getY();
    }

    public SimNetworkDevice getSimAdds() {
        return simAdds;
    }

    public void setSimAdds(SimNetworkDevice simAdds) {
        this.simAdds = simAdds;
    }

    public UINetworkDevice getUiAdds() {
        return uiAdds;
    }

    public void setUiAdds(UINetworkDevice uiAdds) {
        this.uiAdds = uiAdds;
    }

    public int getTelnetPort() {
        return telnetPort;
    }

    public void setTelnetPort(int telnetPort) {
        this.telnetPort = telnetPort;
    }

    
    
    // ---------------------------------------------------------------
    // Martin Svihlik nasledujici metody nepotrebuje
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public void setHwType(HwTypeEnum hwType) {
        this.hwType = hwType;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setX(int x) {
        uiAdds.setX(x);
    }
    
    public void setY(int y) {
        uiAdds.setY(y);
    }
}
