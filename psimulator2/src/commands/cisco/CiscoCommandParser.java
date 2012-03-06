/*
 * created 22.2.2012
 */
package commands.cisco;

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

	public CiscoCommandParser(Device device, CommandShell shell) {
		super(device, shell);
		shell.setPrompt(device.getName()+">");
	}

	@Override
	public int processLineForParsers() {

		isAmbiguousCommand = false;

		if (words.size() < 1) {
            return 0; // jen mezera
        }

		String first = nextWord();

		if (first.equals("")) {
            return 0; // prazdny Enter
        }

		switch (mode) {
            case CISCO_USER_MODE:
                if (isCommand("enable", first)) {
					changeMode(CISCO_PRIVILEGED_MODE);
                    return 0;
                }
                if (isCommand("ping", first)) {
//                    prikaz = new CiscoPing(pc, kon, slova);
//                    return;
                }
                if (isCommand("traceroute", first)) {
//                    prikaz = new CiscoTraceroute(pc, kon, slova);
//                    return;
                }
                if (isCommand("show", first)) {
//                    prikaz = new CiscoShow(pc, kon, slova, mode);
//                    return;
                }
                if (isCommand("exit", first) || isCommand("logout", first)) {
					shell.closeSession();
                    return 0;
                }
                break;

            case CISCO_PRIVILEGED_MODE:
                if (isCommand("enable", first)) { // funguje u cisco taky, ale nic nedela
                    return 0;
                }
                if (isCommand("disable", first)) {
					changeMode(CISCO_USER_MODE);
                    return 0;
                }
                if (isCommand("ping", first)) {
//                    prikaz = new CiscoPing(pc, kon, slova);
//                    return;
                }
                if (isCommand("traceroute", first)) {
//                    prikaz = new CiscoTraceroute(pc, kon, slova);
//                    return;
                }
                if (isCommand("configure", first)) {
//                    configure();
                    return 0;
                }
                if (isCommand("show", first)) {
//                    prikaz = new CiscoShow(pc, kon, slova, mode);
//                    return;
                }
                if (isCommand("exit", first) || isCommand("logout", first)) {
                    shell.closeSession();
                    return 0;
                }

//                if (debug) {
//                    if (kontrola("ip", first)) {
//                        prikaz = new CiscoIp(pc, kon, slova, false, mode);
//                        return;
//                    }
//                    if (kontrola("access-list", first)) {
//                        prikaz = new CiscoAccessList(pc, kon, slova, false);
//                        return;
//                    }
//                    if (kontrola("no", first)) {
//                        no();
//                        return;
//                    }
//                }
                break;

//            case CISCO_CONFIG_MODE:
//                if (kontrola("exit", first) || first.equals("end")) {
//                    mode = ROOT;
//                    kon.prompt = pc.jmeno + "#";
//                    Date d = new Date();
//                    cekej(100);
//                    kon.posliRadek(formator.format(d) + ": %SYS-5-CONFIG_I: Configured from console by console");
//                    return;
//                }
//                if (kontrola("ip", first)) {
//                    prikaz = new CiscoIp(pc, kon, slova, false, mode);
//                    return;
//                }
//                if (kontrola("interface", first)) {
//                    iface();
//                    return;
//                }
//                if (kontrola("access-list", first)) {
//                    prikaz = new CiscoAccessList(pc, kon, slova, false);
//                    return;
//                }
//                if (kontrola("no", first)) {
//                    no();
//                    return;
//                }
//                break;

//            case CISCO_CONFIG_IF_MODE:
//                if (kontrola("exit", first)) {
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
//                if (kontrola("ip", first)) {
//                    prikaz = new CiscoIp(pc, kon, slova, false, mode, aktualni);
//                    return;
//                }
//                if (kontrola("no", first)) {
//                    no();
//                    return;
//                }
//                if (kontrola("shutdown", first)) {
//                    shutdown();
//                    return;
//                }
//
//				break;
        }

//        if (debug) {
//            if (slova.get(0).equals("ifconfig")) { // pak smazat
//                prikaz = new LinuxIfconfig(pc, kon, slova);
//                return;
//            } else if (slova.get(0).equals("route")) {
//                prikaz = new LinuxRoute(pc, kon, slova);
//                return;
//            }
//        }
//
        if (isAmbiguousCommand) {
            isAmbiguousCommand = false;
            ambiguousCommand();
            return 0;
        }

        switch (mode) {
            case CISCO_CONFIG_MODE:
            case CISCO_CONFIG_IF_MODE:
                invalidInputDetected();
                break;

            default:
				shell.printLine("% Unknown command or computer name, or unable to find computer address");
        }


		return 0;
	}

	@Override
	public void catchUserInput(String userInput) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void catchSignal(int sig) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String[] getCommands(int mode) {
		// TODO: getCommands() poresit
		return new String[0];
	}

	/**
	 * Vrati
	 */
	public String getFormattedTime() {
		Date d = new Date();
		return formator.format(d);
	}

	/**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    private boolean isCommand(String command, String cmd) {

        int i = 10;

        // Zde jsou zadefinovany vsechny prikazy. Jsou rozdeleny do poli podle poctu znaku,
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

        if (command.equals("exit")) { // specialni chovani prikazu exit v ruznych stavech
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
        if (command.equals("ip")) { // specialni chovani prikazu ip v ruznych stavech
            switch (mode) {
                case CISCO_CONFIG_MODE:
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_IF_MODE:
                    i = 1;
            }
        }
        if (command.equals("route")) { // specialni chovani prikazu route v ruznych stavech
            switch (mode) {
                case CISCO_PRIVILEGED_MODE:
                    i = 2;
                    break;
                case CISCO_CONFIG_MODE:
                    i = 5;
            }
        }

        if (cmd.length() >= i && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
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
    private void invalidInputDetected() {
        shell.printLine("\n% Invalid input detected.\n");
    }

	/**
     * Vypise chybovou hlasku pri zadani nekompletniho prikazu.
     */
    private void incompleteCommand() {
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
	private void changeMode(int mode) {
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

	/**
     * Prepina cisco do stavu config (CONFIG).
     */
//    private void configure() {
//
//        if (slova.size() == 1 && !configure1) {
//            kon.posli("Configuring from terminal, memory, or network [terminal]? ");
//            kon.vypisPrompt = false;
//            configure1 = true;
//            return;
//        }
//
//        int cis = 1;
//        if (configure1) {
//            cis = 0;
//        }
//        if (!slova.get(cis).isEmpty() && ("memory".startsWith(slova.get(cis))
//                || "network".startsWith(slova.get(cis))
//                || slova.get(cis).equals("?"))) {
//            configureVypisChybu();
//            return;
//        }
//
//        if (kontrola("terminal", slova.get(cis)) || configure1) {
//            stav = CONFIG;
//            kon.prompt = pc.jmeno + "(config)#";
//            kon.posliRadek("Enter configuration commands, one per line.  End with 'exit'."); // zmena oproti ciscu: End with CNTL/Z.
//            configure1 = false;
//            kon.vypisPrompt = true;
//            return;
//        }
//
//        int pocet = pc.jmeno.length() + 1 + slova.get(0).length() + 1;
//        String ret = "";
//
//        for (int i = 0; i < pocet; i++) {
//            ret += " ";
//        }
//        ret += "^";
//        kon.posliRadek(ret);
//        kon.posliRadek("% Invalid input detected at '^' marker.\n");
//    }

//	/**
//     * Vypise chybu pri 'configure' a nastavi vypisovani promptu+zrusi configure1 flag
//     */
//    private void configureVypisChybu() {
//        kon.posliRadek("?Must be \"terminal\"");
//		kon.posliServisne("podporovan je pouze terminal");
//		configure1 = false;
//		kon.vypisPrompt = true;
//    }
}
