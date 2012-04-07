/*
 * created 6.4.2012
 */
package applications;

import commands.cisco.TracerouteCommand;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class CiscoTracerouteApplication extends TracerouteApplication {

	TracerouteCommand cmd;

	public CiscoTracerouteApplication(Device device, TracerouteCommand command) {
		super(device, command);
		this.cmd = command;
	}

	@Override
	public String getDescription() {
		return device.getName() + ": traceroute_app_cisco";
	}

	@Override
	protected void startMessage() {
		cmd.printLine("Type escape sequence to abort.");
		cmd.printLine("Tracing the route to " + target);
		cmd.printLine("");
	}

	@Override
	protected void lineBeginning(int ttl, String address) {
		cmd.print(ttl + " "+ address + " ");
	}

	@Override
	protected void printPacket(TracerouteApplication.Record record) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Vypisuju paket seq=", record.packet.seq);
		if (record.delay == null) { // prints arrived timeout
			cmd.print("* ");
		} else {
			switch (record.packet.type) {
				case REPLY:
				case TIME_EXCEEDED:
					cmd.print(Math.round(record.delay) + " msec ");
					break;
				case UNDELIVERED:
					switch (record.packet.code) {
						case PORT_UNREACHABLE:
							cmd.print(Math.round(record.delay) + " msec ");
							break;
//						case FRAGMENTAION_REQUIRED:
//							cmd.print("F "); // podle dokumentace se asi nevypisuje
//							break;
						case HOST_UNREACHABLE:
							cmd.print("H ");
							break;
						case ZERO:
							cmd.print("N ");
							break;
						case PROTOCOL_UNREACHABLE:
							cmd.print("P ");
							break;
						default:
							cmd.print("? ");
					}
					break;
				// zadny default: tady neni, nic jinyho se asi nema vypisovat
			}
		}
	}

	@Override
	protected void printTimeout() {
		cmd.print("* "); // prints timeout without arrival
	}

	@Override
	protected void lineEnding() {
		cmd.print("\n");
	}
}
