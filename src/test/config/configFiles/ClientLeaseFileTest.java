package config.configFiles;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.packets.dhcp.DhcpPacket;
import dataStructures.packets.dhcp.DhcpPacketType;
import filesystem.ArchiveFileSystem;
import filesystem.exceptions.FileNotFoundException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import networkModule.L3.NetworkInterface;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author michal
 */
public class ClientLeaseFileTest {

    private static ArchiveFileSystem fs;
    private String filePath;
    private DhcpClientLeaseFile leaseFile;
    private final SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");

    public ClientLeaseFileTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        String pathSeparator = System.getProperty("file.separator");

        File filesystemDir = new File("test-DATA");

        if (!filesystemDir.isDirectory() && !filesystemDir.mkdirs())
        {
            fail("unable to create archiveFS test directory");
        }

        String pathFileSystem = filesystemDir.getAbsolutePath() + pathSeparator
                + "leaseFileTest." + ArchiveFileSystem.getFileSystemExtension();
        ClientLeaseFileTest.fs = new ArchiveFileSystem(pathFileSystem);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws FileNotFoundException {
        leaseFile = new DhcpClientLeaseFile(fs);
        filePath = leaseFile.getFilePath();

        if (fs.exists(filePath)) {
            fs.rm_r(filePath);
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createFileTest() {
        leaseFile.createFile();
        assertTrue(fs.exists(filePath));
    }

    @Test
    public void deleteFileTest() {
        leaseFile.createFile();

        try {
            fs.rm_r(filePath);
        } catch (FileNotFoundException ex) {
            fail("could not remove existing file");
        }

        assertFalse(fs.exists(filePath));
    }

    @Test
    public void appendActiveLeaseTest() {
        NetworkInterface iface = new NetworkInterface(0, "etho", null);
        DhcpPacket packet = createActiveLeasePacket();
        DhcpClientLeaseFile.ClientLeaseRecord rec;


        leaseFile.createFile();

        leaseFile.appendLease(packet, iface);

        rec = leaseFile.activeLease(iface);

        assertTrue(rec != null && rec.expireDate != null);
    }

    @Test
    public void appendInactiveLeaseTest() {
        NetworkInterface iface = new NetworkInterface(0, "etho", null);
        DhcpPacket packet = createInactiveLeasePacket();
        DhcpClientLeaseFile.ClientLeaseRecord rec;


        leaseFile.createFile();

        leaseFile.appendLease(packet, iface);

        rec = leaseFile.activeLease(iface);

        assertTrue(rec == null || rec.expireDate == null);
    }

    @Test
    public void emptyFileLeaseStatusTest() {
        NetworkInterface iface = new NetworkInterface(0, "etho", null);
        DhcpClientLeaseFile.ClientLeaseRecord rec;

        leaseFile.createFile();

        rec = leaseFile.activeLease(iface);

        assertTrue(rec == null || rec.expireDate == null);
    }

    private DhcpPacket createActiveLeasePacket() {
        HashMap<String, String> options = new HashMap<>();
        IPwithNetmask ipwn = new IPwithNetmask("10.0.0.0");
        options.put("lease-start", df.format(new Date()));
        options.put("lease-time", "7200");

        DhcpPacket packet = new DhcpPacket(DhcpPacketType.ACK, 0, null,
                ipwn, null, null, options);

        return packet;
    }

    private DhcpPacket createInactiveLeasePacket() {
        HashMap<String, String> options = new HashMap<>();
        IPwithNetmask ipwn = new IPwithNetmask("10.0.0.0");
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 10, 10);
        options.put("lease-start", df.format(cal.getTime()));
        options.put("lease-time", "7200");

        DhcpPacket packet = new DhcpPacket(DhcpPacketType.ACK, 0, null,
                ipwn, null, null, options);

        return packet;
    }
}
