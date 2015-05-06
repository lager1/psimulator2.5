package applications.dhcp;

import applications.Application;
import config.configFiles.DhcpServerLeaseFile;
import config.configFiles.DhcpdConfFile;
import dataStructures.MacAddress;
import dataStructures.PacketItem;
import dataStructures.configurations.DhcpServerConfiguration;
import dataStructures.configurations.DhcpSubnetConfiguration;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.IpPacket;
import dataStructures.packets.UdpPacket;
import dataStructures.packets.dhcp.DhcpPacket;
import dataStructures.packets.dhcp.DhcpPacketType;
import device.Device;
import filesystem.FileSystem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetLayer;
import networkModule.L3.NetworkInterface;
import networkModule.SwitchNetworkModule;

public class DhcpServer extends Application {

    private final static int ttl = 64;    // ttl, se kterym se budoui odesilat pakety
    public final static int SERVER_PORT = 67;
    public final static int CLIENT_PORT = 68;
    /**
     * Konfigurace.
     */
    private final EthernetLayer ethLayer;
    private DhcpServerConfiguration config;
    private final DhcpServerLeaseFile leaseFile;
    // Addresses already leased
    private Set<IpAddress> leased;
    // Addresses reserved but not yet leased
    private HashMap<MacAddress, IPwithNetmask> reserved;
    // Addresses still available for lease
    private HashMap<IPwithNetmask, LinkedList<IPwithNetmask>> addresses;
    private final DhcpdConfFile dhcpdConf;
    private final FileSystem fs;

    public DhcpServer(Device device) {
        super("dhcpd", device);    // jmeno jako u me na linuxu
        port = SERVER_PORT;
        ethLayer = ((SwitchNetworkModule) device.getNetworkModule()).ethernetLayer;
        leaseFile = new DhcpServerLeaseFile(device.getFilesystem());
        dhcpdConf = new DhcpdConfFile(device.getFilesystem());
        addresses = new HashMap<>();
        fs = device.getFilesystem();
    }

    @Override
    public void doMyWork() {
        while (!buffer.isEmpty()) {
            handlePacket(buffer.remove(0));
        }
    }

    private void handleDiscover(DhcpPacket recDhcp, NetworkInterface iface) {
        IPwithNetmask adrm = getNextAddress(iface.getIpAddress());

        // Nebyla nalezena pouzitelna volna adresa, jeste se zkusi refresh pridelitelnych adres
        if (adrm == null) {
            refreshAddresses();
            adrm = getNextAddress(adrm);
            if (adrm == null) {
                return;
            }
        }
        reserved.put(recDhcp.clientMac, adrm);
        sendDhcpPacket(DhcpPacketType.OFFER, iface, recDhcp, adrm);
    }

    private void handleRequest(DhcpPacket recDhcp, NetworkInterface iface) {

        // Pokud byl REQUEST smerovany na jiny DHCP server, neodpovidam na nej
        if (recDhcp.serverIdentifier != null
                && !recDhcp.serverIdentifier.equals(iface.getIpAddress().getIp())) {
            return;
        }

        // Kontrola, ze je pridelovana adresa, ktera na tomto serveru byla zadana
        if (!reserved.containsKey(recDhcp.clientMac)) {
            return;
        }

        IPwithNetmask adrm = reserved.get(recDhcp.clientMac);
        HashMap<String, String> options = getOptions(adrm);
        reserved.remove(recDhcp.clientMac);
        sendDhcpPacket(DhcpPacketType.ACK, iface, recDhcp, adrm, options);
    }

