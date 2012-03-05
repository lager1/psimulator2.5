/*
 * Erstellt am 26.10.2011.
 */
package psimulator2;

import config.Components.NetworkModel;
import config.Serializer.AbstractNetworkSerializer;
import config.Serializer.NetworkModelSerializerXML;
import config.Serializer.SaveLoadException;
import config.configTransformer.Loader;
import java.io.File;
import logging.Logger;
import logging.LoggingCategory;
import telnetd.BootException;
import telnetd.TelnetD;
import telnetd.pridaneTridy.TelnetProperties;

/**
 *
 * @author Tomáš Pitřinec
 * @author Martin Lukáš
 */
public class Main {

	public static String configFileName;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			Logger.log(Logger.ERROR, LoggingCategory.ABSTRACT_NETWORK,
					"No configuration file attached, run again with configuration file as first argument.");
		}

		configFileName = args[0];

		int firstTelnetPort = 11000;
		if (args.length >= 2) {
			try {
				firstTelnetPort = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				Logger.log(Logger.ERROR, LoggingCategory.ABSTRACT_NETWORK, "Second argument is not port number: "+args[1]);
			}
		}

		AbstractNetworkSerializer serializer = new NetworkModelSerializerXML();	// vytvori se serializer

		NetworkModel networkModel = null;
		try {

			networkModel = serializer.loadNetworkModelFromFile(new File(configFileName));	// nacita se xmlko do ukladacich struktur

		} catch (SaveLoadException ex) {
			ex.printStackTrace();
			Logger.log(Logger.DEBUG, LoggingCategory.ABSTRACT_NETWORK, ex.toString());
			Logger.log(Logger.ERROR, LoggingCategory.ABSTRACT_NETWORK, "Cannot load network model from: " + configFileName);
		}

		TelnetProperties.setStartPort(firstTelnetPort);

		Loader loader = new Loader(networkModel);	// vytvari se simulator loader
		loader.loadFromModel();	// simulator se startuje z tech ukladacich struktur


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
