package config.Network;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class NetworkFacade {
    
    private NetworkModel networkModel; 
    private NetworkComponentsFactory networkComponentsFactory;
    
    public NetworkFacade(){
        networkComponentsFactory = new NetworkComponentsFactory();
    }

    public NetworkModel getNetworkModel() {
        return networkModel;
    }

    public void setNetworkModel(NetworkModel network) {
        this.networkModel = network;
    }
    
    public NetworkModel createNetworkModel(){
        this.networkModel = networkComponentsFactory.createEmptyNetworkModel();
        return networkModel;
    }
    
    public HwComponentModel createHwComponentModel(HwTypeEnum hwType, int interfacesCount, int defaultZoomXPos, int defaultZoomYPos){
        HwComponentModel hwComponentModel = networkComponentsFactory.createHwComponent(hwType, interfacesCount, defaultZoomXPos, defaultZoomYPos); 
        return hwComponentModel;
    }
    
    public CableModel createCableModel(HwTypeEnum hwType, HwComponentModel component1, HwComponentModel component2, EthInterfaceModel interface1, EthInterfaceModel interface2){
        CableModel cableModel = networkComponentsFactory.createCable(hwType, component1, component2, interface1, interface2);
        return cableModel;
    }
    
    // -----------------------------------------------
    
    public NetworkCounterModel getNetworkCounterModel(){
        return networkModel.getNetworkCounterModel();
    }
    
    public int getCablesCount(){
        return networkModel.getCablesCount();
    }
    
    public int getHwComponentsCount(){
        return networkModel.getHwComponentsCount();
    }
    
    public Collection<CableModel> getCables(){
        return networkModel.getCables();
    } 
    
    /**
     * Adds calbe to proper bundle of cables, eth interfaces
     * @param cable 
     */
    public void addCable(CableModel cable) {
        cable.getInterface1().setCable(cable);
        cable.getInterface2().setCable(cable);
        
        // add cable to hash map
        networkModel.addCable(cable);

        // set timestamp of edit
        editHappend();
    }
    
    public void addCables(List<CableModel> cableList) {
        for (CableModel c : cableList) {
            addCable(c);
        }
    }
    
    /**
     * removes cable from graph
     *
     * @param cable
     */
    public void removeCable(CableModel cable) {
        cable.getInterface1().removeCable();
        cable.getInterface2().removeCable();

        // remove cable from hash map
        networkModel.removeCable(cable);
        
        // set timestamp of edit
        editHappend();
    }
    
    public void removeCables(List<CableModel> cableList) {
        for (Iterator<CableModel> it = cableList.iterator(); it.hasNext();) {
            removeCable(it.next());
        }
    }
    
    public Collection<HwComponentModel> getHwComponents() {
        return networkModel.getHwComponents();
    }
    
    
    public void addHwComponent(HwComponentModel component) {
        networkModel.addHwComponent(component);
        
        // set timestamp of edit
        editHappend();
    }
    
    public void addHwComponents(List<HwComponentModel> componentList) {
        for (HwComponentModel component : componentList) {
            addHwComponent(component);
        }
    }
    
    public void removeHwComponent(HwComponentModel component) {
        networkModel.removeHwComponent(component);

        // set timestamp of edit
        editHappend();
    }
    
    public void removeHwComponents(List<HwComponentModel> componentList) {
       for(HwComponentModel component : componentList){
            networkModel.removeHwComponent(component);
        }

        // set timestamp of edit
        editHappend();
    }
    
    public HwComponentModel getHwComponentModelById(int id){
        return networkModel.getHwComponentModelById(id);
    }
    
    public CableModel getCableModelById(int id){
        return networkModel.getCableModelById(id);
    }
    
    // TODO
    public void editHappend(){
        //
        networkModel.setLastEditTimestamp(System.currentTimeMillis());        
    }
}
