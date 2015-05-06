/*
 * Erstellt am 7.4.2012.
 */
package commands.linux;

import applications.Application;
import applications.dhcp.DhcpServer;
import applications.Networking;
import applications.dns.DnsServer;
import commands.AbstractCommandParser;
import psimulator2.Psimulator;

/**
 * Linuxovej prikaz service na zapinani sluzeb typu dhcp server apod. Zatim
 * udelana jednoduse, pri pridani dalsich sluzeb dopsat napr. napovedu.
 *
 * @author Tomas Pitrinec
 */
public class Service extends LinuxCommand {

    String command;
    /**
     * Aplikace, ktera bude pripadne spustena.
     */
    Application newApp;

    public Service(AbstractCommandParser parser) {
        super(parser);
    }

    @Override
    public void run() {
        if (parsujPrikaz() == 0) { //parsuju, pokud vse v poradku
            vykonejPrikaz();
        }
    }

    /**
     * Parses the command.
     *
     * @return 1 iff parsing error
     */
    private int parsujPrikaz() {
        String serviceName = dalsiSlovo();
        if (serviceName.equals("dhcp-server")) {
            newApp = new DhcpServer(getDevice());
        } else if (serviceName.equals("dns-server")) {
            newApp = new DnsServer(getDevice());
        } else if (serviceName.equals("networking")) {
            newApp = new Networking(getDevice(), parser.getShell());
        } else if (serviceName.equals("")) { // jmeno sluzby nebylo vubec zadano
            printHelp();
        } else { // zadanej nejakej nesmysl
            printLine(serviceName + ": unrecognized service");
            return 1;
        }

        command = dalsiSlovo();
        if (command.equals("")) {
            printLine("Usage: service <service_name> {start|stop|restart|force-reload|status}");
            return 1;
        }
        // zbytek se kontroluje az u provadeni (nechce se mi 2x)

        return 0;
    }

    private void vykonejPrikaz() {
        Application app = getDevice().getAppByName(newApp.name);
        if (command.equals("start")) {
            startNewApp(app);
        } else if (command.equals("stop")) {
            stopApp(app);
        } else if (command.equals("restart") || command.equals("force-reload")) {
            stopApp(app);
            app = null;
            startNewApp(app);
        } else if (command.equals("status")) {
            if (app != null) {
                printLine("Status of " + app.name + ": running.");
            } else {
                printLine("Status of " + newApp.name + ": not running.");
            }
        } else {
            printLine("Usage: service <service_name> {start|stop|restart|force-reload|status}");
        }
    }

    private void startNewApp(Application oldApp) {
        if (oldApp == null) {
            newApp.start();
        }
        printLine("Started: " + newApp.name);
    }

    private void stopApp(Application app) {
        if (app != null) {
            app.exit();
        }
        printLine("Stopped: " + newApp.name);
    }

    private void printHelp() {
        printLine("Usage: service < option > | --status-all | [ service_name [ command | --full-restart ] ]");
        printService("There are these services in " + Psimulator.getNameOfProgram() +
                ": dhcp-server, dns-server, networking");
    }
}
