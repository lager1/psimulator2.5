package config.configFiles;

import applications.dns.DnsServer.ZoneInfo;
import device.Device;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.exceptions.FileNotFoundException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Horacek
 */
public class NamedConfFile extends AbstractLinuxFile {

    public NamedConfFile(FileSystem fs) {
        super(fs);
        this.filePath = "/etc/named/named.conf";
    }

    public Map<String, ZoneInfo> getZones() {
        Map<String, ZoneInfo> zones = new HashMap<>();
        try {
            fileSystem.runInputFileJob(filePath, new GetZonesFileJob(zones));
        } catch (FileNotFoundException ex) {
            return zones;
        }

        return zones;
    }

    private enum ConfState {

        NONE,
        OPTIONS,
        FORWARDERS,
        ZONE,
    }

    private class GetZonesFileJob implements InputFileJob {

        private ConfState state;
        private String zoneName = null;
        private ZoneInfo zoneInfo = null;
        private final Map<String, ZoneInfo> zones;

        public GetZonesFileJob(Map<String, ZoneInfo> zones) {
            this.state = ConfState.NONE;
            this.zones = zones;
            zoneInfo = new ZoneInfo();
        }

        @Override
        public int workOnFile(InputStream input) throws Exception {
            Scanner sc = new Scanner(input);
            String line;
            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();

                if (line.startsWith(";")) {
                    continue;
                }

                if (isZoneLine(line)) {
                    state = ConfState.ZONE;
                } else if (isEndingBraceLine(line)) {
                    state = ConfState.NONE;
                    zoneName = null;
                    zoneInfo.file = null;
                    zoneInfo.type = null;
                } else {
                    parseZoneLine(line);
                    if (zoneName != null
                            && zoneInfo.file != null && zoneInfo.type != null) {
                        zones.put(zoneName, zoneInfo);
                        zoneInfo = new ZoneInfo();
                    }
                }
            }
            return 0;
        }

        private void parseZoneLine(String line) {
            if (state != ConfState.ZONE) {
                return;
            }

            String FILE_LINE = "\\s*file\\s+\"(.*)\";";
            String TYPE_LINE = "\\s*type\\s+(.*);";
            Pattern filePat = Pattern.compile(FILE_LINE);
            Pattern typePat = Pattern.compile(TYPE_LINE);
            Matcher fileMatch = filePat.matcher(line);
            Matcher typeMatch = typePat.matcher(line);

            if (fileMatch.matches() && fileMatch.groupCount() == 1) {
                zoneInfo.file = fileMatch.group(1);
            } else if (typeMatch.matches() && typeMatch.groupCount() == 1) {
                zoneInfo.type = typeMatch.group(1);
            }
        }

        private boolean isZoneLine(String line) {
            String DN_PATTERN = "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2, }\\.?)";
            String ZONE_LINE_PATTERN = "^zone\\s+\"(" + DN_PATTERN + ")\"\\s+\\{$";
            Pattern p = Pattern.compile(ZONE_LINE_PATTERN);
            Matcher m = p.matcher(line.trim());

            if (m.matches() && m.groupCount() > 2) {
                zoneName = m.group(1);
                if (!zoneName.endsWith(".")) {
                    zoneName += ".";
                }
            }

            return m.matches();
        }

        private boolean isEndingBraceLine(String line) {
            String[] words = line.split("\\s+");
            return state == ConfState.ZONE && words.length == 1 && words[0].equals("}");
        }

        private String getDomainName(String query) {
            int dotIndex = query.indexOf(".");
            String domainName;

            if (dotIndex == -1 || dotIndex >= query.length() - 1) {
                return null;
            }
            domainName = query.substring(dotIndex + 1);

            return domainName;
        }
    }
}
