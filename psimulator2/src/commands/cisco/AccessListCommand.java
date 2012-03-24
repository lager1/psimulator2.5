/*
 * created 19.3.2012
 */

package commands.cisco;

import commands.AbstractCommandParser;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import networkModule.L3.nat.NatTable;

/**
 * Trida pro zpracovani prikazu 'access-list 7 permit 1.1.1.0 0.0.0.31'.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class AccessListCommand extends CiscoCommand {

	private final NatTable natTable;
	private final boolean no;
	private int access;
    private IPwithNetmask adr;

	public AccessListCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;

		this.natTable = getNetMod().ipLayer.getNatTable();
	}

	@Override
	public void run() {
		boolean pokracovat = process();
        if (pokracovat) {
            start();
        }
	}

    private boolean process() {

//        if (no) {
//            if(!isCommand("access-list", nextWord(), 2)) {
//                return false;
//            }
//        }
//access-list 7 permit 1.1.1.0 0.0.0.31
        if (nextWordPeek().isEmpty()) {
            incompleteCommand();
            return false;
        }

        try {
            access = Integer.parseInt(nextWord());
        } catch (NumberFormatException e) {
            invalidInputDetected();
            return false;
        }

        if (no) {
            return true;
        }

        if (!isCommand("permit", nextWord(), 1)) {
            return false;
        }

		IpAddress adrTemp;
        IpNetmask wildcard;
        try {
            if (nextWordPeek().isEmpty()) {
                incompleteCommand();
                return false;
            }
            adrTemp = new IpAddress(nextWord());
            if (nextWordPeek().isEmpty()) {
                incompleteCommand();
                return false;
            }
            wildcard = IpNetmask.maskFromWildcard(nextWord());
        } catch (Exception e) {
            invalidInputDetected();
            return false;
        }

        adr = new IPwithNetmask(adrTemp, wildcard);

//        try {
//
//            adr.nastavMasku(maska);
//        } catch (Exception e) {
//            invalidInputDetected();
//            return false;
//        }

        return true;
    }

    private void start() {

        if (no) {
            natTable.lAccess.deleteAccessList(access);
        } else {
			natTable.lAccess.addAccessList(adr, access);
		}
    }
}
