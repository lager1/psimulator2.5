package config.Network;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkCounterModel implements Serializable{

    private int nextId;
    private int nextMacAddress;
    private Map<HwTypeEnum, Integer> nextNumberMap;

    public NetworkCounterModel(int nextId, int nextMacAddress, Map<HwTypeEnum, Integer> nextNumberMap) {
        this.nextId = nextId;
        this.nextMacAddress = nextMacAddress;
        this.nextNumberMap = nextNumberMap;
    }

    public NetworkCounterModel() {
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    
    
    public int getNextId() {
        return nextId++;
    }
    
    public int getCurrentId(){
        return nextId;
    }

    public void increaseMacAddressCounter() {
        nextMacAddress++;
    }
    
    public int getCurrentMacAddress(){
        return nextMacAddress;
    }
    
    public Integer getFromNumberMap(HwTypeEnum key){
        return nextNumberMap.get(key);
    }
    
    public void putToNumberMap(HwTypeEnum key, Integer value){
        nextNumberMap.put(key, value);
    }

    public int getNextMacAddress() {
        return nextMacAddress;
    }

    public void setNextMacAddress(int nextMacAddress) {
        this.nextMacAddress = nextMacAddress;
    }

    public Map<HwTypeEnum, Integer> getNextNumberMap() {
        return nextNumberMap;
    }

    public void setNextNumberMap(Map<HwTypeEnum, Integer> nextNumberMap) {
        this.nextNumberMap = nextNumberMap;
    }
    
    
    
}
