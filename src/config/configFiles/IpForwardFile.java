package config.configFiles;

import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Michal Horacek
 */
public class IpForwardFile extends AbstractLinuxFile {

    public IpForwardFile(FileSystem fs) {
        super(fs);
        this.filePath = "/proc/sys/net/ipv4/ip_forward";
    }
    
    @Override
    public void createFile() {
        fileSystem.runOutputFileJob(this.filePath, new OutputFileJob() {
            @Override
            public int workOnFile(OutputStream output) throws Exception {
                PrintWriter writer = new PrintWriter(output);
                writer.println("0");
                writer.flush();
                return 0;
            }
        });        
    }
    
    public boolean ip_forward() {
        
        if (!fileSystem.exists(filePath)) {
            createFile();
        }
        
        // vycteni 1. radku:
        final List<String> firstLine = new ArrayList<>(); // neprisel jsem na to, jak jinak to z ty vnitrni fce vytahnout
        try {
            fileSystem.runInputFileJob(this.filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) throws Exception {

                    Scanner sc = new Scanner(input);

                    if (sc.hasNextLine()) {
                        firstLine.add(sc.nextLine());
                    }

                    return 0;
                }
            });
        } catch (FileNotFoundException ex) {
            Logger.log("IpForwardFile", Logger.WARNING, LoggingCategory.NET, "ip_forward file not found!");
            return true;
        }

        // samotny nastaveni:
        try {
            int number = Integer.parseInt(firstLine.get(0));
            if (number == 0) {
                return false;
            }
        } catch (NumberFormatException ex) {
            return true;
        }


        return true;
    }
}
