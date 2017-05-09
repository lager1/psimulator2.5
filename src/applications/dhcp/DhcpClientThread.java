package applications.dhcp;

import config.configFiles.DhcpClientLeaseFile;
import config.configFiles.DhcpClientLeaseFile.ClientLeaseRecord;
import dataStructures.MacAddress;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.L3.IpPacket;
import dataStructures.packets.L4.UdpPacket;
import dataStructures.packets.L7.dhcp.DhcpPacket;
import dataStructures.packets.L7.dhcp.DhcpPacketType;
import device.Device;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import networkModule.IpNetworkModule;
import networkModule.L2.EthernetLayer;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import networkModule.SwitchNetworkModule;
import psimulator2.Psimulator;
import shell.apps.CommandShell.CommandShell;
import utils.SmartRunnable;
import utils.Wakeable;
import utils.WorkerThread;

public class DhcpClientThread implements Wakeable, SmartRunnable {

    private final List<DhcpPacket> buffer = Collections.synchronizedList(new LinkedList<DhcpPacket>());
    private static final int ttl = 64;    // ttl, se kterym se budoui odesilat pakety
    private static final int request_wait_time = 1; // jak dlouho se po odeslani requestu ceka na ACK
    private static final int maxDiscoverCount = 6;
    private static final int maxRequestCount = 6;
    // promenny pro celej beh aplikace:
    private final NetworkInterface iface;
    private final int transaction_id;
    private final EthernetLayer ethLayer;
    private final MacAddress myMac;
    private final IPLayer ipLayer;
    /**
     * Stavovy promenny: True na zacatku nebo po tom, co byla aplikace vzbuzena
     * budikem
     */
    boolean wakedByAlarm = false;
    boolean wakedToLease = false;
    int discoverCount = 0; // pocet odeslanejch zadosti discover
    int requestCount = 0; // pocet odeslanych DHCPREUQEST
    /**
     * 0 - pred zacatkem <br /> 1 - odeslal se discover <br /> 2 - odeslal se
     * request <br /> 3 - vratil se ack - konec
     */
    private State state;
    private IpAddress serverIdentifier = null; // ukladam si server, ze kteryho mi poprve prisla OFFER a kterymu jsem poslal reques
    private final WorkerThread worker;
    private IPwithNetmask expectedAddr;
    private CommandShell shell = null;
    private final DhcpClient dhcpManager;
    private final DhcpClientLeaseFile leaseFile;

    //public DhcpClient(Device device, ApplicationNotifiable command, NetworkInterface iface) {
    public DhcpClientThread(Device device, NetworkInterface iface) {
        this.iface = iface;
        transaction_id = (int) ((Math.random()) * Integer.MAX_VALUE); //nahodne se generuje 32-bitovy transaction ID
        ethLayer = ((SwitchNetworkModule) device.getNetworkModule()).ethernetLayer;
        ipLayer = ((IpNetworkModule) device.getNetworkModule()).ipLayer;
        dhcpManager = ipLayer.getNetMod().applicationLayer.getDhcpManager();
        leaseFile = dhcpManager.getLeaseFile();
        worker = new WorkerThread(this);

        myMac = iface.getMacAddress();
        state = State.START;
        expectedAddr = null;
    }

    @Override
    public void wake() {
        wakedByAlarm = true;
        worker.wake();
    }

    public void lease(CommandShell shell) {
        wakedToLease = true;
        this.shell = shell;
        worker.wake();
    }

    public void receivePacket(DhcpPacket received) {
        buffer.add(received);
        worker.wake();
    }

