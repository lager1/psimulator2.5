

package config.AbstractNetwork.AdditionsSimulator;

import networkModule.L3.RoutingTable;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz> Lukáš <lukasma1@fit.cvut.cz>
 */
public class SimNetworkDevice {

    private String filesystemFolder;
    private RoutingTable table;
    
    public SimNetworkDevice() {
    }

    public String getFilesystemFolder() {
        return filesystemFolder;
    }

    public void setFilesystemFolder(String filesystemFolder) {
        this.filesystemFolder = filesystemFolder;
    }

    public RoutingTable getTable() {
        return table;
    }

    public void setTable(RoutingTable table) {
        this.table = table;
    }
    
    

    
}
