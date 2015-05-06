package networkModule.L7;

import applications.dhcp.DhcpClient;
import config.configFiles.HostsFile;
import config.configFiles.InterfacesFile;
import config.configFiles.ResolvConfFile;
import filesystem.FileSystem;
import logging.Loggable;
import networkModule.IpNetworkModule;
import networkModule.L3.IPLayer;
import utils.Utilities;

/**
 * @author Michal Horacek
 */
public class ApplicationLayer implements Loggable {

    private final IpNetworkModule netMod;
    private final FileSystem fs;

    private DhcpClient dhcpManager;

    private final InterfacesFile ifaceFile;
    private final ResolvConfFile resolvFile;
    private final HostsFile hostsFile;

    public ApplicationLayer(IpNetworkModule netMod) {
        this.netMod = netMod;
        this.fs = netMod.getDevice().getFilesystem();

        this.ifaceFile = new InterfacesFile(netMod.ipLayer);
        this.resolvFile = new ResolvConfFile(fs);
        this.hostsFile = new HostsFile(fs);
    }

    public IpNetworkModule getNetMod() {
        return netMod;
    }

    public IPLayer getIpLayer() {
        return netMod.ipLayer;
    }

    public InterfacesFile getInterfacesFile() {
        return this.ifaceFile;
    }

    public HostsFile getHostsFile() {
        return this.hostsFile;
    }

    public ResolvConfFile getResolvFile() {
        return this.resolvFile;
    }

    public DhcpClient getDhcpManager() {
        return this.dhcpManager;
    }

    public void startServices() {

        createConfigFiles();

        // dhcp-client
        dhcpManager = new DhcpClient(netMod.getDevice());
        dhcpManager.initInterfaces();
        dhcpManager.start();
    }

    private void createConfigFiles() {
        // updating interfaces file on every simulator startup to add possible 
        // changes done in frontend
        ifaceFile.createFile();

        if (!fs.exists(hostsFile.getFilePath())) {
            hostsFile.createFile();
        }

        if (!fs.exists(resolvFile.getFilePath())) {
            resolvFile.createFile();
        }
    }

    @Override
    public String getDescription() {
        return Utilities.alignFromRight(netMod.getDevice().getName(), Utilities.deviceNameAlign) + "AppLayer";
    }
}
