package config.configFiles;

import applications.dns.DnsServerThread.DnsQuery;
import config.configFiles.FileJobs.QueryZoneFileJob;
import dataStructures.packets.L7.DnsPacket;
import filesystem.FileSystem;
import filesystem.exceptions.FileNotFoundException;
import logging.Logger;
import logging.LoggingCategory;

/**
 * @author Michal Horacek
 */
public class DnsZoneFile extends AbstractLinuxFile {

    public DnsZoneFile(FileSystem fs) {
        super(fs);
    }

    /**
     * Parses zone file and tries to find given query
     *
     * @param query
     * @param packet
     * @return
     */
    public DnsPacket resolveQuery(DnsQuery query, DnsPacket packet) {
        try {
            fileSystem.runInputFileJob(query.info.file, new QueryZoneFileJob(packet, query));
        } catch (FileNotFoundException | NullPointerException ex) {
            Logger.log("DnsZoneFile", Logger.INFO, LoggingCategory.GENERIC_APPLICATION,
                    "zone file does not exist: " + query.info.file);
            return packet;
        }

        return packet;
    }
}
