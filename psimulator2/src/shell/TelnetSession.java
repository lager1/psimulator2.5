package shell;

import java.io.IOException;
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

        this.m_Connection = con;
        this.m_IO = m_Connection.getTerminalIO();
        this.m_Connection.addConnectionListener(this); //dont forget to register listener





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
            //log.error("connectionTimedOut()", ex);
        }
    }//connectionTimedOut

    @Override
    public void connectionIdle(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_IDLE");
            m_IO.flush();
        } catch (IOException e) {
           // log.error("connectionIdle()", e);
        }

    }//connectionIdle

    @Override
    public void connectionLogoutRequest(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_LOGOUTREQUEST");
            m_IO.flush();
            this.m_Connection.close();
        } catch (Exception ex) {
         //   log.error("connectionLogoutRequest()", ex);
        }
    }//connectionLogout

    @Override
    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_BREAK");
            m_IO.flush();
        } catch (Exception ex) {
           // log.error("connectionSentBreak()", ex);
        }
    }//connectionSentBreak
}
