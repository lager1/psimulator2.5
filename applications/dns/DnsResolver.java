package applications.dns;

import applications.Application;
import commands.linux.LinuxCommand;
import commands.linux.LinuxCommand.LinuxCommandType;
import commands.linux.Ping;
import commands.linux.Traceroute;
import config.configFiles.HostsFile;
import config.configFiles.ResolvConfFile;
import dataStructures.PacketItem;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.L3.IpPacket;
import dataStructures.packets.L4.UdpPacket;
import dataStructures.packets.L7.DnsPacket;
import dataStructures.packets.L7.dns.DnsQuestion;
import device.Device;
import filesystem.FileSystem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import psimulator2.Psimulator;
import utils.Wakeable;

/**
 * @author Michal Horacek
 */
public class DnsResolver extends Application implements Wakeable {

    private final FileSystem fs;
    private HostsFile hostsFile;
    private ResolvConfFile resolvConf;
    private final LinuxCommand command;
    private String query;
    private boolean wakedByAlarm = false;
    private List<IpAddress> nameServers;
    private int ttl = 16;

    /**
     * @param device
     * @param cmd
     * @param toResolve
     */
    public DnsResolver(Device device, LinuxCommand cmd, String toResolve) {
        super("dns-resolver", device);
        this.fs = device.getFilesystem();
        this.hostsFile = applicationLayer.getHostsFile();
        this.resolvConf = applicationLayer.getResolvFile();
        this.command = cmd;
        this.query = toResolve;
    }

    /**
     *
     */
    public void resolveAddress() {
        IpAddress res;

        if (query == null) {
            answerResolved(null);
            return;
        }

        // check /etc/hosts file
        res = hostsFile.resolveAddress(query);
        if (res != null) {
            answerResolved(res);
            return;
        }

        // normalize query
        if (!query.endsWith(".")) {
            query += ".";
        }


        if (!isValidDomainName(query)) {
            answerResolved(null);
            return;
        }

        nameServers = resolvConf.getNameServers();

        // dns server running on local machine
        if (transportLayer.isRunningApp(DnsServer.PORT)) {
            queryLocalDatabase();
        } else {
            // try quering nameservers found in resolv.conf file
            queryAddress();
        }
    }

    private void answerResolved(IpAddress res) {
        if (command.type == LinuxCommandType.PING) {
            ((Ping) command).runTranslated(res);
        } else if (command.type == LinuxCommandType.TRACEROUTE) {
            ((Traceroute) command).executeCommand(res);
        }

        this.exit();
    }

    /**
     * Checks if parameter is a valid domain name
     *
     * @param toResolve
     * @return true if toResolve is a valid domain name
     */
    public static boolean isValidDomainName(String toResolve) {
        if (toResolve == null) {
            return false;
        }

        String DN_PATTERN = "^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2, })\\.$";
        Pattern dnPattern = Pattern.compile(DN_PATTERN);
        Matcher dnMatcher = dnPattern.matcher(toResolve);

        return dnMatcher.matches();
    }

    private void queryAddress() {
        if (!nameServers.isEmpty()) {
            IpAddress nsAddress = nameServers.remove(0);
            sendPacket(nsAddress, new DnsQuestion(query));
            setTimeout();
        } else {
            answerResolved(null);
        }
    }

    private void setTimeout() {
        Psimulator.getPsimulator().budik.registerWake(this, 1000);
    }

    private void handleIncomingPacket(PacketItem packetItem) {
        DnsPacket dnsPacket = unwrapPacket(packetItem);
        IpAddress resAddress;

        if (dnsPacket == null || dnsPacket.answers == null) {
            queryAddress();
        }

        if (dnsPacket.answers.isEmpty()) {
            // received no information about auth. nameservers for given query
            if (dnsPacket.additional.isEmpty()) {
                queryAddress();

                // try asking received auth. nameserver
            } else {
                IpAddress nsAddress = IpAddress.correctAddress(dnsPacket.additional.get(0).aData);

                if (nsAddress == null) {
                    queryAddress();
                } else {
                    sendPacket(nsAddress, new DnsQuestion(query));
                    setTimeout();
                }
            }
        } else {
            resAddress = IpAddress.correctAddress(dnsPacket.answers.get(0).aData);
            answerResolved(resAddress);
        }
    }

    private void sendPacket(IpAddress nsAddress, DnsQuestion question) {
        DnsPacket dns = new DnsPacket(DnsPacket.DnsPacketType.QUERY, 1, question);
        UdpPacket udp = new UdpPacket(this.port, DnsServer.PORT, dns);

        applicationLayer.getIpLayer().sendPacket(udp, null, nsAddress, ttl);
    }

    private void queryLocalDatabase() {
        DnsQuestion question = new DnsQuestion(query);
        DnsPacket dns = new DnsPacket(DnsPacket.DnsPacketType.QUERY, 1, question);
        UdpPacket udp = new UdpPacket(this.port, DnsServer.PORT, dns);
        IpPacket ip = new IpPacket(null, new IpAddress("127.0.0.1"), ttl, udp);
        PacketItem item = new PacketItem(ip, null);

        transportLayer.forwardPacketToApplication(item, DnsServer.PORT);
    }

    @Override
    protected void atStart() {
        if (hostsFile == null) {
            hostsFile = new HostsFile(device.getFilesystem());
        }

        if (resolvConf == null) {
            resolvConf = new ResolvConfFile(device.getFilesystem());
        }

        if (!fs.exists(hostsFile.getFilePath())) {
            hostsFile.createFile();
        }

        if (!fs.exists(resolvConf.getFilePath())) {
            resolvConf.createFile();
        }
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
        } else if (wakedByAlarm) {
            queryAddress();
        } else {
            resolveAddress();
        }

        wakedByAlarm = false;
    }

    @Override
    public String getDescription() {
        return device.getName() + " DNS resolver";
    }

    @Override
    public void wake() {
        wakedByAlarm = true;
        worker.wake();
    }

    private DnsPacket unwrapPacket(PacketItem packetItem) {
        IpPacket ip;
        UdpPacket udp;
        DnsPacket dns;
        try {
            ip = (IpPacket) packetItem.packet;
            udp = (UdpPacket) ip.data;
            dns = (DnsPacket) udp.getData();
        } catch (Exception ex) {
            return null;
        }

        ttl = ip.ttl - 1;
        return dns;
    }
}
