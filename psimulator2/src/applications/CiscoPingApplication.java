/*
 * created 8.3.2012
 */

package applications;

import commands.ApplicationNotifiable;
import commands.cisco.CiscoCommandParser;
import dataStructures.IcmpPacket;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;
import utils.Util;

/**
 *
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoPingApplication extends PingApplication {

	private final CommandShell shell;
	private final CiscoCommandParser parser;

	public CiscoPingApplication(Device device, CiscoCommandParser parser, ApplicationNotifiable command) {
		super(device, command);
		this.parser = parser;
		this.shell = parser.getShell();
		this.count = 5;
		this.timeout = 1_000; // default is 2_000
		this.waitTime = 50;
	}

	@Override
	public String getDescription() {
		return device.getName() + ": cisco" + getName() +" application";
	}

	@Override
	protected void startMessage() {
		String s = "";
        s += "\nType escape sequence to abort.\n"
                + "Sending " + count + ", " + size + "-byte ICMP Echos to " + target + ", timeout is " + timeout / 1000 + " seconds:";

        shell.printWithDelay(s, 20);
	}

	@Override
	protected void handleIncommingPacket(IcmpPacket packet) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, getName()+" handleIncommingPacket, type="+packet.type+", code="+packet.code+", seq="+packet.seq, packet);

		areAllAtHome(packet);

		switch (packet.type) {
			case REPLY:
				shell.print("!"); // ok
				break;
			case TIME_EXCEEDED:
				// TODO: Time To Live Exceeded
				shell.print(".");
				break;
			case UNDELIVERED:
				switch (packet.code) {
					case NETWORK_UNREACHABLE:
						// cisco posila 'U' a '.', jak se mu chce
						if (Math.round(Math.random()) % 2 == 0) {
							shell.print("U");
						} else {
							shell.print(".");
						}
						break;
					case HOST_UNREACHABLE:
						shell.print(".");
						break;
					default:
						shell.print(".");
						// TODO: jeste pridat na ostatni code reakci
				}
				break;

			default:
			// jeste tu je REQUEST, ten se sem ale nikdy nedostane
			// zalogovat neznamy typ ICMP ?
		}
	}

	@Override
	public void printStats() {
		String s;
		s = "\nSuccess rate is " + stats.uspech + " percent (" + stats.prijate + "/" + stats.odeslane + ")";
        if (stats.prijate > 0) {
            s += ", round-trip min/avg/max = " + Math.round(stats.min) + "/" + Math.round(stats.avg) + "/" + Math.round(stats.max) + " ms";
        }
        shell.printWithDelay(s, 10);
	}

	private void areAllAtHome(IcmpPacket packet) {
		recieved[packet.seq - 1] = true;
		for (int i = 0; i < recieved.length; i++) {
			if (recieved[i] == false) {
				return;
			}
		}
	}
}
