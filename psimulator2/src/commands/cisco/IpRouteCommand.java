/*
 * created 13.3.2012
 */

package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpRouteCommand extends CiscoCommand {

	private final boolean no;
	private AbstractCommand command;

	private IPwithNetmask adresat;
    private IpAddress brana;
    private NetworkInterface rozhrani;

	private final CiscoIPLayer ipLayer = (CiscoIPLayer)getNetMod().ipLayer;

	public IpRouteCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
	}

	protected boolean zpracujRadek() {

        if (no == true) {
//            if (debug) pc.vypis("prikaz no, pridej="+no);
            nextWord();
        }

        nextWord(); // route

        try {
            String adr = nextWord();
            String maska = nextWord();

            if (adr.isEmpty() || maska.isEmpty()) {
                incompleteCommand();
                return false;
            }
            adresat = new IPwithNetmask(adr, maska);
        } catch (Exception e) { // SpatnaMaskaException, SpatnaAdresaException
            invalidInputDetected();
            return false;
        }

        if (!adresat.isNetworkNumber()) {
            printLine("%Inconsistent address and mask");
            return false;
        }

        if (IpAddress.isForbiddenIP((adresat.getIp()))) {
            printLine("%Invalid destination prefix");
            return false;
        }

        String dalsi = nextWord();

        if (Util.zacinaCislem(dalsi)) { // na branu
            try {
                brana = new IpAddress(dalsi);
            } catch (BadIpException e) {
                invalidInputDetected();
                return false;
            }

            if (IpAddress.isForbiddenIP(brana)) {
                printLine("%Invalid next hop address");
                return false;
            }

        } else if (!dalsi.equals("")) { // na rozhrani
            String posledni = nextWord();
            dalsi += posledni; // nemuze byt null

            rozhrani = getNetMod().ipLayer.getNetworkIntefaceIgnoreCase(dalsi);
            if (rozhrani == null) { // rozhrani nenalezeno
                invalidInputDetected();
                return false;
            }

        } else { // prazdny
            if (no == false) {
                incompleteCommand();
                return false;
            }
        }

        if (!nextWord().equals("")) { // border za spravnym 'ip route <adresat> <maska> <neco> <bordel>'
            invalidInputDetected();
            return false;
        }

        return true;
    }

	@Override
	public void run() {

		boolean pokracovat = zpracujRadek();
        if (pokracovat) {
            vykonejPrikaz();
        }
	}
	public void vykonejPrikaz() {

//		if (debug) pc.vypis("pridej="+no);
        if (no == false) {
            if (brana != null) { // na branu
                ipLayer.wrapper.pridejZaznam(adresat, brana);
            } else { // na rozhrani
                if (rozhrani == null) {
                    return;
                }
                ipLayer.wrapper.pridejZaznam(adresat, rozhrani);
            }
        } else { // mazu
            int n = ipLayer.wrapper.smazZaznam(adresat, brana, rozhrani);
            if (n == 1) {
                printLine("%No matching route to delete");
            }
        }
	}
}