    private synchronized void handlePacket(PacketItem item) {
        NetworkInterface iface = item.iface;
        IpPacket recIp = item.packet;
        UdpPacket recUdp;
        DhcpPacket recDhcp;

        // najednou nactu vsechny pakety, kdyby neco bylo null nebo neslo pretypovat,
        // hodilo by to vyjimku - v tom pripade koncim
        try {
            recUdp = (UdpPacket) recIp.data;
            recDhcp = (DhcpPacket) recUdp.getData();
            recDhcp.getSize();    // abych si overil, ze to neni null
        } catch (Exception ex) {
            log(Logger.INFO, "DHCP serveru prisel spatnej paket.", recIp);
            return;
        }
        //resim jednotlivy pripady:
        switch (recDhcp.type) {
            case DISCOVER:
                handleDiscover(recDhcp, iface);
                break;
            case REQUEST:
                handleRequest(recDhcp, iface);
                break;
            default:
                System.out.println("Prisel paket, co neni DISCOVER ani REQUEST");
        }
    }

    private void sendDhcpPacket(DhcpPacketType replyType, NetworkInterface iface, DhcpPacket recDhcp,
            IPwithNetmask adrm, HashMap<String, String> options) {
        // nactu, co mu poslu:
        IpAddress serverAddress = iface.getIpAddress().getIp();
        // sestavim pakety:
        DhcpPacket replyDhcp = new DhcpPacket(replyType, recDhcp.transaction_id, serverAddress,
                adrm, adrm.getBroadcast(), recDhcp.clientMac, options);

        if (replyType == DhcpPacketType.ACK) {
            leaseFile.appendLease(replyDhcp, iface);
        }

        UdpPacket replyUdp = new UdpPacket(SERVER_PORT, CLIENT_PORT, replyDhcp);
        IpPacket replyIp = new IpPacket(serverAddress, new IpAddress("255.255.255.255"), ttl, replyUdp);
        // nakonec to poslu pomoci ethernetovy vrstvy:

        ethLayer.sendPacket(replyIp, iface.ethernetInterface, recDhcp.clientMac);
    }

    private void sendDhcpPacket(DhcpPacketType replyType, NetworkInterface iface, DhcpPacket recDhcp, IPwithNetmask adrm) {
        sendDhcpPacket(replyType, iface, recDhcp, adrm, null);
    }

    private HashMap<String, String> getOptions(IPwithNetmask adrm) {
        HashMap<String, String> options = new HashMap<>(config.options);
        DhcpSubnetConfiguration subnetConfig = config.getSubnetConfiguration(adrm);

        if (subnetConfig != null) {
            for (String key : subnetConfig.options.keySet()) {
                options.put(key, subnetConfig.options.get(key));
            }
        }

        setLeaseTime(options);
        return options;
    }

    private void setLeaseTime(HashMap<String, String> options) {
        if (config.defaultLeaseTime == null) {
            config.defaultLeaseTime = "7200";
        }

        if (!options.containsKey("lease-time")) {
            options.put("lease-time", config.defaultLeaseTime);
        }

        try {
            if (Integer.parseInt(options.get("lease-time"))
                    > Integer.parseInt(config.maxLeaseTime)) {
                options.put("lease-time", config.maxLeaseTime);
            }
        } catch (NumberFormatException ex) {
            options.put("lease-time", "7200");
        }

        options.put("lease-start", formatCurrentDate());
    }

    private String formatCurrentDate() {
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");

        return df.format(now);
    }

    private IPwithNetmask getNextAddress(IPwithNetmask received) {
        IPwithNetmask ret = null;
        LinkedList<IPwithNetmask> q;

        for (IPwithNetmask key : addresses.keySet()) {
            if (key.equals(received.getNetworkNumber())) {
                q = addresses.get(key);

                while (!q.isEmpty()) {
                    ret = q.remove();
                    if (!leased.contains(ret.getIp())) {
                        break;
                    }
                }
                break;
            }
        }

        return ret;
    }

    @Override
    protected void atStart() {
        if (!fs.exists(dhcpdConf.getFilePath())) {
            dhcpdConf.createFile();
            config = new DhcpServerConfiguration();
        } else {
            config = dhcpdConf.getConfiguration();
        }

        reserved = new HashMap<>();
        refreshAddresses();
    }

    private void refreshAddresses() {
        addresses = new HashMap<>(config.getAddressess());
        leased = leaseFile.getLeasedAddresses();
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
        return device.getName() + "_DHCP_server";
    }
}
