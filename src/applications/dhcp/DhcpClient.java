package applications.dhcp;

import applications.Application;
import config.configFiles.DhcpClientLeaseFile;
import dataStructures.PacketItem;
import dataStructures.packets.dhcp.DhcpPacket;
import dataStructures.packets.IpPacket;
import dataStructures.packets.UdpPacket;
import dataStructures.packets.dhcp.DhcpPacketType;
import device.Device;
import java.util.HashMap;
import java.util.Map;
import networkModule.IpNetworkModule;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Michal Horacek
 */
public class DhcpClient extends Application {

    public final int DHCP_CLIENT_PORT = 68;
    private final IPLayer ipLayer;
    private Map<NetworkInterface, DhcpClientThread> interfaces;
    private final DhcpClientLeaseFile leaseFile;

    public DhcpClient(Device device) {
        super("dhclient", device);
        this.ipLayer = ((IpNetworkModule) device.getNetworkModule()).ipLayer;
        this.leaseFile = new DhcpClientLeaseFile(device.getFilesystem());        
        port = DHCP_CLIENT_PORT;
    }

    public void lease(NetworkInterface iface, CommandShell shell) {
        if (interfaces.containsKey(iface)) {
            interfaces.get(iface).lease(shell);
        }
    }

    public void initInterfaces() {
        interfaces = new HashMap<>();

        for (NetworkInterface iface : ipLayer.getNetworkIfaces()) {
            interfaces.put(iface, new DhcpClientThread(device, iface));
        }
    }

    private void handleIncomingPacket(PacketItem pItem) {
        // nejdriv se prectu ten paket:
        IpPacket recIp;
        DhcpPacket recDhcp;

	// prelozim pakety, a kdyby bylo neco spatne, koncim
        // delam to vsechno najednou
        try {
            recIp = pItem.packet;
            recDhcp = (DhcpPacket) ((UdpPacket) recIp.data).getData();
            recDhcp.getSize();	// abych si overil, ze to neni null
        } catch (Exception ex) {
            return;
        }

        for (NetworkInterface key : interfaces.keySet()) {
            DhcpClientThread client = interfaces.get(key);

            if (recDhcp.type == DhcpPacketType.OFFER && client.getState() == DhcpClientThread.State.DISCOVER_SENT) {
                client.receivePacket(recDhcp);
            } else if (recDhcp.type == DhcpPacketType.ACK
                    && client.getState() == DhcpClientThread.State.REQUEST_SENT
                    && recDhcp.ipToAssign.equals(client.getExpectedAddress())) {
                client.receivePacket(recDhcp);
            }
        }
    }

    public DhcpClientLeaseFile getLeaseFile() {
        return this.leaseFile;
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

    @Override
    public void doMyWork() {
        if (!buffer.isEmpty()) {
            handleIncomingPacket(buffer.remove(0));
        }
    }

    @Override
    public String getDescription() {
        return "DHCP manager";
    }
}
