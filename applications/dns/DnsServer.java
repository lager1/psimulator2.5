package applications.dns;

import applications.Application;
import config.configFiles.NamedConfFile;
import dataStructures.PacketItem;
import device.Device;
import filesystem.FileSystem;
import java.util.Map;

/**
 *
 * @author Michal Horacek
 */
public class DnsServer extends Application {

    public static final int PORT = 53;
    protected FileSystem fs;
    protected NamedConfFile namedConf;

    protected Map<String, ZoneInfo> zoneDatabase;

    public DnsServer(Device device) {
        super("dns_server", device);
        this.port = PORT;
        this.fs = device.getFilesystem();
        namedConf = new NamedConfFile(device.getFilesystem());
    }

    private void handleIncomingPacket(PacketItem packetItem) {
        new DnsServerThread(this, this.applicationLayer, packetItem).start();
    }
    
    @Override
    protected void atStart() {
        if (!fs.exists(namedConf.getFilePath())) {
            namedConf.createFile();
        }

        zoneDatabase = namedConf.getZones();
    }

    @Override
    protected void atExit() {
    }

    @Override
    protected void atKill() {
    }

    @Override
    public void doMyWork() {
        while (!buffer.isEmpty()) {
            handleIncomingPacket(buffer.remove(0));
        }
    }

    @Override
    public String getDescription() {
        return device.getName() + ": DNS Server";
    }
    
    public static class ZoneInfo {
        public String file;
        public String type;
    }
}
