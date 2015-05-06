package config.configFiles;

import dataStructures.packets.dhcp.DhcpPacket;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.OutputFileJob;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import networkModule.L3.NetworkInterface;

/**
 *
 * @author Michal Horacek
 */
public abstract class AbstractLeaseFile extends AbstractLinuxFile {

    protected final SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");

    public AbstractLeaseFile(FileSystem fs) {
            super(fs);
    }

    public abstract void appendLease(final DhcpPacket packet, final NetworkInterface iface);

    protected String getDateString(String[] words) {
        StringBuilder sb = new StringBuilder();
        if (words.length < 2) { 
            return null;
        }
        
        for (int i = 1; i < words.length; i++) {
            sb.append(words[i]);
            sb.append(" ");
        }

        return sb.toString();
    }

    protected boolean isExpired(Date d) {
        return new Date().after(d);
    }
}
