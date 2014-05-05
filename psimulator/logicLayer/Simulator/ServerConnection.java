package psimulator.logicLayer.Simulator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author lager1
 */

public class ServerConnection {

	private Process p;						// process to start
	private String port = "12000";			// process port
	private File log;						// server log file
	private String ipAddress = "127.0.0.1";	// server ip address 
	private BufferedReader br;				// for continuous reading of log file
	private File serverFile;
	private ServerFileStatus status;
	private File netFile;					// file with the network to simulate
	
	// sha256 sum of server jar file (so it can be checked, whether the file was modified)
	//private String serverFileHash = "49f3196e44ac529305b1e2fc12a74125d05ba604de172a501ad17459a8579b4b";		
	private String serverFileHash = "059ea32e324630971725becb8e552e9919bb2c83426fb63ee8cb7b7e37d1414c";		
	private String location;
	
        // HLAVNI TODO
        // PROC JE TAK VELKY ROZDIL MEZI PUVODNIM A NOVYM BACKENDEM ?!!!
        
	/* creates the server process
	 * @param file current file with topology
	 * 
	 */
public ServerConnection (File file) {
        location = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        location = location.replace("file:", "");
        location = location.substring(0, location.lastIndexOf(File.separator));

        serverFile = new File(location + File.separator + "psimulator2_backend.jar");
        netFile = file;
        status = checkServerFile(); 
    }

			
	public void start() {
		ProcessBuilder pb = new ProcessBuilder(location + File.separator + "psimulator2_backend.jar", netFile.toString()); 
		//ProcessBuilder pb = new ProcessBuilder(serverFile.getAbsolutePath(), serverFile.toString()); 

		//System.out.println("backend nasrtoval");
		
        try {
			log = File.createTempFile("psimulatorServerLog", ".tmp");	// temporary server file
		} catch (IOException e1) {
			System.out.println("Error creating server log file!");
			e1.printStackTrace();
		}
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.appendTo(log));
        try {
			this.p = pb.start();
		} catch (IOException e) {
			System.out.println("Error starting the server process!");
			e.printStackTrace();
		}
	}
			
	public ServerFileStatus getServerFileStatus() {
		return status;
	
	}
			
    private ServerFileStatus checkServerFile() {
                // file does not exist
                if(!serverFile.exists()) {
                    return ServerFileStatus.FILE_NOT_FOUND;
                }
	
		// is not readable
		if(!serverFile.canRead())
			return ServerFileStatus.FILE_NOT_READABLE;
		
		
		// file is not executable
		if(!serverFile.canExecute())
			return ServerFileStatus.FILE_NOT_EXECUTABLE;

		// hash of file does not match expected hash
		if (!getSHA256CheckSum(serverFile.getAbsolutePath()).equals(serverFileHash)) {
			System.out.println(getSHA256CheckSum(serverFile.getAbsolutePath()));
			System.out.println(serverFileHash);
			return ServerFileStatus.FILE_MODIFIED;
		}

		// file is ok
		return ServerFileStatus.FILE_OK;
    }

    
    public static byte[] createChecksum(String filename) {
        InputStream is;

        try {
			is = new FileInputStream(filename);

	        byte[] buffer = new byte[1024];
	        MessageDigest complete = MessageDigest.getInstance("SHA-256");
	        int numRead;

	        do {
	            numRead = is.read(buffer);
	            if (numRead > 0) {
	                complete.update(buffer, 0, numRead);
	            }
	        } while (numRead != -1);

	        is.close();

	        return complete.digest();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

    private String getSHA256CheckSum(String filename) {
        String result = "";
        byte[] b = null;

        try {
			b = createChecksum(filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        
        return result;
    }

    
    /*
     * Terminates the server process
	 *
     */
	public void terminate() {
		p.destroy();	// end process
		log.delete();	// delete temporary file
	}

	/*
	 * Returns the server port 
	 * 
	 */
	public String getPort() {
		
        try {
			br = new BufferedReader(new FileReader(log));
			
			String line;
			line = br.readLine();

    		// need to wait for server to generate output
	        while (true) { // need to wait for server port
	        	if(line != null && line.contains("listening on port")) {
	    			String[] fields = line.split(" ");	// split by " "
	    			port = fields[fields.length - 1];	// port is the last field
	    			
	    			return port;		
	        	}
	        	else {
		    		try {
						Thread.sleep(100);		// wait for more output
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        	}

	        	line = br.readLine();	// read next line
	        }

        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return port;	// just for compiler's sake
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public File getLogFile() {
		return log;
	}
	
}