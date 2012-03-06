/*
 * created 22.2.2012
 */
package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import device.Device;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import networkModule.L3.NetworkInterface;
import shell.apps.CommandShell.CommandShell;
import static shell.apps.CommandShell.CommandShell.*;


/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoCommandParser extends AbstractCommandParser {

	/**
     * Specialni formatovac casu pro vypis servisnich informaci z cisca.
     */
    private DateFormat formator = new SimpleDateFormat("MMM  d HH:mm:ss.SSS");

	/**
     * Pomocna promenna pro zachazeni s '% Ambiguous command: '
	 *
	 * TODO: nezrusit tohle?
	 *
     */
    private boolean isAmbiguousCommand = false;
	/**
     * Rozhrani, ktere se bude upravovat ve stavu IFACE
     */
    private NetworkInterface configuredInterface = null;

	private AbstractCommand command = null;

	public CiscoCommandParser(Device device, CommandShell shell) {
		super(device, shell);
		shell.setPrompt(device.getName()+">");
	}

	@Override
	public void processLineForParsers() {

		isAmbiguousCommand = false;

		String first = nextWord();

		switch (mode) {
            case CISCO_USER_MODE:
                if (isCommand("enable", first)) {
					changeMode(CISCO_PRIVILEGED_MODE);
                    return;
                }
                if (isCommand("ping", first)) {
//                    command = new CiscoPing(pc, kon, slova);
//                    return;
                }
                if (isCommand("traceroute", first)) {
//                    command = new CiscoTraceroute(pc, kon, slova);
//                    return;
                }
                if (isCommand("show", first)) {
                    command = new ShowCommand(this);
					command.run();
                    return;
                }
                if (isCommand("exit", first) || isCommand("logout", first)) {
					shell.closeSession();
                    return;
                }
                break;

            case CISCO_PRIVILEGED_MODE:
                if (isCommand("enable", first)) { // funguje u cisco taky, ale nic nedela
                    return;
                }
                if (isCommand("disable", first)) {
					changeMode(CISCO_USER_MODE);
                    return;
                }
                if (isCommand("ping", first)) {
//                    command = new CiscoPing(pc, kon, slova);
//                    return;
                }
                if (isCommand("traceroute", first)) {
//                    command = new CiscoTraceroute(pc, kon, slova);
//                    return;
                }
                if (isCommand("configure", first)) {
//                    configure();
					command = new ConfigureCommand(this);
					command.run();
                    return;
                }
                if (isCommand("show", first)) {
                    command = new ShowCommand(this);
					command.run();
					return;
                }
                if (isCommand("exit", first) || isCommand("logout", first)) {
                    shell.closeSession();
                    return;
                }

//                if (debug) {
//                    if (isCommand("ip", first)) {
//                        command = new CiscoIp(pc, kon, slova, false, mode);
//                        return;
//                    }
//                    if (isCommand("access-list", first)) {
//                        command = new CiscoAccessList(pc, kon, slova, false);
//                        return;
//                    }
//                    if (isCommand("no", first)) {
//                        no();
//                        return;
//                    }
//                }
                break;

            case CISCO_CONFIG_MODE:
                if (isCommand("exit", first) || first.equals("end")) {
					changeMode(CISCO_PRIVILEGED_MODE);
//                    mode = ROOT;
//                    kon.prompt = pc.jmeno + "#";
//                    Date d = new Date();
//                    cekej(100);
//                    kon.posliRadek(formator.format(d) + ": %SYS-5-CONFIG_I: Configured from console by console");
                    return;
                }
                if (isCommand("ip", first)) {
                    command = new IpCommand(this, false);
                    return;
                }
//                if (isCommand("interface", first)) {
//                    iface();
//                    return;
//                }
//                if (isCommand("access-list", first)) {
//                    command = new CiscoAccessList(pc, kon, slova, false);
//                    return;
//                }
//                if (isCommand("no", first)) {
//                    no();
//                    return;
//                }
                break;

//            case CISCO_CONFIG_IF_MODE:
//                if (isCommand("exit", first)) {
//                    mode = CONFIG;
//                    kon.prompt = pc.jmeno + "(config)#";
//                    aktualni = null; // zrusime odkaz na menene rozhrani
//                    return;
//                }
//                if (first.equals("end")) {
//                    mode = ROOT;
//                    kon.prompt = pc.jmeno + "#";
//                    Date d = new Date();
//                    kon.posliRadek(formator.format(d) + ": %SYS-5-CONFIG_I: Configured from console by console");
//                    return;
//                }
//                if (isCommand("ip", first)) {
//                    command = new CiscoIp(pc, kon, slova, false, mode, aktualni);
//                    return;
//                }
//                if (isCommand("no", first)) {
//                    no();
//                    return;
//                }
//                if (isCommand("shutdown", first)) {
//                    shutdown();
//                    return;
//                }
//
//				break;
        }

//        if (debug) {
//            if (slova.get(0).equals("ifconfig")) { // pak smazat
//                command = new LinuxIfconfig(pc, kon, slova);
//                return;
//            } else if (slova.get(0).equals("route")) {
//                command = new LinuxRoute(pc, kon, slova);
//                return;
//            }
//        }
//
        if (isAmbiguousCommand) {
            isAmbiguousCommand = false;
            ambiguousCommand();
            return;
        }

        switch (mode) {
            case CISCO_CONFIG_MODE:
            case CISCO_CONFIG_IF_MODE:
                invalidInputDetected();
                break;

            default:
				shell.printLine("% Unknown command or computer name, or unable to find computer address");
        }
	}

	@Override
	public void catchSignal(int sig) {
		// tady bude pak reakce na Ctrl+Z
		//		prechod do jinych modu

		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String[] getCommands(int mode) {
		// TODO: getCommands() poresit
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Vrati
	 */
	protected String getFormattedTime() {
		Date d = new Date();
		return formator.format(d);
	}

	/**
     * Tato metoda simuluje zkracovani commandu tak, jak cini cisco.
     * @param command command, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd command, ktery zadal uzivatel
     * @return Vrati true, pokud retezec cmd je jedinym moznym commandem, na ktery ho lze doplnit.
     */
    protected boolean isCommand(String command, String cmd) {

        int i = 10;

        // Zde jsou zadefinovany vsechny commandy. Jsou rozdeleny do poli podle poctu znaku,
        // ktere je potreba k jejich bezpecne identifikaci. Cisla byla ziskana z praveho cisca IOS version 12.4
        String[] jedna = {"terminal", "inside", "outside", "source", "static", "pool", "netmask", "permit"};
        // + ip, exit
        String[] dva = {"show", "interface", "no", "shutdown", "enable", "classless",
            "access-list", "ping", "logout", "nat", "traceroute"};
        // + ip, exit
        String[] tri = {"running-config", "name-server", "nat", "address"};
        // + exit
        String[] ctyri = {"configure", "disable"};
        //String[] pet = {"route"};

        List<String[]> seznam = new ArrayList<>();
        seznam.add(jedna);
        seznam.add(dva);
        seznam.add(tri);
        seznam.add(ctyri);
        //seznam.add(pet);

        int n = 0;
        for (String[] pole : seznam) { // nastaveni spravne delky dle zarazeni do seznamu
            n++;
            for (String s : pole) {
                if (s.equals(command)) {
                    i = n;
                }
            }
        }

        if (command.equals("exit")) { // specialni chovani commandu exit v ruznych stavech
            switch (mode) {
                case CISCO_USER_MODE:
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_MODE:
                    i = 3;
                    break;
                case CISCO_CONFIG_IF_MODE:
                    i = 1;
            }
        }
        if (command.equals("ip")) { // specialni chovani commandu ip v ruznych stavech
            switch (mode) {
                case CISCO_CONFIG_MODE:
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_IF_MODE:
                    i = 1;
            }
        }
        if (command.equals("route")) { // specialni chovani commandu route v ruznych stavech
            switch (mode) {
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_MODE:
                    i = 5;
            }
        }

        if (cmd.length() >= i && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny command
            return true;
        }

        if (command.startsWith(cmd)) {
            isAmbiguousCommand = true;
        }

        return false;
    }

	/**
     * Vypise chybovou hlasku pri zadani neplatneho vstupu.
     */
    protected void invalidInputDetected() {
        shell.printLine("\n% Invalid input detected.\n");
    }

	/**
     * Vypise chybovou hlasku pri zadani nekompletniho commandu.
     */
    protected void incompleteCommand() {
        shell.printLine("% Incomplete command.");
    }

    /**
     * Vypise hlasku do konzole "% Ambiguous command: ".
     */
    protected void ambiguousCommand() {
        shell.printLine("% Ambiguous command:  \"" + line + "\"");
    }

	/**
	 * Target mode we want change to.
	 * @param mode
	 */
	protected void changeMode(int mode) {
		switch (mode) {
			case CISCO_USER_MODE:
				shell.setMode(mode);
                shell.setPrompt(device.getName() + ">");
				break;

			case CISCO_PRIVILEGED_MODE:
				shell.setMode(mode);
				shell.setPrompt(device.getName() + "#");

				if (this.mode == CISCO_CONFIG_MODE || this.mode == CISCO_CONFIG_IF_MODE) { // jdu z configu
					shell.printWithDelay(getFormattedTime() + ": %SYS-5-CONFIG_I: Configured from console by console", 100);
				}
				break;

			case CISCO_CONFIG_MODE:
				shell.setMode(mode);
				shell.setPrompt(device.getName() + "(config)#");
				if (this.mode == CISCO_PRIVILEGED_MODE) { // jdu z privilegovaneho
					shell.printLine("Enter configuration commands, one per line.  End with 'exit'."); // zmena oproti ciscu: End with CNTL/Z.
	//				configure1 = false;
	//				kon.vypisPrompt = true;
				}
				break;

			case CISCO_CONFIG_IF_MODE:
				shell.setMode(mode);
				shell.setPrompt(device.getName() + "(config-if)#");
				break;

			default:
				throw new RuntimeException("Change to unsupported mode: "+mode);

		}
	}
}
