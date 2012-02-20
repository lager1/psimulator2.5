package config.AbstractNetwork;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkCounter implements Serializable {

    private int nextId;
    private int lastTelnetPort;
    private int nextMacAddress;
    private Map<HwTypeEnum, Integer> nextNumberMap;

    public NetworkCounter(int nextId, int nextMacAddress) {
        this.nextId = nextId;
        this.lastTelnetPort = 2000;
        this.nextMacAddress = nextMacAddress;
        this.nextNumberMap = new EnumMap<HwTypeEnum, Integer>(HwTypeEnum.class);

    }

    public NetworkCounter() {
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public void setNextNumberMap(Map<HwTypeEnum, Integer> nextNumberMap) {
        this.nextNumberMap = nextNumberMap;
    }

    public int generateNextTelnetPort() {
        int ret = lastTelnetPort;
        this.lastTelnetPort += 1;
        return ret;
    }

    public int getLastTelnetPort() {
        return lastTelnetPort;
    }

    public void setLastTelnetPort(int lastTelnetPort) {
        this.lastTelnetPort = lastTelnetPort;
    }

    public Map<HwTypeEnum, Integer> getNextNumberMap() {
        return nextNumberMap;
    }

    public void setNextMacAddress(int nextMacAddress) {
        this.nextMacAddress = nextMacAddress;
    }

    public void setNextNumberMap(EnumMap<HwTypeEnum, Integer> nextNumberMap) {
        this.nextNumberMap = nextNumberMap;
    }

    public int getNextID() {
        return nextId;
    }

    public int getNextMacAddress() {
        return nextMacAddress;
    }

    public void putIntoNextNumberMap(HwTypeEnum hwType, Integer value) {
        nextNumberMap.put(hwType, value);
    }

    public Integer getFromNextNumberMap(HwTypeEnum hwType) {
        return nextNumberMap.get(hwType);
    }
    // ---------------------------------------------------------------
    // Martin Svihlik nasledujici metody nepotrebuje
}
