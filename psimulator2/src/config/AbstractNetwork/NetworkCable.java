package config.AbstractNetwork;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkCable implements Serializable{
    private int ID;
    
    private HwTypeEnum hwType;
    
    private NetworkInterface interface1;
    private NetworkInterface interface2;
    
    private int delay;

    public NetworkCable(int ID, NetworkInterface interface1, NetworkInterface interface2, HwTypeEnum hwType, int delay) {
        this.ID = ID;
        this.interface1 = interface1;
        this.interface2 = interface2;
        this.hwType = hwType;
        this.delay = delay;
    }

    public NetworkCable() {
    }
    
    

    public int getID() {
        return ID;
    }

    public int getDelay() {
        return delay;
    }

    @XmlAttribute @XmlIDREF
    public NetworkInterface getInterface1() {
        return interface1;
    }

    @XmlAttribute @XmlIDREF
    public NetworkInterface getInterface2() {
        return interface2;
    }

    public HwTypeEnum getHwType() {
        return hwType;
    }

    // ---------------------------------------------------------------
    // Martin Svihlik nasledujici metody nepotrebuje
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    public void setInterface1(NetworkInterface interface1) {
        this.interface1 = interface1;
    }
    
    public void setInterface2(NetworkInterface interface2) {
        this.interface2 = interface2;
    }
    
    public void setHwType(HwTypeEnum hwType) {
        this.hwType = hwType;
    }
}
