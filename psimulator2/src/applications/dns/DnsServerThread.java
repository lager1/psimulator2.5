package applications.dns;

import applications.dns.DnsServer.ZoneInfo;
import config.configFiles.DnsZoneFile;
import config.configFiles.NamedConfFile;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.IpPacket;
import dataStructures.packets.UdpPacket;
import dataStructures.packets.dns.DnsPacket;
import dataStructures.packets.dns.DnsPacket.DnsStatus;
import device.Device;
import filesystem.FileSystem;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L7.ApplicationLayer;

/**
 *
 * @author Michal Horacek
 */
public class DnsServerThread implements Runnable {

    private final DnsServer server;
    private final PacketItem packetItem;
    private IpPacket incIpPacket;
    private UdpPacket incUdpPacket;
    private final NamedConfFile confFile;
    private final FileSystem fs;
    private final Device device;
    private final ApplicationLayer appLayer;

    public DnsServerThread(DnsServer server, ApplicationLayer appLayer, PacketItem packet) {
        this.packetItem = packet;
        this.confFile = server.namedConf;
        this.fs = server.fs;
        this.device = appLayer.getNetMod().getDevice();
        this.server = server;
        this.appLayer = appLayer;
    }

    @Override
    public void run() {
        // getting dns packet from incoming packet item
        DnsPacket dnsPacket = unwrapPacket();
        if (dnsPacket == null) {
            sendAnswer(DnsStatus.FORMAT_ERROR);
            return;
        }

        // checking query validity
        if (!DnsResolver.isValidDomainName(dnsPacket.question.qName)) {
            sendAnswer(DnsStatus.FORMAT_ERROR);
            return;
        }
		
        DnsQuery query = getZoneInfo(dnsPacket.question.qName);
        if (query == null) {
            sendAnswer(DnsStatus.NAME_ERROR);
            return;
        }
        
        queryAnswer(query, dnsPacket);
    }

    private DnsQuery getZoneInfo(String queryString) {
        int dotIndex;
        String[] res = {"", queryString};

        while (true) {
            if (server.zoneDatabase.containsKey(res[1])) {
                DnsQuery resQuery = new DnsQuery(res[0], res[1], server.zoneDatabase.get(res[1]));
                return resQuery;
            }

            dotIndex = res[1].indexOf(".");
            if (dotIndex == -1) {
                return null;
            }

            res[0] = res[1].substring(0, dotIndex);
            res[1] = res[1].substring(dotIndex + 1);
        }
    }

    private void queryAnswer(DnsQuery query, DnsPacket packet) {
        DnsZoneFile zoneFile = new DnsZoneFile(device.getFilesystem());
        zoneFile.resolveQuery(query, packet);
        sendPacket(packet);
    }

    private void sendAnswer(DnsStatus status) {
                    DnsPacket answer = new DnsPacket(DnsPacket.DnsPacketType.ANSWER, 0, null);
            answer.status = status;
            sendPacket(answer);
    }
    
    private void sendPacket(DnsPacket packet) {
        packet.type = DnsPacket.DnsPacketType.ANSWER;
        UdpPacket udp = new UdpPacket(DnsServer.PORT, incUdpPacket.srcPort, packet);
      
        if (incIpPacket.dst.equals(new IpAddress("127.0.0.1"))) {
            handleLocalQuery(udp);
        } else {
            appLayer.getIpLayer().sendPacket(udp, null, incIpPacket.src, incIpPacket.ttl - 1);
        }
    }

    private void handleLocalQuery(UdpPacket udp) {
        IpPacket ip = new IpPacket(null, null, 0, udp);
        PacketItem item = new PacketItem(ip, null);
        appLayer.getNetMod().transportLayer.forwardPacketToApplication(item, udp.dstPort);
    }

    private DnsPacket unwrapPacket() {
        DnsPacket dns;

        try {
            incIpPacket = this.packetItem.packet;
            incUdpPacket = (UdpPacket) incIpPacket.data;
            dns = (DnsPacket) incUdpPacket.getData();
        } catch (Exception ex) {
            return null;
        }

        return dns;
    }

    public static class DnsQuery {

        public ZoneInfo info;
        public String query;
        public String zone;

        public DnsQuery(String query, String zoneName, ZoneInfo info) {
            this.info = info;
            this.query = query;
            this.zone = zoneName;
        }
    }
}
