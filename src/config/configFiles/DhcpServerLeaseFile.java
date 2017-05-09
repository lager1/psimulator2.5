package config.configFiles;

import dataStructures.ipAddresses.IpAddress;
import dataStructures.packets.L7.dhcp.DhcpPacket;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import networkModule.L3.NetworkInterface;

/**
 * @author Michal Horacek
 */
public class DhcpServerLeaseFile extends AbstractLeaseFile {

    private final Set<IpAddress> leased;
    private final Set<IpAddress> expired;

    public DhcpServerLeaseFile(FileSystem fs) {
        super(fs);
        this.filePath = "/etc/dhcp/dhcpd.leases";
        leased = new HashSet<>();
        expired = new HashSet<>();
    }

    @Override
    public void appendLease(final DhcpPacket packet, final NetworkInterface iface) {
        if (!fileSystem.exists(filePath)) {
            createFile();
        }

        fileSystem.runOutputFileJob(filePath, new OutputFileJob() {
            @Override
            public int workOnFile(OutputStream output) throws Exception {
                SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");
                Date startDate = df.parse(packet.options.get("lease-start"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.SECOND, Integer.parseInt(packet.options.get("lease-time")));

                PrintWriter writer = new PrintWriter(output);
                writer.append("lease " + packet.ipToAssign.getIp() + " {\n");

                if (packet.clientMac != null) {
                    writer.append("client-address " + packet.clientMac + ";\n");
                }

                writer.append("starts " + packet.options.get("lease-start") + ";\n");
                writer.append("ends " + df.format(cal.getTime()) + ";\n");
                writer.append("}\n\n");
                writer.flush();
                return 0;
            }
        }, true);
    }

    public Set<IpAddress> getExpiredLeases() {
        refreshLeases();
        return expired;
    }

    private IpAddress parseLine(String line, IpAddress leaseAddr) {
        String[] words = line.split("\\s+");
        Date endDate;

        if (words.length == 3 && words[0].equals("lease")) {
            leaseAddr = IpAddress.correctAddress(words[1]);
            // kontrola validity
        } else if (words.length == 4 && words[0].equals("ends")) {
            if (leaseAddr == null) {
                return null;
            }

            try {
                endDate = df.parse(getDateString(words));
            } catch (ParseException ex) {
                return null;
            }

            if (isExpired(endDate)) {
                expired.add(leaseAddr);
            } else {
                leased.add(leaseAddr);
            }
            leaseAddr = null;
        }

        return leaseAddr;
    }

    private void refreshLeases() {
        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {

                @Override
                public int workOnFile(InputStream input) throws Exception {
                    Scanner sc = new Scanner(input);
                    IpAddress leaseAddr = null;
                    while (sc.hasNextLine()) {
                        leaseAddr = parseLine(sc.nextLine().trim(), leaseAddr);
                    }
                    return 0;
                }
            });
        } catch (FileNotFoundException ex) {

        }
    }

    public Set<IpAddress> getLeasedAddresses() {
        refreshLeases();
        return leased;
    }
}
