/*
 * created 17.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.nat.NatTable;

/**
 * Trida pro zpracovani prikazu: <br />
 * ip nat pool ovrld 172.16.10.1 172.16.10.1 prefix 24 <br />
 * ip nat inside source list 7 pool ovrld overload
 * ip nat inside source static 10.10.10.2 171.16.68.5
 *
 * ip nat pool ovrld 172.16.0.1 172.16.0.1 netmask 255.255.255.252  - not implemented
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 *
 */
public class IpNatCommand extends CiscoCommand {

	private final boolean no;

	int poolPrefix = -1;
    IpAddress start;
    IpAddress konec;
    String poolJmeno;
    int accesslist = -1;
    boolean overload = false;
    State stav;
	private NatTable natTable;

    private enum State {

        POOL, // ip nat pool ovrld 172.16.10.1 172.16.10.1 prefix 24
        INSIDE, // ip nat inside source list 7 pool ovrld overload?
        STATIC // ip nat inside source static 10.10.10.1 171.16.68.5
    }

	public IpNatCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;

		this.natTable = getNetMod().ipLayer.getNatTable();
	}

	@Override
	public void run() {
		boolean pokracovat = zpracujRadek();
        if (pokracovat) {
            vykonejPrikaz();
        }
	}

	private void ladici(String s) { // TODO: smazat
		Logger.log("IpNatCommand: ", Logger.DEBUG, LoggingCategory.CISCO_COMMAND_PARSER, s);
	}

	private boolean zpracujRadek() {

        // ip nat pool ovrld 172.16.10.1 172.16.10.1 prefix 24
        // ip nat inside source list 7 pool ovrld overload?
        // ip nat inside source static 10.10.10.1 171.16.68.5

        String dalsi = nextWord();
        if (dalsi.startsWith("p")) {
            if (kontrola("pool", dalsi, 1)) {
                return zpracujPool();
            }
            return false;
        } else {
            if (kontrola("inside", dalsi, 1)) {
                return zpracujInside();
            }
            if (dalsi.startsWith("outside")) {
                printService("Tato funkcionalita neni implementovana.");
            }
            return false;
        }
    }

    private void vykonejPrikaz() {

        int n;
        if (no) {
            if (stav == State.INSIDE) { // no ip nat inside source list 7 pool ovrld overload?
                n = natTable.lPoolAccess.smazPoolAccess(accesslist);
                if (n == 1) {
                    printLine("%Dynamic mapping not found");
                }
                return;
            }
            if (stav == State.POOL) { // no ip nat pool ovrld 172.16.10.1 172.16.10.1 prefix 24
                n = natTable.lPool.smazPool(poolJmeno);
                if (n == 1) {
                    printLine("%Pool " + poolJmeno + " not found");
                }
                if (n == 2) {
                    printLine("%Pool " + poolJmeno + " in use, cannot redefine");
                }
				return;
            }

            if (stav == State.STATIC) {
                n = natTable.deleteStaticRule(start, konec);
                if (n == 1) {
                    printLine("% Translation not found");
                }
            }

            return;
        }

        if (stav == State.POOL) { // ip nat pool ovrld 172.16.10.1 172.16.10.1 prefix 24
            int ret = natTable.lPool.pridejPool(start, konec, poolPrefix, poolJmeno);
            switch (ret) {
                case 0:
                    // ok
                    break;
                case 1:
                    printLine("%End address less than start address");
                    break;
                case 2:
                    printLine("%Pool ovrld in use, cannot redefine");
                    break;
                case 3:
                    invalidInputDetected();
                    break;
                case 4:
                    printLine("%Start and end addresses on different subnets");
                    break;
                default:
                    invalidInputDetected();
            }
            return;
        }

        if (stav == State.INSIDE) { // ip nat inside source list 7 pool ovrld overload
            natTable.lPoolAccess.pridejPoolAccess(accesslist, poolJmeno, overload);
			return;
        }

        if (stav == State.STATIC) { // ip nat inside source static 10.10.10.2 171.16.68.5
            n = natTable.addStaticRuleForCisco(start, konec);
            if (n == 1) {
                printLine("% " + start + " already mapped (" + start + " -> "
                        + konec + ")");
            }
            if (n == 2) {
                printLine("% similar static entry (" + start + " -> " + konec + ") "
                        + "already exists");
            }
        }
    }

    /**
     * Vrati true, pokud parsovani dobre dopadlo.
     * @return
     */
    private boolean zpracujPool() {
        // ip nat pool ovrld | 172.16.10.1 172.16.10.1 prefix 24

        String dalsi = nextWord();
        if (jePrazdny(dalsi)) {
            return false;
        }
        poolJmeno = dalsi;

        stav = State.POOL;

//        ladici("tady10");
        if (no) { // staci po jmeno, dal me to nezajima
            return true;
        }
//        ladici("tady11");
        try {
            dalsi = nextWord();
            if (jePrazdny(dalsi)) {
                return false;
            }
            start = new IpAddress(dalsi);

            dalsi = nextWord();
            if (jePrazdny(dalsi)) {
                return false;
            }
            konec = new IpAddress(dalsi);
        } catch (Exception e) {
            invalidInputDetected();
            return false;
        }

        dalsi = nextWord();

        if(dalsi.startsWith("n")) {
            if (!kontrola("netmask", dalsi, 1)) {
                return false;
            }
            printService("netmask neni implementovan; pouzijte volbu prefix-length");
            return false;
        }

        if (!kontrola("prefix-length", dalsi, 1)) {
            return false;
        }

        dalsi = nextWord();
        if (jePrazdny(dalsi)) {
            return false;
        }
        try {
            poolPrefix = Integer.parseInt(dalsi);
        } catch (NumberFormatException e) {
            invalidInputDetected();
            return false;
        }
        if (poolPrefix > 30) {
            printLine("%Pool " + poolJmeno + " prefix length " + poolPrefix + " too large; should be no more than 30");
            return false;
        } else if (poolPrefix < 1) {
            invalidInputDetected();
            return false;
        }

        if (!nextWord().equals("")) { // kdyz je jeste neco za tim
            invalidInputDetected();
            return false;
        }

        return true;
    }

    /**
     * Vrati true, pokud parsovani dobre dopadlo.
     * @return
     */
    private boolean zpracujInside() {
        // ip nat inside source list 7 pool ovrld overload
        // ip nat inside source static 10.10.10.1 171.16.68.5

        String dalsi;

        if (!kontrola("source", nextWord(), 1)) {
            return false;
        }

        dalsi = nextWord();
        if (dalsi.startsWith("s")) {
            return zpracujStatic(dalsi);
        }

        if (!kontrola("list", dalsi, 1)) {
            return false;
        }

        dalsi = nextWord();
        try {
            if (jePrazdny(dalsi)) {
                return false;
            }
            accesslist = Integer.parseInt(dalsi);
        } catch (NumberFormatException e) {
            invalidInputDetected();
            return false;
        }

        if (!kontrola("pool", nextWord(), 1)) {
            return false;
        }

        dalsi = nextWord();
        if (jePrazdny(dalsi)) {
            return false;
        }
        poolJmeno = dalsi;

        stav = State.INSIDE;

        if (no) { // pokud mazu, tak pocamcad mi to staci, ale mazu stejnak jen podle cisla:-)
            return true;
        }

        dalsi = nextWord();
        if (dalsi.equals("overload")) {
            overload = true;
        } else if (dalsi.equals("")) {
            // nic, parametr overload je volitelny
            return true;
        } else {
            invalidInputDetected();
            return false;
        }

        dalsi = nextWord();
        if (!dalsi.equals("")) {
            invalidInputDetected();
            return false;
        }

        return true;
    }

    private boolean zpracujStatic(String s) {
        // ip nat inside source static 10.10.10.2 171.16.68.5
        String dalsi = s;
        if (!kontrola("static", dalsi, 1)) {
            return false;
        }

        try {
            dalsi = nextWord();
            if (jePrazdny(dalsi)) {
                return false;
            }
            start = new IpAddress(dalsi);

            dalsi = nextWord();
            if (jePrazdny(dalsi)) {
                return false;
            }
            konec = new IpAddress(dalsi);
        } catch (Exception e) {
            invalidInputDetected();
            return false;
        }
        stav = State.STATIC;

        return true;
    }
}
