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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Michal Horacek
 */
public class HostsFile extends AbstractLinuxFile {

    public HostsFile(FileSystem fs) {
        super(fs);
        this.filePath = "/etc/hosts";
    }

    @Override
    public void createFile() {
        fileSystem.runOutputFileJob(filePath, new OutputFileJob() {
            @Override
            public int workOnFile(OutputStream output) throws Exception {
                PrintWriter writer = new PrintWriter(output);
                writer.println("127.0.0.1 localhost");
                writer.flush();
                return 0;
            }
        });
    }

    public IpAddress resolveAddress(final String toResolve) {
        final IpAddress[] res = {null};
        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) throws Exception {
                    Scanner sc = new Scanner(input);
                    while (sc.hasNextLine()) {
                        res[0] = parseLine(sc.nextLine().trim(), toResolve);
                        if (res[0] != null) {
                            return 0;
                        }
                    }
                    return 1;
                }
            });
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResolvConfFile.class.getName()).log(Level.INFO, null, ex);
        }

        return res[0];
    }

    private IpAddress parseLine(String line, String toResolve) {
        IpAddress res = null;
        String[] words = line.split("\\s+");
        
        if (words.length > 1) {
            for (int i = 1; i < words.length; i++) {
                if (words[i].equals(toResolve)) {
                    return IpAddress.correctAddress(words[0]);
                }
            }
        }

        return res;
    }
}
