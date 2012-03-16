/*
 * Erstellt am 16.3.2012.
 */

package commands.linux;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 * Vypisuje napovedu simulatoru.
 * TODO: Nutno upravit podle konecny verze.
 * @author Tomas Pitrinec
 */
public class Help extends AbstractCommand {

	public Help(AbstractCommandParser parser) {
		super(parser);
	}



	@Override
	public void run() {
		vykonejPrikaz();
	}



    protected void vykonejPrikaz() {
        printLine("Tento prikaz na realnem pocitaci s linuxem neni. Zde je pouze pro informaci, jake prikazy jsou v tomto simulatoru implementovany.");
        printLine("");
        printLine("Simulator ma oproti skutecnemu pocitaci navic tyto prikazy:");
        printLine("uloz / save   ulozeni stavijici virtualni site do souboru");
        printLine("              napr. uloz ./konfiguraky/sit.xml   - ulozi se relativne k ceste, ze ktere je spusten server");
        printLine("help          vypsani teto napovedy");
        printLine("");
        printLine("Z linuxovych prikazu jsou podporovany tyto:");
        printLine("ifconfig      parametry adresa, netmask, up, down");
        printLine("route         akce add, del; parametry -net, -host, dev, gw, netmask");
        printLine("iptables      jen pro pridani pravidla k natovani");
        printLine("              napr: iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE");
        printLine("ping          prepinace -c, -i, -s, -t");
        printLine("              prednastaven na 4 pakety");
        printLine("traceroute    jen napr. traceroute 1.1.1.1");
        printLine("exit");
        printLine("ip            podprikazy addr a route");
        printLine("echo, cat     jen na zapisovani a cteni souboru /proc/sys/net/ipv4/ip_forward");
        printLine("");
    }

}
