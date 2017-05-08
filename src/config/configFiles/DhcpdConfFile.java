package config.configFiles;

import dataStructures.configurations.DhcpConfigRange;
import dataStructures.configurations.DhcpServerConfiguration;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Michal Horacek
 */
public class DhcpdConfFile extends AbstractLinuxFile {

    private int currentState;
    // parser se nachazi v globalnim prostoru konfig. souboru
    private final int NONE_STATE = 0;
    // parser se nachazi uvnitr subnet bloku
    private final int SUBNET_STATE = 1;
    private static final String[] ALLOWED_STRINGS = {
        "lease-time",
        "domain-name-servers",
        "domain-name",
        "subnet-mask",
        "broadcast-address",
        "routers"};
    private static final Set<String> ALLOWED_OPTIONS = new HashSet<>(Arrays.asList(ALLOWED_STRINGS));

    /**
     *
     * @param device
     */
    public DhcpdConfFile(FileSystem fs) {
        super(fs);
        this.filePath = "/etc/dhcp/dhcpd.conf";
        currentState = NONE_STATE;
    }

    private String loadMultipleOptions(String[] words) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < words.length - 1; ++i) {
            sb.append(words[i]);
            sb.append(" ");
        }
        sb.append(words[words.length - 1]);
        return sb.toString();
    }

    /**
     *
     * @return
     */
    public DhcpServerConfiguration getConfiguration() {
        DhcpServerConfiguration config = new DhcpServerConfiguration();
        ArrayList<String> lines = parseFile();
        ArrayList<DhcpConfigRange> ranges = new ArrayList<>();
        String subNet = null;
        String subMask = null;
        HashMap<String, String> subnetOptions = new HashMap<>();

        for (String line : lines) {
            String[] words = line.split("\\s+");

            if (words.length == 0) {
                continue;
            } else if (words.length == 5 && words[0].equals("subnet")) {
                if (currentState == SUBNET_STATE) {
                    continue;
                }
                subNet = words[1];
                subMask = words[3];
                currentState = SUBNET_STATE;
            } else if (words.length == 3 && words[0].equals("range")) {
                if (currentState == NONE_STATE) {
                    continue;
                }
                ranges.add(new DhcpConfigRange(words[1], words[2]));
            } else if (words.length > 2 && words[0].equals("option")) {
                if (words[1].equals("domain-name-servers")) {
                    String opt = loadMultipleOptions(words);
                    if (currentState == NONE_STATE) {
                        config.options.put(words[1], opt);
                    } else {
                        subnetOptions.put(words[1], opt);
                    }
                } else if (words[1].equals("routers")) {
                    String opt = loadMultipleOptions(words);
                    if (currentState == NONE_STATE) {
                        config.options.put(words[1], opt);
                    } else {
                        subnetOptions.put(words[1], opt);
                    }
                } else if (ALLOWED_OPTIONS.contains(words[1])) {
                    if (currentState == NONE_STATE) {
                        config.options.put(words[1], words[2]);
                    } else {
                        subnetOptions.put(words[1], words[2]);
                    }
                }
            } else if (words.length == 1 && words[0].equals("}")) {
                if (currentState == SUBNET_STATE) {
                    config.addSubnet(subNet, subMask, ranges, subnetOptions);
                    subnetOptions.clear();
                    currentState = NONE_STATE;
                }
            } else if (words.length == 2 && words[0].equalsIgnoreCase("default-lease-time")) {
                if (currentState == NONE_STATE) {
                    config.defaultLeaseTime = words[1];
                } else {
                    subnetOptions.put(words[0], words[1]);
                }
            } else if (words.length == 2 && words[0].equalsIgnoreCase("max-lease-time")) {
                if (currentState == NONE_STATE) {
                    config.maxLeaseTime = words[1];
                } else {
                    subnetOptions.put(words[0], words[1]);
                }
            }
        }

        return config;
    }

    private ArrayList<String> parseFile() {
        final ArrayList<String> lines = new ArrayList<>();

        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) throws Exception {
                    Scanner sc = new Scanner(input);
                    String line;

                    while (sc.hasNextLine()) {
                        line = sc.nextLine().trim();
                        if (line.endsWith(";")) {
                            lines.add(line.substring(0, line.length() - 1));
                        } else if (line.endsWith("{") || line.endsWith("}")) {
                            lines.add(line);
                        }
                    }

                    return 0;
                }
            });
        } catch (FileNotFoundException ex) {
            Logger.log("DhcpdConf", Logger.WARNING, LoggingCategory.FILE_SYSTEM,
                    "DhcpdConf does not exist");
        }

        return lines;
    }
}
