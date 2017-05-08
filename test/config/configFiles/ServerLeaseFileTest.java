package config.configFiles;

import filesystem.ArchiveFileSystem;
import filesystem.exceptions.FileNotFoundException;
import java.io.File;
import java.text.SimpleDateFormat;
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
public class ServerLeaseFileTest {

    private static ArchiveFileSystem fs;
    private String filePath;
    private DhcpServerLeaseFile leaseFile;
    private final SimpleDateFormat df = new SimpleDateFormat("W yyyy/MM/dd HH:mm:ss");

    public ServerLeaseFileTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        String pathSeparator = System.getProperty("file.separator");

        File filesystemDir = new File("test-DATA");

        if (!filesystemDir.isDirectory() && !filesystemDir.mkdirs()) {
            fail("unable to create archiveFS test directory");
        }

        String pathFileSystem = filesystemDir.getAbsolutePath() + pathSeparator
                + "leaseFileTest." + ArchiveFileSystem.getFileSystemExtension();
        ServerLeaseFileTest.fs = new ArchiveFileSystem(pathFileSystem);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws FileNotFoundException {
        leaseFile = new DhcpServerLeaseFile(fs);
        filePath = leaseFile.getFilePath();

        if (fs.exists(filePath)) {
            fs.rm_r(filePath);
        }
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void emptyFileExpiredTest() {
        leaseFile.createFile();
        
        assertTrue(leaseFile.getExpiredLeases().isEmpty());
    }
    
    @Test
    public void emptyFileLeasedTest() {
        leaseFile.createFile();
        
        assertTrue(leaseFile.getLeasedAddresses().isEmpty());
    }
}
