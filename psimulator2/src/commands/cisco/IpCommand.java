/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class IpCommand extends CiscoCommand {

	private final boolean no;
	private AbstractCommand command;
	private final int state;

	public IpCommand(AbstractCommandParser parser, boolean no) {
		super(parser);
		this.no = no;
		this.state = parser.getShell().getMode();
	}

	@Override
	public void run() {

		String dalsi;

		dalsi = nextWord(); // route, classless, nat, address

        if(dalsi.isEmpty()) {
            incompleteCommand();
            return;
        }

        if (state == CommandShell.CISCO_CONFIG_MODE) {

			if (debug) {
				if (kontrolaBezVypisu("route", dalsi, 5)) {
					command = new IpRouteCommand(parser, no);
					command.run();
					return;
				}
			}

            if (kontrolaBezVypisu("nat", dalsi, 3)) {
                command = new IpNatCommand(parser, no);
				command.run();
                return;
            }

            if (kontrolaBezVypisu("classless", dalsi, 2)) {
                if (no) {
					getNetMod().ipLayer.routingTable.classless = false;
                } else {
					getNetMod().ipLayer.routingTable.classless = true;
                }
                return;
            }
        }

        if (state == CommandShell.CISCO_CONFIG_IF_MODE) {
            if (kontrolaBezVypisu("address", dalsi, 3)) {
                command = new IpAddressCommand(parser, no);
				command.run();
                return;
            }

            if (kontrolaBezVypisu("nat", dalsi, 2)) {
				command = new IpNatInterfaceCommand(parser, no);
				command.run();
                return;
            }
        }

        if (dalsi.length() != 0 && ambiguous == false) { // jestli to je prazdny, tak to uz vypise kontrolaBezVypisu
            invalidInputDetected();
        }
	}
}
