package dataStructures;

import java.nio.ByteBuffer;

/**
 * Class representing STP bridge identifier.
 *
 * @author Peter Bábics <babicpe1@fit.cvut.cz>
 */
public class BridgeIdentifier implements Comparable {
    public MacAddress macAddress;
    public Short priority;

    public BridgeIdentifier(MacAddress macAddress, short priority) {
        this.macAddress = macAddress;
        this.priority = priority;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public Short getPriority() {
        return priority;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }

    public long toLong() {
        return (getPriority() << 48) + ByteBuffer.wrap(macAddress.getByteArray()).getLong();
    }

    @Override
    public String toString() {
        return ((int) priority & 0xffff) + " / " + macAddress.toString();
    }


    public Boolean isLessOrEqual(BridgeIdentifier identifier) {
        if (identifier == null)
            return false;
        if (((int)this.priority & 0xffff) > ((int)identifier.priority & 0xffff)) // Has higher priority
            return true;
        if  (((int)this.priority & 0xffff) < ((int)identifier.priority & 0xffff)) // Has lower priority
            return false;
        return this.macAddress.isLessOrEqualThan(identifier.macAddress); // Has lower numeric mac address value
    }

    public boolean equals(BridgeIdentifier obj) {
        if (obj == null)
            return false;
        return obj.priority == this.priority &&
                    obj.macAddress == this.macAddress;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null)
            return -1;
        BridgeIdentifier o = (BridgeIdentifier) obj;
        if (o == null)
            return -1;
        if (this.priority > o.priority)
            return -1;
        else if (this.priority < o.priority)
            return 1;
        return this.macAddress.compareTo(o.macAddress);
    }
}
