/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Tomas Pitrinec
 */
public class Man extends AbstractCommand {

	public Man(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		vykonejPrikaz();
	}


	

	protected void vykonejPrikaz() {
        parser.printService("Manualove stranky nejsou v simulatoru dostupne. Doporucuji pouzit manualove " +
                "stranky na webu, napriklad: http://linux.die.net/man/, nebo pouzit google.");

        parser.printService("Seznam prikazu implementovanych v tomto pocitaci vypisete " +
                "zvlastnim prikazem help.");
    }

}
