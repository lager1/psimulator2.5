package commands.linux;

import applications.dhcp.DhcpClientThread;
import commands.AbstractCommandParser;
import java.util.ArrayList;
import java.util.List;
import networkModule.IpNetworkModule;
import networkModule.L3.NetworkInterface;
import shell.apps.CommandShell.CommandShell;

/**
 *
 * @author Michal Horacek
 */
public class Dhclient extends LinuxCommand {

    private CommandShell shell;
    private List<NetworkInterface> interfaces;

    public Dhclient(AbstractCommandParser parser) {
        super(parser);
        this.shell = null;
        this.interfaces = new ArrayList<>();
    }

    @Override
    public void run() {
        String word = dalsiSlovo();

        while (!word.isEmpty()) {
            parseInput(word);
            word = dalsiSlovo();
        }

        runCommand();
    }

    private void parseInput(String word) {
        NetworkInterface iface;

        if (word.length() > 1 && word.charAt(0) == '-') {
            switch (word.charAt(1)) {
                case 'v':
                    this.shell = parser.getShell();
                    break;
                case 'h':
                    printHelp();
                    break;
                default:
                    printHelp();
            }
        } else {
            iface = ipLayer.getNetworkInteface(word);
            if (iface == null) {
                parser.getShell().printLine("Invalid interface: " + word);
            } else {
                interfaces.add(iface);
            }
        }
    }

    private void runCommand() {
        IpNetworkModule netMod = (IpNetworkModule) ipLayer.getNetMod();
        for (NetworkInterface iface : interfaces) {
            netMod.applicationLayer.getDhcpManager().lease(iface, shell);
        }
    }

    private void printHelp() {
        CommandShell cmd = parser.getShell();
        cmd.printLine("Usage: dhclient [-h][-v] [iface0 [... ifaceN]]");
        cmd.printLine("-v\tVerbose mode");
        cmd.printLine("-h\tPrint usage");
    }
}
