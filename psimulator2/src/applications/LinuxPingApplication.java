/*
 * Erstellt am 9.3.2012.
 */

package applications;


import commands.linux.Ping;
import dataStructures.IcmpPacket;
import dataStructures.IpPacket;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import logging.Logger;
import logging.LoggingCategory;
import utils.Util;

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
		//ping.printLine("Tady se budou vypisovat statistiky pingu.");

        int cas = (int) ((stats.odeslane - 1) * waitTime + Math.random() * 10); // celkovej cas se zas vymejsli =)
        ping.printLine("");
        ping.printLine("--- " + target.toString() + " ping statistics ---");
        if (stats.errors == 0) {//errory nebyly - tak se nevypisujou
            ping.printLine(stats.odeslane + " packets transmitted, " + stats.prijate + " received, " +
                    stats.ztrata + "% packet loss, time " + cas + "ms");
        } else { //vypis i s errorama
            ping.printLine(stats.odeslane + " packets transmitted, " + stats.prijate + " received, +" + stats.errors + " errors, " +
                    stats.ztrata + "% packet loss, time " + cas + "ms");
        }
        if (stats.prijate > 0) { //aspon jeden prijaty paket - vypisuji se statistiky
            double mdev = Util.zaokrouhli((((stats.avg - stats.min) + (stats.max - stats.avg)) / 2) * 0.666); //ma to bejt stredni odchylka,
            //je tam jen na okrasu, tak si ji pocitam po svym =)
            ping.printLine("rtt min/avg/max/mdev = " + Util.zaokrouhli(stats.min) + "/" + Util.zaokrouhli(stats.avg) + "/" +
                    Util.zaokrouhli(stats.max) + "/" + mdev + " ms");
        } else { // neprijat zadny paket, statistiky se nevypisuji
            ping.printLine(", pipe 3");
        }
	}



	@Override
	protected void handleIncommingPacket(IpPacket p, IcmpPacket packet, long delay) {

		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Prisel mi paket, jdu ho vypsat.", null);

		ping.printLine("XX bytes from "+p.src+": icmp_req="+packet.seq+" ttl="+p.ttl+" time="+delay+" ms");
		Logger.log(this, Logger.DEBUG, LoggingCategory.PING_APPLICATION, "Vypsal jsem paket.", null);
	}

	@Override
	protected void startMessage() {
		ping.printLine("PING "+target+" ("+target+") "+size+"("+(size+28)+") bytes of data.");
	}

	@Override
	public String getDescription() {
		return device.getName()+": ping_app_linux";
	}

}
