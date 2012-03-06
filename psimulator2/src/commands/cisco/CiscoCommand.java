/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class CiscoCommand extends AbstractCommand {

	protected final CiscoCommandParser parser;
	/**
	 * nevim, k cemu to tu je
	 */
	protected boolean ambiguous = false;

	public CiscoCommand(AbstractCommandParser parser) {
		super(parser);
		this.parser = (CiscoCommandParser) parser;
	}

	protected void invalidInputDetected() {
		parser.invalidInputDetected();
	}

	protected void incompleteCommand() {
		parser.incompleteCommand();
	}

	protected void ambiguousCommand() {
		parser.ambiguousCommand();
	}

	 /**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * Metoda se take stara o vypisy typu: IncompleteCommand, AmbigiousCommand, InvalidInputDetected.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @param min kolik musi mit mozny prikaz znaku
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    protected boolean kontrola(String command, String cmd, int min) {

        if (cmd.length() == 0) {
            incompleteCommand();
            return false;
        }

        if (cmd.length() >= min && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }

        if (command.startsWith(cmd)) {
            ambiguousCommand();
        } else {
            invalidInputDetected();
        }
        return false;
    }

    /**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco.
     * Metoda se take stara o vypis: AmbigiousCommand.
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit.
     * @param cmd prikaz, ktery zadal uzivatel
     * @param min kolik musi mit mozny prikaz znaku
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    protected boolean kontrolaBezVypisu(String command, String cmd, int min) {

        if (cmd.length() == 0) {
            return false;
        }

        if (cmd.length() >= min && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }

        if (command.startsWith(cmd)) {
            ambiguousCommand();
            ambiguous = true;
        }
        return false;
    }
}
