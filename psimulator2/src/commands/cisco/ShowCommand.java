/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.nat.NatTable;
import networkModule.L3.nat.NatTable.Record;
import networkModule.L3.NetworkInterface;
import networkModule.L3.nat.AccessList;
import networkModule.L3.nat.NatTable.StaticRule;
import networkModule.L3.nat.Pool;
import networkModule.L3.nat.PoolAccess;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

/**
 * Trida pro zpracovani a obsluhu prikazu 'show'.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ShowCommand extends CiscoCommand {

    private State stavShow = null;
    private int stavCisco;
	private final CiscoIPLayer ipLayer;

    /**
     * Pomocne rozhrani pro prikaz 'show interfaces FastEthernet0/0'
     */
    NetworkInterface iface;

	public ShowCommand(AbstractCommandParser parser) {
		super(parser);
		this.stavCisco = parser.getShell().getMode();
		this.ipLayer = (CiscoIPLayer) getNetMod().ipLayer;
	}

    // show interfaces (rozhrani 0/0)?
    private boolean zpracujInterfaces() {
        stavShow = State.INTERFACES;

        String rozh = nextWord();
        if (rozh.isEmpty()) {
            return true;
        }
        rozh += nextWord();

        iface = null;
        iface = ipLayer.getNetworkIntefaceIgnoreCase(rozh);
        if (iface == null) {
            invalidInputDetected();
            return false;
        }
        return true;
    }

	@Override
	public void run() {
		boolean cont = zpracujRadek();
		if (cont) {
			vykonejPrikaz();
		}

	}

    enum State {

        RUN,
        ROUTE,
        NAT,
        QUESTION_MARK,
        INTERFACES,
    };

    private boolean zpracujRadek() {
        // show ip route
        // show running-config      - jen v ROOT rezimu
        // show interfaces          - jen v ROOT rezimu
        // show ip nat translations

        String dalsi = nextWord(); // druhe slovo
        if (dalsi.isEmpty()) {
            printLine("% Type \"show ?\" for a list of subcommands\n");
            return false;
        }

        if (dalsi.equals("?")) {
            String s = "";
            s += "[PSIMULATOR]: v opravdovem ciscu je tady holy seznam parametru "
                    + "(bez celeho prikazu jako zde!)\n\n";
            s += "  show ip route                   IP routing table\n";
            s += "  show ip nat translations        Translation entries\n";
            if (stavCisco == CommandShell.CISCO_PRIVILEGED_MODE) {
                s += "  show running-config             Current operating configuration\n";
            }
            s += "\n";
            printWithDelay(s, 50);
            return false;
        }

        if (dalsi.startsWith("r")) {
            if (stavCisco == CommandShell.CISCO_USER_MODE) {
                invalidInputDetected();
                return false;
            }

            if (!kontrola("running-config", dalsi, 3)) {
                return false;
            }
            stavShow = State.RUN;
            return true;
        } else {
            if (dalsi.startsWith("in")) {
                if (stavCisco != CommandShell.CISCO_PRIVILEGED_MODE) {
                    invalidInputDetected();
                    return false;
                }
                if (!kontrola("interfaces", dalsi, 3)) {
                    return false;
                }
                return zpracujInterfaces();
            }

            if (!kontrola("ip", dalsi, 2)) {
                return false;
            }

            dalsi = nextWord();
            if (dalsi.startsWith("r")) {
                if (!kontrola("route", dalsi, 2)) {
                    return false;
                }
                stavShow = State.ROUTE;
                return true;
            } else {
                if (!kontrola("nat", dalsi, 2)) {
                    return false;
                }
                if (stavCisco == CommandShell.CISCO_USER_MODE) {
                    invalidInputDetected();
                    return false;
                }
                if (!kontrola("translations", nextWord(), 1)) {
                    return false;
                }
                stavShow = State.NAT;
                return true;
            }
        }
    }

    private void vykonejPrikaz() {
        switch (stavShow) {
            case RUN:
                runningConfig();
                break;
            case ROUTE:
                ipRoute();
                break;
            case NAT:
                ipNatTranslations();
                break;
            case INTERFACES:
                interfaces();
                break;
        }
    }

    /**
     * Posle vypis pro prikaz 'show interfaces.
     */
    private void interfaces() {
        if (iface == null) {
            for (NetworkInterface nIface : ipLayer.getNetworkIfaces()) {
				printWithDelay(getInterfaceReport(nIface), 30);
            }
            printLine("");
            return;
        }
        printWithDelay(getInterfaceReport(iface), 30);
    }

    /**
     * Posle vypis pro prikaz 'show ip nat translations.
     */
    private void ipNatTranslations() {
        String s = "";

		NatTable table = ipLayer.getNatTable();
		table.deleteOldDynamicRecords();

		if (table.getDynamicRules().isEmpty()) {
            s += "\n\n";
			printWithDelay(s, 50);
            return;
        }

		s += Util.zarovnej("Pro Inside global", 24) + Util.zarovnej("Inside local", 20);
        s += Util.zarovnej("Outside local", 20) + Util.zarovnej("Outside global", 20);
        s += "\n";

        for (Record zaznam : table.getDynamicRules()) {
			s += Util.zarovnej("icmp " + zaznam.out.getAddressWithPort(), 24)
					+ Util.zarovnej(zaznam.in.getAddressWithPort(), 20)
					+ Util.zarovnej(zaznam.target.toString(), 20)
					+ Util.zarovnej(zaznam.target.toString(), 20)+"\n";
        }

        for (NatTable.StaticRule rule : table.getStaticRules()) {
			s += Util.zarovnej("--- " + rule.out, 24)
					+ Util.zarovnej(rule.in.toString(), 20)
					+ Util.zarovnej("---", 20)
					+ Util.zarovnej("---", 20) + "\n";
        }

		printWithDelay(s, 50);
    }

    /**
     * Posle vypis pro prikaz 'show ip route'.
     */
    private void ipRoute() {
        String s = "";
        s += ipLayer.wrapper.vypisRT();
        printWithDelay(s, 50);
    }

    /**
     * Prikaz 'show running-config' ve stavu # (ROOT).
     * Aneb vypis rozhrani v uplne silenem formatu.
     */
    private void runningConfig() {
        String s = "";

        s += "Building configuration...\n"
                + "\n"
                + "Current configuration : 827 bytes\n"
                + "!\n"
                + "version 12.4\n"
                + "service timestamps debug datetime msec\n"
                + "service timestamps log datetime msec\n"
                + "no service password-encryption\n"
                + "!\n"
                + "hostname " + getDevice().getName() + "\n"
                + "!\n";
		if (!debug) {
			s += "boot-start-marker\n"
                + "boot-end-marker\n"
                + "!\n"
                + "!\n"
                + "no aaa new-model\n"
                + "!\n"
                + "resource policy\n"
                + "!\n"
                + "mmi polling-interval 60\n"
                + "no mmi auto-configure\n"
                + "no mmi pvc\n"
                + "mmi snmp-timeout 180\n"
                + "ip subnet-zero\n"
                + "ip cef\n"
                + "!\n"
                + "!\n"
                + "no ip dhcp use vrf connected\n"
                + "!\n"
                + "!\n"
                + "!\n";
		}

        for (NetworkInterface singleIface : ipLayer.getNetworkIfaces()) {

            s += "interface " + singleIface.name + "\n";
			if (singleIface.getIpAddress() == null) {
				s += " no ip address\n";
			} else {
				s += " ip address " + singleIface.getIpAddress().getIp() + " " + singleIface.getIpAddress().getMask() + "\n";
			}

            if (ipLayer.getNatTable().getOutside() != null && ipLayer.getNatTable().getOutside().name.equals(singleIface.name)) {
				s += " ip nat outside" + "\n";
            }

			if (ipLayer.getNatTable().getInside(singleIface.name) != null) {
				s += " ip nat inside" + "\n";
			}

            if (singleIface.isUp == false) {
                s += " shutdown" + "\n";
            }
            s += " duplex auto\n speed auto\n!\n";
        }

        if (ipLayer.routingTable.classless) {
            s += "ip classless\n";
        }
        s += ipLayer.wrapper.vypisRunningConfig();

        s += "!\n";
        s += "ip http server\n";

        for (Pool pool : ipLayer.getNatTable().lPool.getSortedPools()) {
            s += "ip nat pool " + pool.name + " " + pool.prvni() + " " + pool.posledni()
                    + " prefix-length " + pool.prefix + "\n";
        }

        for (PoolAccess pa : ipLayer.getNatTable().lPoolAccess.getSortedPoolAccess()) {
            s += "ip nat inside source list " + pa.access + " pool " + pa.poolName;
            if (pa.overload) {
                s += " overload";
            }
            s += "\n";
        }

		for (StaticRule rule : ipLayer.getNatTable().getStaticRules()) {
			s += "ip nat inside source static " + rule.in + " " + rule.out + "\n";
		}

        s += "!\n";

        for (AccessList access : ipLayer.getNatTable().lAccess.getList()) {
            s += "access-list " + access.cislo + " permit " + access.ip.getIp() + " " + access.ip.getMask().getWildcardRepresentation() + "\n";
        }

        if (!debug) {
            s += "!\n" + "!\n" + "control-plane\n"
                    + "!\n" + "!\n" + "line con 0\n"
                    + "line aux 0\n" + "line vty 0 4\n" + " login\n" + "!\n" + "end\n\n";
        }
        printWithDelay(s, 10);
    }

	private String getInterfaceReport(NetworkInterface iface) {
		String s = iface.name + " is ";
		boolean up = false;
		if (!iface.isUp) {
			s += "administratively down";
		} else {
			boolean isCableConnected = iface.ethernetInterface.isConnected();
			if (isCableConnected == false) { // bez kabelu && nahozene
				s += "down";
				// This indicates a physical problem,
				// either with the interface or the cable attached to it.
				// Or not attached, as the case may be.
			} else { // s kabelem && nahozene
				s += "up";
				up = true;
			}
		}
		s += ", line protocol is ";
		if (up) {
			s += "up";
		} else {
			s += "down";
			/*
			 * 1/ encapsulation mismatch, such as when one partner in a point-to-point connection is configured for HDLC
			 * and the other for PPP.
			 *
			 * 2/ Whether the DCE is a CSU/DSU or another Cisco router in a home lab, the DCE must supply a clockrate to
			 * the DTE. If that clockrate is not present, the line protocol will come down.
			 */
		}
		s += "\n";
		String mac = iface.getMacAddress().getCiscoRepresentation();
		s += "  Hardware is Gt96k FE, address is " + mac + " (" + mac + ")\n";
		if (iface.getIpAddress() != null) {
			s += "  Internet address is " + iface.getIpAddress().getIp() + "\n";
		}
		s += "  MTU 1500 bytes, BW 100000 Kbit/sec, DLY 100 usec, \n"
				+ "     reliability 255/255, txload 1/255, rxload 1/255\n"
				+ "  Encapsulation ARPA, loopback not set\n"
				+ "  Keepalive set (10 sec)\n"
				+ "  Auto-duplex, Auto Speed, 100BaseTX/FX\n"
				+ "  ARP type: ARPA, ARP Timeout 04:00:00\n"
				+ "  Last input never, output never, output hang never\n"
				+ "  Last clearing of \"show interface\" counters never\n"
				+ "  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\n"
				+ "  Queueing strategy: fifo\n"
				+ "  Output queue: 0/40 (size/max)\n"
				+ "  5 minute input rate 0 bits/sec, 0 packets/sec\n"
				+ "  5 minute output rate 0 bits/sec, 0 packets/sec\n"
				+ "     0 packets input, 0 bytes\n"
				+ "     Received 0 broadcasts, 0 runts, 0 giants, 0 throttles\n"
				+ "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\n"
				+ "     0 watchdog\n"
				+ "     0 input packets with dribble condition detected\n"
				+ "     115658 packets output, 6971948 bytes, 0 underruns\n"
				+ "     0 output errors, 0 collisions, 1 interface resets\n"
				+ "     0 unknown protocol drops\n"
				+ "     0 babbles, 0 late collision, 0 deferred\n"
				+ "     0 lost carrier, 0 no carrier\n"
				+ "     0 output buffer failures, 0 output buffers swapped out";
		return s;
	}
}

