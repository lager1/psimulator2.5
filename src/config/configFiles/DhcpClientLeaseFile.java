package config.configFiles;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import dataStructures.packets.dhcp.DhcpPacket;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import networkModule.L3.NetworkInterface;

/**
 *
 * @author Michal Horacek
 */
public class DhcpClientLeaseFile extends AbstractLeaseFile {

    public DhcpClientLeaseFile(FileSystem fs) {
        super(fs);
        this.filePath = "/etc/dhcp/dhclient.leases";
    }

    @Override
    public synchronized void appendLease(final DhcpPacket packet, final NetworkInterface iface) {
        if (!fileSystem.exists(filePath)) {
            createFile();
        }

        fileSystem.runOutputFileJob(filePath, new OutputFileJob() {
            @Override
            public int workOnFile(OutputStream output) throws Exception {
                if (!packet.options.containsKey("lease-start")) {
                    return 1;
                }

                SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");
                Date startDate = df.parse(packet.options.get("lease-start"));
                Calendar cal = Calendar.getInstance();
                PrintWriter writer = new PrintWriter(output);

                cal.setTime(startDate);
                cal.add(Calendar.SECOND, Integer.parseInt(packet.options.get("lease-time")));
                String endDate = df.format(cal.getTime());

                writer.append("lease {\n");
                writer.append("interface " + iface.name + ";\n");
                writer.append("fixed-address " + packet.ipToAssign.getIp() + ";\n");
                writer.append("option netmask " + packet.ipToAssign.getMask() + ";\n");

                for (String key : packet.options.keySet()) {
                    if (key.equals("lease-start")) {
                        continue;
                    }
                    writer.append("option " + key + " " + packet.options.get(key) + ";\n");
                }

                writer.append("renew " + endDate + ";\n");
                writer.append("rebind " + endDate + ";\n");
                writer.append("expire " + endDate + ";\n");

                writer.append("}\n\n");
                writer.flush();
                return 0;
            }
        }, true);
    }

    public ClientLeaseRecord activeLease(final NetworkInterface iface) {
        final ClientLeaseRecord lease = new ClientLeaseRecord();

        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) throws Exception {
                    Scanner sc = new Scanner(input);
                    String line;
                    String[] words;
                    IpAddress adr = null;
                    IpNetmask mask = null;
                    Date expire = null;
                    String ifaceName = null;

                    // 0/1 = outside/inside lease record
                    int state = 0;

                    while (sc.hasNextLine()) {
                        line = sc.nextLine().trim();

                        if (line.startsWith("#")) {
                            continue;
                        }

                        words = line.split("\\s+");
                        if (words.length == 2 && words[0].equals("lease")) {
                            adr = null;
                            mask = null;
                            expire = null;
                            ifaceName = null;
                            state = 1;
                        } else if (state == 1 && words.length == 2 && words[0].equals("interface")) {
                            ifaceName = words[1].substring(0, words[1].length() - 1);
                        } else if (state == 1 && words.length == 2 && words[0].equals("fixed-address")) {
                            adr = IpAddress.correctAddress(words[1].substring(0, words[1].length() - 1));
                        } else if (state == 1 && words.length == 3
                                && words[0].equals("option") && words[1].equals("netmask")) {
                            mask = IpNetmask.correctNetmask(words[2].substring(0, words[2].length() - 1));
                        } else if (state == 1 && words.length == 4 && words[0].equals("expire")) {
                            expire = df.parse(getDateString(words));
                            if (isExpired(expire)) {
                                expire = null;
                            }
                        } else if (words.length == 1 && words[0].equals("}")) {
                            state = 0;
                        }

                        if (adr != null && mask != null && 
                            expire != null && ifaceName.equals(iface.name)) {
                            lease.leasedAddress = new IPwithNetmask(adr, mask);
                            lease.expireDate = expire;
                            break;
                        }
                    }
                    return 0;
                }
            });
        } catch (FileNotFoundException ex) {
            return null;
        }

        return lease;
    }

    public ArrayList<String> getLeasedAddresses() {
        final ArrayList<String> leased = new ArrayList<>();

        try {
            fileSystem.runInputFileJob(filePath, new InputFileJob() {
                @Override
                public int workOnFile(InputStream input) throws Exception {
                    Scanner sc = new Scanner(input);
                    String line;
                    String[] words;

                    while (sc.hasNextLine()) {
                        line = sc.nextLine().trim();

                        // Line is a comment - ignore
                        if (line.startsWith("#")) {
                            continue;
                        }

                        words = line.split("\\s+");
                        if (words[0].equalsIgnoreCase("fixed-address")) {
                            leased.add(words[1]);
                        }
                    }

                    return 0;
                }
            });
        } catch (FileNotFoundException e) {
            return null;
        }

        return leased;
    }

    public class ClientLeaseRecord {
        public IPwithNetmask leasedAddress;
        public Date expireDate;
    }
}
