/*
 * Erstellt am 26.10.2011.
 */

package psimulator2;

import utils.TestLogger;

/**
 *
 * @author neiss
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        TestLogger.logMessage("testovací zpráva", TestLogger.TYPE.DEBUG, TestLogger.SOURCE.TELNET);


		Psimulator vsechno = Psimulator.getPsimulator();
    }

}
