/*
 * Erstellt am 21.3.2012.
 */

package commands;

import device.Device;
import physicalModule.RealSwitchport;
import physicalModule.Switchport;
import psimulator2.Psimulator;

/**
 * This command is used to manage connections from simulator to real network.
 * @author Tomas Pitrinec
 */
public class Rnetconn extends AbstractCommand {

	public Rnetconn(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		parsujAVykonejPrikaz();
	}

	private void parsujAVykonejPrikaz(){
		parser.printService("This command is'n present on real cisco or linux device. It's in this simulator to manage connection to real network. For help type \"rnetconn help\".");

		if(nextWord().equals("list")){
			listAllRealSwitchports();
		}else if(nextWord().equals("help")){
			printHelp();
		}
	}

	private void listAllRealSwitchports() {
		printLine("This is the list of all real switchports on all devices in simulated network. Real switchport is the swichport on simulated network device, that can be connect to real computer.");
		for(Device dev: Psimulator.getPsimulator().devices){
			for(Switchport swport: dev.physicalModule.getSwitchports().values()){
				if(swport.isReal()){
					printSwitchportSettings(dev,(RealSwitchport) swport);
				}
			}
		}
	}

	private void printSwitchportSettings(Device dev, RealSwitchport swport){
		String vratit = dev.getName()+"    switchport no. "+swport.number+"    ";
		if(swport.isConnected()){
			vratit+="connected     "+swport.getIfaceName();
		} else {
			vratit +="not connected";
		}
		printLine(vratit);
	}

	private void printHelp() {
		throw new UnsupportedOperationException("Not yet implemented");	// TODO vypsat napovedu
	}

}
