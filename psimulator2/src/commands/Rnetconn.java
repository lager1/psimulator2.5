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

		String prikaz = nextWord();

		if (prikaz.equals("")) {
			parser.printService("This command is'n present on real cisco or linux device. It's in this simulator to manage connection to real network.\n");
			printHelp();
		} else if (prikaz.equals("list")) {
			listAllRealSwitchports();
		} else if (prikaz.equals("help")) {
			printHelp();
		} else if (prikaz.equals("connect")) {
			connectSwitchport();
		} else{
			printLine("Unsupported command: "+prikaz);
			printHelp();
		}

		printLine("");	// na konci odradkovani
	}

	/**
	 * Najde a vypise vsechny realny switchporty v systemu.
	 */
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

	/**
	 * Vypise nastaveni jednoho realnyho switchportu.
	 * @param dev
	 * @param swport
	 */
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
		printLine("Tady se bude vypisovat napoveda.");	// TODO vypsat napovedu
	}

	/**
	 * Zparsuje a provede prikazy connect, tedy pripojeni rozhrani na realnej switchport.
	 */
	private void connectSwitchport() {

		// zparsovani:
		String deviceName = nextWord();
		int switchportNumber;
		try{
			switchportNumber = Integer.parseInt(nextWord());
		} catch (NumberFormatException ex){
			printLine("Bad switchport number.");
			printHelp();
			return;
		}
		String ifaceName = nextWord();

		// hledani pocitace:
		Device dev = Psimulator.getPsimulator().getDeviceByName(deviceName);
		if(dev==null){
			printLine("Device "+deviceName+" doesn't exist in this simulated network.");
			printHelp();
			return;
		}

		//hledani switchportu:
		Switchport swport = dev.physicalModule.getSwitchports().get(switchportNumber);
		if(swport==null){
			printLine("On "+deviceName+" doesn't exist switchport number "+switchportNumber);
			printHelp();
			return;
		} else if(!swport.isReal()){
			printLine("Switchport "+switchportNumber+" on "+deviceName+" is only simulator switchport, it can not be connected to real interface.");
			printHelp();
			return;
		}

		// vsechno je v poradku, jde se pripojit:
		RealSwitchport rport = (RealSwitchport)swport;
		int navrKod = rport.start(ifaceName);
		if(navrKod!=0){
			printLine("An error occured while trying to connect to interface. For more informations see the programms main console.");
		}else{
			printLine("Switchport was connected.");
		}

	}

}