    @Override
    public void doMyWork() {
        if (!buffer.isEmpty()) {
            while (!buffer.isEmpty()) {
                handleIncomingPacket(buffer.remove(0));
            }
        } else if (wakedToLease) {
            ClientLeaseRecord rec = leaseFile.activeLease(iface);

            // Existuje stale platny zaznam, pouzije se ten misto zadosti o novou adresu
            if (rec.leasedAddress != null) {
                setNewIpAddress(rec.leasedAddress);
            } else {
                discoverCount = 0;
                expectedAddr = null;
                sendDiscover();
            }
        } else if (wakedByAlarm) {
            // DISCOVER timeout - neprisel OFFER packet
            if (state == State.DISCOVER_SENT) {
                if (discoverCount <= maxDiscoverCount) {
                    sendDiscover();
                } else {
                    state = State.START;
                }
                // REQUEST timeout - neprisel ACK packet
            } else if (state == State.REQUEST_SENT) {
                if (requestCount <= maxRequestCount) {
                    sendPacket(DhcpPacketType.REQUEST, expectedAddr, serverIdentifier);
                } else {
                    discoverCount = 0;
                    sendDiscover();
                    state = State.DISCOVER_SENT;
                }
                expectedAddr = null;
                sendDiscover();
            } else if (state == State.LEASED) {
                freeLease();
            }
        } else {
            // pri startu aplikace - kontrola, jestli existuje platny lease zaznam
            checkLeasesState();
        }

        // nakonec zrusim priznak:
        wakedToLease = false;
        wakedByAlarm = false;
    }

    private void freeLease() {
        // pro staticke rozhrani nic nedelat
        if (!iface.isDhcp) {
            return;
        }

        ClientLeaseRecord rec = leaseFile.activeLease(iface);

        // pokud lease zaznam uz neni platny, vynullovat ip adresu na rozhrani
        if (rec == null || rec.expireDate == null) {
            ipLayer.changeIpAddressOnInterface(iface, null);
            iface.isDhcp = false;
            state = State.START;
        }
    }

    private void checkLeasesState() {
        if (iface == null || iface.getIpAddress() != null || leaseFile == null) {
            return;
        }

        ClientLeaseRecord rec = leaseFile.activeLease(iface);
        if (rec.leasedAddress != null) {
            setNewIpAddress(rec.leasedAddress);
            setExpirationDate(rec.expireDate);
        }
    }

    private void handleIncomingOffer(DhcpPacket recDhcp) {
        if (state == State.DISCOVER_SENT) {
            expectedAddr = recDhcp.ipToAssign;
            serverIdentifier = recDhcp.serverIdentifier;
            requestCount = 0;
            sendPacket(DhcpPacketType.REQUEST, null, serverIdentifier);    // posilam request
            state = State.REQUEST_SENT;
            Psimulator.getPsimulator().budik.registerWake(this, request_wait_time * 1000);    // nastavuju cekani na ACK
        }
    }

    private void handleIncomingAck(DhcpPacket recDhcp) {
        if (state == State.REQUEST_SENT) {

            // adresa prijata v ACK packetu se lisi od adresy v OFFER packetu
            if (!recDhcp.ipToAssign.equals(expectedAddr)) {
                return;
            }

            // nastaveni nove adresy na rozhrani
            setNewIpAddress(recDhcp.ipToAssign);
            leaseFile.appendLease(recDhcp, iface);
            handleRouting(recDhcp);
            updateNameservers(recDhcp);
            setExpirationDate(getExpirationDate(recDhcp));
            expectedAddr = null;
            state = State.LEASED;

            // ulozi konfiguraci do xml souboru site
            Psimulator psimulator = Psimulator.getPsimulator();
            psimulator.saveSimulatorToConfigFile(psimulator.lastConfigFile);
        }
    }

    private void handleIncomingPacket(DhcpPacket recDhcp) {
        // jsem ve stavu, ve kterem ocekavam dhcp packet - vypisu, co prislo
        if (shell != null && (state == State.DISCOVER_SENT || state == State.REQUEST_SENT)) {
            shell.printLine("DHCP" + recDhcp.type + " from " + recDhcp.serverIdentifier);
        }

        switch (recDhcp.type) {
            case OFFER:
                handleIncomingOffer(recDhcp);
                break;
            case ACK:
                handleIncomingAck(recDhcp);
                break;
            default:

        }
    }

    private void handleRouting(DhcpPacket recDhcp) {
        if (recDhcp.options.containsKey("routers")) {

            // z dhcp packetu ziskam seznam vsech rout
            String routersOption = recDhcp.options.get("routers");
            String[] routers = routersOption.split(" ");

            for (String router : routers) {
                IpAddress routerAddr = IpAddress.correctAddress(router);

                // pokud je string platna ip adresa, pridam do routovaci tabulky
                if (routerAddr != null) {
                    IPwithNetmask routeAddr =
                            new IPwithNetmask(routerAddr, recDhcp.ipToAssign.getMask());
                    //ipLayer.routingTable.addRecord(routeAddr.getNetworkNumber(), iface);
                    ipLayer.routingTable.addRecord(routeAddr.getNetworkNumber(), routerAddr, iface);
                }
            }
        }
    }

