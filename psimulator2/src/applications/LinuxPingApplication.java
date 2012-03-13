/*
 * Erstellt am 9.3.2012.
 */

package applications;


import commands.linux.Ping;
import dataStructures.IcmpPacket;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Tomas Pitrinec
 */
public class LinuxPingApplication extends PingApplication {

	Ping ping;

	/**
	 * Konstruktor.
	 *
	 * @param device
	 * @param command
	 * @param target cilova ip adresa
	 * @param count pocet pingu
	 * @param size velikost pingu
	 * @param timeout v ms
	 * @param interval v ms
	 * @param ttl kdyz je -1, nastavi se defaultni systemovej (ze sitovyho modulu)
	 */
	public LinuxPingApplication(Device device, Ping command, IpAddress target, int count, int size, int timeout, int interval, int ttl) {
		super(device, command);
		ping = (Ping) command;	// je to jako parametr, takze to musi vzdy projit
		this.target = target;
		this.count = count;
		this.size = size;
		this.timeout = timeout;
		this.waitTime = interval;
		if (ttl != -1) {
			this.ttl = ttl;
		}
	}





	@Override
	public void printStats() {
		ping.printLine("Tady se budou vypisovat statistiky pingu.");
	}

	@Override
	protected void handleIncommingPacket(IcmpPacket packet) {
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, getName() + " handleIncommingPacket, type="
				+ packet.type + ", code=" + packet.code + ", seq=" + packet.seq, packet);

		ping.printLine("Prisel paket " + packet.type);
	}

	@Override
	protected void startMessage() {
		ping.printLine("PING "+target+" ("+target+") "+size+"("+(size+28)+") bytes of data.");
	}

	@Override
	public String getDescription() {
		return device.getName()+": linux aplikace ping";
	}

}
