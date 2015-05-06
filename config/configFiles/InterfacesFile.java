package config.configFiles;

import dataStructures.configurations.InterfaceConfiguration;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;

/**
 * @author Michal Horacek
 */
public class InterfacesFile extends AbstractLinuxFile {

    private IPLayer ipLayer;

    public InterfacesFile(IPLayer iplayer) {
        super(iplayer.getNetMod().getDevice().getFilesystem());
        this.ipLayer = iplayer;
        this.filePath = "/etc/network/interfaces";
    }

    @Override
    public void createFile() {
        fileSystem.runOutputFileJob(filePath, new OutputFileJob() {
            @Override
            public int workOnFile(OutputStream output) throws Exception {
                PrintWriter writer = new PrintWriter(output);
                for (NetworkInterface iface : ipLayer.getSortedNetworkIfaces()) {
                    if (iface.getIpAddress() == null) {
                        writer.println("iface " + iface.name + " inet dhcp");
                        writer.println("");
                    } else {
                        writer.println("iface " + iface.name + " inet static");
                        writer.println("address " + iface.getIpAddress().getIp().toString());
                        writer.println("netmask " + iface.getIpAddress().getMask().toString());
                        writer.println("broadcast " + iface.getIpAddress().getBroadcast().toString());
                        writer.println("");
                    }
                }
                writer.flush();
                return 0;
            }
        });
    }

    // true, pokud uz pro interface se jmenem ifaceName existuje v konfiguraci zaznam
    private boolean isDuplicateInterface(String ifaceName,
                                         ArrayList<InterfaceConfiguration> ifaces) {
        for (InterfaceConfiguration iface : ifaces) {
            if (iface.ifaceName.equals(ifaceName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method used to parse the networking configuration file
     *
     * @return Parsed configurations
     */
    public ArrayList<InterfaceConfiguration> getConfig() {
        final ArrayList<InterfaceConfiguration> ifaces = new ArrayList<>();
        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) {
                    InterfaceConfiguration iface = new InterfaceConfiguration();
                    Scanner sc = new Scanner(input);
                    String line;
                    String[] words;

                    while (sc.hasNextLine()) {
                        line = sc.nextLine().trim();

                        // Radek je komentar - ignoruji
                        if (line.startsWith("#")) {
                            continue;
                        }

                        words = line.split("\\s+");
                        if (words.length == 0) {
                            continue;
                        } else if (words.length == 4 && words[0].equalsIgnoreCase("iface")) {

                            // Parsovani druheho nebo dalsiho rozhrani. Nacitani konfigurace predchoziho rozhrani
                            // je dokonceno, takze se nejprve ulozi. Teprve potom se zacina parsovat soucasne zozhrani.
                            if (iface.ifaceName != null) {
                                ifaces.add(iface);
                                iface = new InterfaceConfiguration();
                            }

                            // Pokud se jedna o dalsi zaznam k uz nactenemu rozhrani, preskoci se
                            if (!isDuplicateInterface(words[1], ifaces)) {
                                iface.ifaceName = words[1];
                                iface.inetType = words[2];
                                iface.type = words[3];
                            }
                        } /*
                         * else if (words.lenghth == 3 && words[0].equalsIgnoreCsae("iface")) {
                         *   if (iface.ifaceName != null) {
                         *     ifaces.add(iface);
                         *     iface = new InterfaceConfiguration();
                         *   }
                         * 
                         *   iface.ifaceName = words[1];
                         *   iface.type      = words[2];
                         * }
                         */ else if (words.length == 2 && iface.ifaceName != null) {
                            if (words[0].equalsIgnoreCase("address")) {
                                iface.address = IpAddress.correctAddress(words[1]);
                            } else if (words[0].equalsIgnoreCase("netmask")) {
                                iface.mask = IpNetmask.correctNetmask(words[1]);
                            } else if (words[0].equalsIgnoreCase("broadcast")) {
                                iface.broadcast = IpAddress.correctAddress(words[1]);
                            } else if (words[0].equalsIgnoreCase("gateway")) {
                                iface.gateway = IpAddress.correctAddress(words[1]);
                            }
                        }
                    }
                    if (iface.ifaceName != null) {
                        ifaces.add(iface);
                    }

                    return 0;
                }
            });
        } catch (FileNotFoundException e) {
            Logger.log("IfaceFile", Logger.WARNING, LoggingCategory.FILE_SYSTEM,
                    "IfaceFile does not exist");
        }

        return ifaces;
    }
}
