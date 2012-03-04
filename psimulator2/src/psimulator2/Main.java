/*
 * Erstellt am 26.10.2011.
 */
package psimulator2;

import config.Components.NetworkModel;
import config.Serializer.AbstractNetworkSerializer;
import config.Serializer.NetworkModelSerializerXML;
import config.Serializer.SaveLoadException;
import java.io.File;
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

		AbstractNetworkSerializer serializer = new NetworkModelSerializerXML();

		NetworkModel networkModel = null;
		try {

			networkModel = serializer.loadNetworkModelFromFile(new File(args[0]));

		} catch (SaveLoadException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.ABSTRACT_NETWORK, ex.toString());
			Logger.log(Logger.ERROR, LoggingCategory.ABSTRACT_NETWORK, "Cannot load network model form:" + configFileName);

		}

		int firstTelnetPort = 11000;
		TelnetProperties.setStartPort(firstTelnetPort);

		// SEM PŘIDEJTE VYTVÁŘENÍ Device Z networkModel. 
		// v KONSTRUKTORU DEVICE VOLÁM TelnetProperties a věci z toho používám níže, tak proto to dejte sem. Pak tohle smažte!!


		TelnetD telnetDaemon;

		Logger.log(Logger.INFO, LoggingCategory.TELNET, "Starting telnet listeners");
		try {

			telnetDaemon = TelnetD.createTelnetD(TelnetProperties.getProperties());
			telnetDaemon.start();

			Logger.log(Logger.INFO, LoggingCategory.TELNET, "Telnet listeners successfully started");

		} catch (BootException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
			Logger.log(Logger.ERROR, LoggingCategory.TELNET, "Error occured when creating telnet servers.");
		}




	}
}
