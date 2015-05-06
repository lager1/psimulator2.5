package applications;

import config.configFiles.InterfacesFile;
import dataStructures.configurations.InterfaceConfiguration;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import filesystem.FileSystem;

import java.util.ArrayList;

import logging.Logger;
import logging.LoggingCategory;
import networkModule.IpNetworkModule;
import networkModule.L3.NetworkInterface;
import psimulator2.Psimulator;
import shell.apps.CommandShell.CommandShell;

/*
 * @author Michal Horacek
 */
public class Networking extends Application {

    private final FileSystem fs;
    private final CommandShell shell;
    private final IpNetworkModule ipNetMod;

    public Networking(Device device, CommandShell shell) {
        super("networking", device);
        this.fs = device.getFilesystem();
        this.shell = shell;
        ipNetMod = (IpNetworkModule) device.getNetworkModule();
    }

    @Override
    public void doMyWork() {
        InterfacesFile ifaceFile = ipNetMod.applicationLayer.getInterfacesFile();

        if (!fs.exists(ifaceFile.getFilePath())) {
            ifaceFile.createFile();
        }

        ArrayList<InterfaceConfiguration> configs = ifaceFile.getConfig();
        for (InterfaceConfiguration config : configs) {
            setInterface(config);
        }

        // Probehla zmena udaju o interface rozhranich, zmeny se zapisi do XML souboru
        Psimulator psimulator = Psimulator.getPsimulator();
        psimulator.saveSimulatorToConfigFile(psimulator.lastConfigFile);
    }

    private IPwithNetmask validateAddress(InterfaceConfiguration config) {
        IPwithNetmask addr = null;

        if (config.address == null || IpAddress.isForbiddenIP(config.address)) {
            shell.printLine("Invalid ip address on interface " + config.ifaceName);
            return null;
        }

        if (config.mask == null) {
            shell.printLine("Invalid netmask on interface " + config.ifaceName);
            return null;
        } else {
            addr = new IPwithNetmask(config.address, config.mask);
        }

        // Adresa je cislem site nebo broadcast
        if (addr.isNetworkNumber() || addr.isBroadcast()) {
            shell.printLine("Invalid ip address on interface " + config.ifaceName);
            return null;
        }

        return addr;
    }

    private void setInterface(InterfaceConfiguration config) {
        NetworkInterface iface = ipNetMod.ipLayer.getNetworkIntefaceIgnoreCase(config.ifaceName);
        if (iface == null) {
            shell.printLine("No such device: " + config.ifaceName);
            return;
        }

        if (config.type.equalsIgnoreCase("static")) {
            IPwithNetmask addr = validateAddress(config);
            if (addr == null) {
                return;
            }

            // vse v poradku, nastavim adresu
            ipNetMod.ipLayer.changeIpAddressOnInterface(iface, addr);
            updateRoutingTable(iface, config);
            iface.isDhcp = false;
        } else if (config.type.equalsIgnoreCase("dhcp") &&
                iface.ethernetInterface.isConnected()) {
            ipNetMod.applicationLayer.getDhcpManager().lease(iface, shell);
        } else {
        }
    }

    private void updateRoutingTable(NetworkInterface iface, InterfaceConfiguration config) {
        ipNetMod.ipLayer.routingTable.flushRecords(iface);

        if (iface.getIpAddress() != null) {
            ipNetMod.ipLayer.routingTable.addRecord(iface.getIpAddress().getNetworkNumber(), iface);
        }

        if (config.gateway != null) {
            IPwithNetmask destination = new IPwithNetmask("0.0.0.0");
            ipNetMod.ipLayer.routingTable.addRecord(destination, config.gateway, iface);
        }
    }

    @Override
    protected void atStart() {
    }

    @Override
    protected void atExit() {
    }

    @Override
    protected void atKill() {
    }

    private void log(int logLevel, String msg, Object obj) {
        Logger.log(this, logLevel, LoggingCategory.DHCP, msg, obj);
    }

    @Override
    public String getDescription() {
        return device.getName() + ": networking service";
    }
}
