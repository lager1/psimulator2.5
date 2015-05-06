package psimulator.logicLayer.Simulator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author lager1
 */

public class ServerConnection {

    private Process p;                        // process to start
    private String port = "12000";            // process port
    private File log;                        // server log file
    private String ipAddress = "127.0.0.1";    // server ip address
    private BufferedReader br;                // for continuous reading of log file
    private File serverFile;
    private ServerFileStatus status;
    private File netFile;                    // file with the network to simulate

    // sha256 sum of server jar file (so it can be checked, whether the file was modified)
    private String serverFileHash = "d4273e435926d3764c26ce1459763cea85ec63ea2dbb23e0e7e8b3f0de85ba5e";

        private String location;
    private String os;

    /* creates the server process
     * @param file current file with topology
     *
     */
    public ServerConnection (File file) {
        location = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();    // absolute path
        os = System.getProperty("os.name");

        if(os.startsWith("Win")) {
            location = location.replaceFirst("/", "");
        }

        location = location.substring(0, location.lastIndexOf("/"));

        try {
            location = URLDecoder.decode(location, "UTF-8");    // for dealing with non ascii characters
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        serverFile = new File(location + File.separator + "psimulator2_backend.jar");
        netFile = file;
        status = checkServerFile();
    }

    public void start() {
        ProcessBuilder pb;

        if(os.startsWith("Win")) {
            pb = new ProcessBuilder("cmd.exe", "/C", "java -jar ",
                location + File.separator + "psimulator2_backend.jar", netFile.toString());
        }
        else {
            pb = new ProcessBuilder(
                    System.getProperty("java.home") + File.separator + "bin" + File.separator + "java", // absolute java binary path
                    "-jar", location + File.separator + "psimulator2_backend.jar",      // jar !
                    netFile.toString());
        }

        try {
            log = File.createTempFile("psimulatorServerLog", ".tmp");    // temporary server file
        } catch (IOException e1) {
            System.out.println("Error creating server log file!");
            e1.printStackTrace();
        }

        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.appendTo(log));

        // just for debug
        pb.redirectError(Redirect.appendTo(log));

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

         if(os.startsWith("Win")) {
            Runtime rt = Runtime.getRuntime();
            String lastLine = "";           // for pid o running java process !
            String commands = "cmd /c tasklist /FI \"IMAGENAME eq java.exe\" /FI \"SESSIONNAME eq Console\"";

            Process proc;
            try {
                 proc = rt.exec(commands);
                BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    lastLine = s;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String delims = "[ ]+";
            String[] tokens = lastLine.split(delims);

             String cmd = "cmd /c taskkill /T /F /PID " + tokens[1];       // pid of running backend
             try {
                 Process child = Runtime.getRuntime().exec(cmd);
             } catch (IOException ex) {
                ex.printStackTrace();
             }

         }
         else {
            p.destroy();    // end process
         }
            log.delete();    // delete temporary file
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
                    String[] fields = line.split(" ");    // split by " "
                    port = fields[fields.length - 1];    // port is the last field

                    return port;
                }
                else {
                    try {
                        Thread.sleep(100);        // wait for more output
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                line = br.readLine();    // read next line
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return port;    // just for compiler's sake
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public File getLogFile() {
        return log;
    }

}