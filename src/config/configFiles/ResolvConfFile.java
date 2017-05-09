package config.configFiles;

import dataStructures.ipAddresses.IpAddress;
import device.Device;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Michal Horacek
 */
public class ResolvConfFile extends AbstractLinuxFile {

    public ResolvConfFile(FileSystem fs) {
        super(fs);
        this.filePath = "/etc/resolv.conf";
    }

    /**
     * Updates information about nameservers into the /etc/resolv.conf file
     *
     * @param nameServers List of strings representing ip addresses of nameservers
     */
    public void updateNameservers(final String[] nameServers) {
        if (!fileSystem.exists(filePath)) {
            createFile();
        }

        fileSystem.runOutputFileJob(filePath, new OutputFileJob() {
            @Override
            public int workOnFile(OutputStream output) throws Exception {
                PrintWriter writer = new PrintWriter(output);
                for (String nameServer : nameServers) {

                    // jestli se jedna o validni ip adresu, zapis
                    if (IpAddress.correctAddress(nameServer) != null) {
                        writer.println("nameserver " + nameServer);
                    }
                }
                writer.flush();
                return 0;
            }
        });
    }

    /**
     * Gets the list of nameservers found in the /etc/resolv.conf file
     *
     * @return List of IpAddresses representing nameservers
     */
    public List<IpAddress> getNameServers() {
        final List<IpAddress> nameServers = new LinkedList<>();

        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) throws Exception {
                    Scanner sc = new Scanner(input);
                    while (sc.hasNextLine()) {
                        String[] words = sc.nextLine().trim().split("\\s+");
                        if (words.length == 2 && words[0].equals("nameserver")) {
                            IpAddress ip = IpAddress.correctAddress(words[1]);
                            if (ip != null) {
                                nameServers.add(ip);
                            }
                        }
                    }
                    return 0;
                }
            });
        } catch (FileNotFoundException ex) {
            return nameServers;
        }

        return nameServers;
    }
}
