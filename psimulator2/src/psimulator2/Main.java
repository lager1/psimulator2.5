/*
 * Erstellt am 26.10.2011.
 */
package psimulator2;

import config.AbstractNetwork.Network;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import logging.Logger;
import logging.LoggingCategory;
import telnetd.BootException;
import telnetd.TelnetD;
import telnetd.pridaneTridy.TelnetProperties;

/**
 *
 * @author neiss
 */
public class Main {

    public static String configFileName;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Psimulator psimulatorInstance = Psimulator.getPsimulator();


        Network networkModel = null;
        try {
            networkModel = Network.load(configFileName);
        } catch (JAXBException ex) {
            Logger.log(Logger.DEBUG, LoggingCategory.ABSTRACT_NETWORK, ex.toString());
            Logger.log(Logger.ERROR, LoggingCategory.ABSTRACT_NETWORK, "Cannot load network model form:" + configFileName);

        }


        TelnetD telnetDaemon;
        try {
            Logger.log(Logger.INFO, LoggingCategory.TELNET, "Starting telnet listeners");
            TelnetProperties properties = new TelnetProperties(networkModel.getDevices().values());
            telnetDaemon = TelnetD.createTelnetD(properties.getProperties());
            telnetDaemon.start();
            Logger.log(Logger.INFO, LoggingCategory.TELNET, "Telnet listeners successfully started");

        } catch (BootException ex) {
            Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
            Logger.log(Logger.ERROR, LoggingCategory.TELNET, "Error occured when creating telnet servers.");
        }


    }
}
