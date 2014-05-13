/*
 * Erstellt am 21.3.2012.
 */

package commands;

import device.Device;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import org.jnetpcap.PcapIf;
import physicalModule.RealSwitchport;
import physicalModule.Switchport;
import psimulator2.Psimulator;

/**
 * This command is used to manage connections from simulator to real network.
 * @author Tomas Pitrinec
 * @author lager1
 */
public class Rnetconn extends AbstractCommand {
    private List<PcapIf> alldevs;
    //private DataLayerFacade dataLayer;
    
	public Rnetconn(AbstractCommandParser parser) {
		super(parser);
	}

	@Override
	public void run() {
		parsujAVykonejPrikaz();
	}

	private void parsujAVykonejPrikaz(){

		String prikaz = nextWord();

                if (prikaz.equals("")) {
			parser.printService("This command isn't present on real cisco or linux device. It's in this simulator to manage connection to real network.");
			printHelp();
		} 
                else if (prikaz.equals("on")) {
                    connectToRealNetwork();
                }                
                else if (prikaz.equals("off")) {
                    disconnectFromRealNetwork();
                }                
		else if (prikaz.equals("help")) {
			printHelp();
		} else if (prikaz.equals("help-cz")) {
			printHelpCz();
		} else{
			printLine("Unsupported command: "+prikaz);
			printHelp();
		}
                
	}

        private Device getRealDevice() {
            for (Device dev : Psimulator.getPsimulator().devices) {
                for (Switchport swport : dev.physicalModule.getSwitchports().values()) {
                    if (swport.isReal()) {
                        return dev;
                    }
                }
            }
            
            return null;
        }

        private RealSwitchport getRealPort(Device dev) {
                for (Switchport swport : dev.physicalModule.getSwitchports().values()) {
                    if (swport.isReal()) {
                        return (RealSwitchport) swport;
                    }
                }
                
            return null;
        }

	private void printHelp() {
		printLine("");
		printLine("Usage:");
		printLine("This command is not present on real linux or cisco device. It's only command of this simulator to manage connection to real network.");
		printLine("The command can manage all real switchports on all simulated devices in virtual network.");
		printLine("SYNOPSIS: rnetconn command options");
		printLine("  The possible commands are:");
		printLine("    rnetconn on                   connects simulated network with real network.");
		printLine("    rnetconn off                  disconnects simulated network from real network.");
		printLine("    help                          prints this help.");
		printLine("    help-cz                       prints this help in czech.");
		printLine("");
	}

	private void printHelpCz() {
		printLine("");
		printLine("Pouziti:");
		printLine("Tento prikaz neexistuje na realnem cisco ani linux pocitaci. Je implementovan pouze v tomto simulatoru ke sprave pripojeni na realnou sit.");
		printLine("Timto prikazem je mozno obsluhovat realna pripojeni vsech virtualnich sitovych zarizeni v simulatoru.");
		printLine("");
		printLine("PREHLED: moznosti prikazu rnetconn");
		printLine("  Mozne prikazy jsou:");
		printLine("    rnetconn on                   propoji simulovanou sit s realnou siti.");
		printLine("    rnetconn off                  odpoji simulovanou sit od realne site.");
		printLine("    help                          vypise napovedu v anglictine.");
		printLine("    help-cz                       vypise napovedu v cestine.");
		printLine("");
	}
        
	/**
	 * provede tedy pripojeni rozhrani na realnej switchport.
	 */
	private boolean connectSwitchport(Device d, RealSwitchport p, String realInterface) {

                if(p.isConnected()){
                    return false;
		}
                
                int navrKod = p.start(realInterface);       // interface selected in frontend in options of real pc
		if (navrKod == 1) {
                    printLine("An error occured while trying to connect to interface. For more informations see the programms main console.");
                    return false;
		} else if (navrKod == 2) {
                    printLine("An error occured while trying to connect to interface. For more informations see the programms main console.");
                    // should not happen
                    return false;
		} else {
                    return true;
		}
	}

	/**
	 * provede odpojeni rozhrani od realneho switchportu.
	 */
	private boolean disconnectSwitchport(Device d, RealSwitchport p) {
            if(!p.isConnected()){
                return false;
            }

            p.stop();
            return true;
        }

        
        // returns the name of the selected real interface
        private String getRealInterface() {
            File real;

            File f = new File(System.getProperty("java.io.tmpdir"));
            File[] matchingFiles = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("realNetConnection") && name.endsWith("sim");    // defined file name
                }
            });

            if(matchingFiles == null) {
                return null;    // no interface selected in frontend
            }

            long age = matchingFiles[0].lastModified();
            real = matchingFiles[0];

            for(File fe : matchingFiles) {  // find the newest one
                if(fe.lastModified() > age)
                    real = fe;
            }

            String realInterface = null;

            BufferedReader br = null; 
            try {
                String line;
                br = new BufferedReader(new FileReader(real));

                while ((line = br.readLine()) != null) {    // read the content
                        realInterface = line;
                }

            } catch (IOException e) {
                    e.printStackTrace();
            } finally {
                try {
                        if (br != null)br.close();
                } catch (IOException ex) {
                        ex.printStackTrace();
                }
            }

            return realInterface;
        }

    
        private void disconnectFromRealNetwork() {
            Device device;
            RealSwitchport realport;

            device = getRealDevice();

            if(device == null) {
                printLine("error getting device with real switchport.");
                return;
            }

            realport = getRealPort(device);

            if(realport == null) {
                printLine("error getting real switchport.");
                return;
            }

            if(realport.isConnected() == false) {
                printLine("not connected to real network.");
                return;
            }

            if(disconnectSwitchport(device, realport) == true)
                printLine("successfully disconnected from real network.");
            else
                printLine("disconnecting from real network failed.");
       }


        private void connectToRealNetwork() {
            Device device;
            RealSwitchport realport;

            device = getRealDevice();

            if(device == null) {
                printLine("error getting device with real switchport.");
                return;
            }

            realport = getRealPort(device);

            if(realport == null) {
                printLine("error getting real switchport.");
                return;
            }

            if(realport.isConnected() == true) {
                printLine("already connected to real network.");
                return;
            }

            String realInterface = getRealInterface();
            if(realInterface == null) {
                printLine("no interface selected in properties of real network.");
                return;
            }

            if(connectSwitchport(device, realport, realInterface) == true)
                printLine("successfully connected to real network.");
            else
                printLine("connection to real network failed.");
        }
}
