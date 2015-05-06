package config.configFiles.FileJobs;

import applications.dns.DnsResolver;
import applications.dns.DnsServerThread.DnsQuery;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.L7.dns.DnsAnswer;
import dataStructures.packets.L7.DnsPacket;
import dataStructures.packets.L7.dns.DnsType;
import filesystem.dataStructures.jobs.InputFileJob;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Michal Horacek
 */
public class QueryZoneFileJob implements InputFileJob {

    private final DnsPacket packet;
    private final DnsQuery dnsQuery;
    private String origin;
    private String label;
    private String authDomain;
    private int authLevels;
    private Map<String, String> nameServers;

    /**
     * @param packet
     * @param dnsQuery
     */
    public QueryZoneFileJob(DnsPacket packet, DnsQuery dnsQuery) {
        this.packet = packet;
        this.dnsQuery = dnsQuery;
        this.origin = dnsQuery.zone;
        this.label = origin;
        this.authDomain = origin;
        this.authLevels = authDomain.split("\\.").length;
        this.nameServers = new HashMap<>();
    }

    @Override
    public int workOnFile(InputStream input) throws Exception {
        Scanner sc = new Scanner(input);

        while (sc.hasNextLine()) {
            parseLine(sc.nextLine().trim());
        }

        return 0;
    }

    private void parseLine(String line) {
        // comment line
        if (line.startsWith(";")) {
            return;
        }

        String[] words = line.split("\\s+");

        if (words.length == 2) {
            handleDirectiveLine(words);
            // possible if line contains blank substitution
        } else if (words.length == 3) {
            handleLine(words);
        } else if (words.length == 4) {
            if (words[0].equals("@")) {
                words[0] = origin;
            } else if (!words[0].endsWith(".")) {
                words[0] = words[0] + "." + origin;
            }

            label = words[0];
            handleLine(words);
        }
    }

    private void handleDirectiveLine(String[] words) {
        // $ORIGIN example.com.
        if (words[0].equals("$ORIGIN")
                && DnsResolver.isValidDomainName(words[1])) {
            origin = words[1];
        }
    }

    private void handleLine(String[] words) {
        switch (words[words.length - 2]) {
            case "A":
                handleAddressLine(words);
                break;
            case "NS":
                handleNsLine(words);
                break;
            case "MX":
            case "TXT":
            case "CNAME":
            case "PTR":
            default:
                // to be implemented
        }
    }

    private void handleAddressLine(String[] words) {
        int ansIndex = words.length - 1;

        IpAddress addr = IpAddress.correctAddress(words[3]);
        if (addr == null) {
            return;
        }

        // address of a queried domain name, adding to the answer section
        if (label.equals(packet.question.qName)
                && !packet.containsData(words[ansIndex], packet.answers)) {
            packet.answers.add(new DnsAnswer(packet.question.qName, DnsType.A, words[ansIndex]));
        } else if (isCloserDomainMatch(nameServers.get(label))) {
            packet.additional.clear();
            packet.authority.clear();
            packet.authority.add(new DnsAnswer(nameServers.get(label), DnsType.NS, label));
            packet.additional.add(new DnsAnswer(label, DnsType.A, words[ansIndex]));
            authDomain = nameServers.get(label);
            authLevels = authDomain.split("\\.").length;
        } // address of an already defined nameserver, adding to the additional section
        else if (nameServers.get(label) != null
                && nameServers.get(label).equals(authDomain)) {

            if (!packet.containsData(label, packet.authority)) {
                packet.authority.add(
                        new DnsAnswer(nameServers.get(label),
                                DnsType.NS,
                                label));
            }

            packet.additional.add(new DnsAnswer(label, DnsType.A, words[ansIndex]));
        }
    }

    private void handleNsLine(String[] words) {
        int ansIndex = words.length - 1;

        if (!words[ansIndex].endsWith(".")) {
            words[ansIndex] += ".";
        }

        if (DnsResolver.isValidDomainName(words[ansIndex])) {
            nameServers.put(words[ansIndex], label);
        }
    }

    private boolean isCloserDomainMatch(String cmp) {
        if (cmp == null) {
            return false;
        }

        String[] queryLevels = packet.question.qName.split("\\.");
        String[] cmpLevels = cmp.split("\\.");
        int resLevels = 0;
        int limit = Math.min(queryLevels.length, cmpLevels.length);

        for (int i = 1; i <= limit; ++i) {
            if (queryLevels[queryLevels.length - i]
                    .equals(cmpLevels[cmpLevels.length - i])) {
                resLevels++;
            } else {
                return false;
            }
        }

        return resLevels > authLevels;
    }
}
