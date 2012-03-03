package shell;

import java.io.IOException;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.CommandShell.CommandShell;
import telnetd.io.BasicTerminalIO;
import telnetd.net.Connection;
import telnetd.net.ConnectionEvent;
import telnetd.shell.Shell;

/**
 *
 * @author Martin Lukáš
 */
public class TelnetSession implements Shell {

    private Connection m_Connection;
    private BasicTerminalIO m_IO;

    public void run(Connection con) {

		Logger.log(Logger.INFO, LoggingCategory.TELNET, "telnet session estabilished with host: " + con.getConnectionData().getHostAddress() +" port: "+ con.getConnectionData().getPort());
		
        this.m_Connection = con;
        this.m_IO = m_Connection.getTerminalIO();
        this.m_Connection.addConnectionListener(this); //dont forget to register listener
		
		new CommandShell(m_IO, null).run();
		

    }


    //this implements the ConnectionListener!
    @Override
    public void connectionTimedOut(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_TIMEDOUT");
            m_IO.flush();
            //close connection
            m_Connection.close();
        } catch (Exception ex) {
            Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection timeout");
        }
    }//connectionTimedOut

    @Override
    public void connectionIdle(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_IDLE");
            m_IO.flush();
        } catch (IOException e) {
            Logger.log(Logger.WARNING, LoggingCategory.TELNET, "CONNECTION_IDLE");
           
        }

    }//connectionIdle

    @Override
    public void connectionLogoutRequest(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_LOGOUTREQUEST");
            m_IO.flush();
            this.m_Connection.close();
        } catch (Exception ex) {
            Logger.log(Logger.INFO, LoggingCategory.TELNET, "CONNECTION_LOGOUTREQUEST");
        }
    }//connectionLogout

    @Override
    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_BREAK");
            m_IO.flush();
        } catch (Exception ex) {
            Logger.log(Logger.WARNING, LoggingCategory.TELNET, "CONNECTION_BREAK");
          
        }
    }//connectionSentBreak
	
	public static Shell createShell() {
        return new TelnetSession();
    }//telnetd library needs this method

}
