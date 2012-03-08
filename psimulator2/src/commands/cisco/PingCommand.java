/*
 * created 8.3.2012
 */

package commands.cisco;

import applications.CiscoPingApplication;
import commands.AbstractCommandParser;
import commands.ApplicationNotifiable;
import dataStructures.ipAddresses.IpAddress;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PingCommand extends CiscoCommand implements ApplicationNotifiable {

	private final CiscoPingApplication app;

	public PingCommand(AbstractCommandParser parser) {
		super(parser);
		this.app = new CiscoPingApplication(getDevice(), this.parser, this);
	}

	@Override
	public void catchUserInput(String input) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void run() {
		parser.runningCommand = this;

		boolean conti = zpracujRadek();
		if (conti) {
			// vytvor aplikaci
			app.run();
		} else {
			parser.runningCommand = null;
		}
	}

	/**
     * Ulozi vsechny parametry do tridnich promennych nebo vypise chybovou hlasku.
     * @param typVolby, ktery ma zpracovavat
     * @return true pokud se ma pokracovat v posilani pingu
     *         false pokud to vypsalo chybu a tedy uz nic nedelat
     */
    private boolean zpracujParametry(String typVolby) {
        if (!kontrola("timeout", typVolby) && !kontrola("repeat", typVolby) && !kontrola("size", typVolby)) {
            printLine("% Invalid input detected.");
            return false;
        }

        String volba = nextWord();

        if (volba.equals("")) {
            printLine("% Incomplete command.");
            return false;
        }


        if (kontrola("timeout", typVolby)) {
			int timeout;
            try {
                timeout = Integer.valueOf(volba) * 1000;
            } catch (NumberFormatException e) {
                printLine("% Invalid input detected.");
                return false;
            }
			app.setTimeout(timeout);
        }


        if (kontrola("repeat", typVolby)) {
			int count;
            try {
                count = Integer.valueOf(volba);
            } catch (NumberFormatException e) {
                printLine("% Invalid input detected.");
                return false;
            }
			app.setCount(count);
        }
        if (kontrola("size", typVolby)) {
            int n;
            try {
                n = Integer.valueOf(volba);
            } catch (NumberFormatException e) {
                printLine("% Invalid input detected.");
                return false;
            }
            if (n < 36 || n > 18024) {
                printLine("% Invalid input detected.");
                return false;
            }
            app.setSize(n);
        }

        typVolby = nextWord();
        if (! typVolby.equals("")) {
            return zpracujParametry(typVolby);
        }

        return true;
    }

    /**
     * Parsuje prikaz ping.
     * @return
     */
    private boolean zpracujRadek() {
//        if (parser.words.size() < 2) {
		if (nextWordPeek().isEmpty()) { // TODO: overit funkcnost

            printService("podporovana syntaxe: <IP> (<size|timeout|repeat> <cislo>)* ");
            return false;
        }
        String ip = nextWord();
		IpAddress target;
        try {
            target = new IpAddress(ip);
        } catch (Exception e) {
			printWithDelay("Translating \"" + ip + "\"" + "...domain server (255.255.255.255)\n"
					+ "% Unrecognized host or address, or protocol not running.", 500);
            return false;
        }
		app.setTarget(target);

        String typVolby = nextWord();
        if (typVolby.equals("")) {
            return true;
        }

        return zpracujParametry(typVolby);
    }

	/**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    private boolean kontrola(String command, String cmd) {

        int n = 1;
        if (command.equals("size")) n = 2;

        if (cmd.length() >= n && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }
        return false;
    }

	@Override
	public void applicationFinished() {
		parser.runningCommand = null;
	}

}