    private void updateNameservers(DhcpPacket recDhcp) {
        if (recDhcp.options.containsKey("domain-name-servers")) {
            String nameServerOption = recDhcp.options.get("domain-name-servers");
            String[] nameServers = nameServerOption.split(" ");
            this.ipLayer.getNetMod().applicationLayer
                    .getResolvFile().updateNameservers(nameServers);
        }
    }

    private Date getExpirationDate(DhcpPacket recDhcp) {
        SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");
        Date startDate;
        Date endDate;

        try {
            startDate = df.parse(recDhcp.options.get("lease-start"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.SECOND, Integer.parseInt(recDhcp.options.get("lease-time")));
            endDate = cal.getTime();
        } catch (ParseException | NumberFormatException ex) {
            Logger.getLogger(DhcpClientThread.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }

        return endDate;
    }

    private boolean setExpirationDate(Date endDate) {
        // neplatne datum expirace zaznamu
        if (endDate == null) {
            return false;
        }

        long waiting;
        waiting = endDate.getTime() - System.currentTimeMillis();

        if (waiting <= 0) {
            return false;
        }

        Psimulator.getPsimulator().budik.registerWake(this, waiting + 1);
        return true;
    }

    private void setNewIpAddress(IPwithNetmask newIpAddress) {
        // vymazani starych informaci o routach a ip adrese
        ipLayer.routingTable.flushRecords(iface);
        ipLayer.changeIpAddressOnInterface(iface, null);

        // update interface souboru
        ipLayer.getNetMod().applicationLayer.getInterfacesFile().createFile();

        // nacteni novych informaci
        ipLayer.changeIpAddressOnInterface(iface, newIpAddress);
        iface.isDhcp = true;
        ipLayer.routingTable.addRecord(newIpAddress.getNetworkNumber(), iface);
        state = State.LEASED;
    }

    /**
     * Odesle DHCP paket. Udelana tak, aby omhla posilat veskery klientsky
     * pakety.
     *
     * @param type
     * @param ipToAssign     null if type is discover
     * @param serverIdentier null if type is discover
     */
    private void sendPacket(DhcpPacketType type, IPwithNetmask ipToAssign, IpAddress serverIdentier) {
        // sestavim paket:
        DhcpPacket discover = new DhcpPacket(type, transaction_id, serverIdentier, ipToAssign, null, myMac, null);
        UdpPacket udp = new UdpPacket(DhcpServer.CLIENT_PORT, DhcpServer.SERVER_PORT, discover);
        IpPacket ip = new IpPacket(new IpAddress("0.0.0.0"), new IpAddress("255.255.255.255"), ttl, udp);
        // poslui paket:
        ethLayer.sendPacket(ip, iface.ethernetInterface, MacAddress.broadcast());

    }

    /**
     * Posle DHCP discover a naridi budik, kdy se ma zas vzbudit
     */
    private void sendDiscover() {
        // poslu DISCOVER packet
        sendPacket(DhcpPacketType.DISCOVER, null, null);

        // nastavim budik pro vzbuzeni
        int interval = ((int) Math.random()) * 5 + 3; // nahodne se generuje interval, kdy se bude znova posilat discover
        // -> asi je to opravdu nahodne, ale ty cisla jsem si vymyslel
        Psimulator.getPsimulator().budik.registerWake(this, interval * 1000);
        // vypisu a nastavim stav:

        if (shell != null) {
            shell.printLine("DHCPDISCOVER on " + iface.name
                    + " to 255.255.255.255 port " + DhcpServer.SERVER_PORT);
        }

        state = State.DISCOVER_SENT;
        discoverCount++;
    }

    // jen pomocny informativni metody:
    @Override
    public String getDescription() {
        return ipLayer.getNetMod().getDevice().getName() + ": DHCP_client";
    }

    public State getState() {
        return this.state;
    }

    public IPwithNetmask getExpectedAddress() {
        return this.expectedAddr;
    }

    public static enum State {

        START,
        DISCOVER_SENT,
        REQUEST_SENT,
        LEASED,
    }
}
