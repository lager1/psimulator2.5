/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package config;


import filesystem.ArchiveFileSystem;
import filesystem.FileSystem;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.Test;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class TestFileSystem {
    
    
    public static String fileSystemPath = "network.fsm";
	public static String testFileName = "/etc/iproute2/rt_tables";
    public static String testString = "muhehe";
    

    @Test
    public void testConfig() throws IOException {

		testWrite();
		testRead();

    }
	
	public void testWrite(){
	
		FileSystem fsm = new ArchiveFileSystem(fileSystemPath);
		PrintStream print = new PrintStream(fsm.getOutputStreamToFile(testFileName));
		print.println(testString);
		print.close();
		
		fsm.umount();
		
	}

	private void testRead() {
		
		FileSystem fsm = new ArchiveFileSystem(fileSystemPath);
		Scanner scan = new Scanner(fsm.getInputStreamToFile(testFileName));
		
		assert scan.nextLine().equalsIgnoreCase(testString);
		
		fsm.umount();
		
	}

}
