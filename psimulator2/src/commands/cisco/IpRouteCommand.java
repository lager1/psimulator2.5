/*
 * created 13.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import dataStructures.ipAddresses.BadIpException;
import dataStructures.ipAddresses.BadNetmaskException;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import networkModule.L3.CiscoIPLayer;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 * Class for parsing this commands:
 *
 * ip route 'target' 'mask of target' 'gateway or interface
 * '
 * ip route 0.0.0.0 0.0.0.0 192.168.2.254
 * ip route 192.168.2.0 255.255.255.192 fastEthernet 0/0
 * no ip route ...
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpRouteCommand extends CiscoCommand {

	private final boolean no;

	private IPwithNetmask adresat;
    private IpAddress brana;
    private NetworkInterface rozhrani;

	private final CiscoIPLayer ipLayer = (CiscoIPLayer)getNetMod().ipLayer;

	public IpRouteCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
	}

	private boolean process() {

		String adr = "";
		String maska = "";
        try {
            adr = nextWord();
			debug("address: "+adr);

            maska = nextWord();
			debug("mask: "+maska);

            if (adr.isEmpty() || maska.isEmpty()) {
                incompleteCommand();
                return false;
            }
            adresat = new IPwithNetmask(adr, maska);
        } catch (BadNetmaskException e) {
			System.out.println("spatna maska: "+maska);
			invalidInputDetected();
			return false;
		} catch (BadIpException e) { // SpatnaMaskaException, SpatnaAdresaException
			System.out.println("spatna adresa: "+adr);
            invalidInputDetected();
            return false;
        } catch (Exception e) {
			System.out.println(e);
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
		debug("dalsi: "+dalsi);

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

		boolean cont = process();
        if (cont) {
            start();
        }
	}

	private void start() {

//		if (debug) pc.vypis("pridej="+no);
        if (no == false) {
            if (brana != null) { // na branu
                ipLayer.wrapper.addRecord(adresat, brana);
            } else { // na rozhrani
                if (rozhrani == null) {
                    return;
                }
                ipLayer.wrapper.addRecord(adresat, rozhrani);
            }
        } else { // mazu
            int n = ipLayer.wrapper.deleteRecord(adresat, brana, rozhrani);
            if (n == 1) {
                printLine("%No matching route to delete");
            }
        }
	}
}
