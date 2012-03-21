package logging.networkEvents;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import logging.Logger;
import logging.LoggingCategory;
import shared.NetworkObject;
import telnetd.pridaneTridy.TelnetProperties;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ClientSession {

	/**
	 * socket reference
	 */
	private Socket socket;
	/**
	 * flag if quiting
	 */
	private boolean done = false;
	private ObjectOutputStream outputStream;
	/**
	 * reference used for self removing from the list
	 *
	 */
	private List listReference;
	
	private boolean telnetConfigSend = false;

	public ClientSession(Socket socket) {
		this.socket = socket;
	}

	public boolean isTelnetConfigSend() {
		return telnetConfigSend;
	}
	
	public void sendTelnetConfig(){
		this.telnetConfigSend = true;	
		this.send(TelnetProperties.getTelnetConfig());
			
	}

	/**
	 * transmission object throught connected socket and initialized outputstream
	 * @param object 
	 */
	public void send(NetworkObject object) {

		if(!this.telnetConfigSend){  // send only once... just for sure, if someone try to send object before telnetConfig is send
			this.sendTelnetConfig();
		}
		
		if (done) {
			return;
		}

		if (this.outputStream == null) {

			if (this.socket == null || this.socket.isClosed() || !this.socket.isConnected()) {
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Starting ClientSessionThread without properly connected socket. Stopping ClientSessionThread!!!");
				this.closeSession();
				return;
			}
			try {
				this.outputStream = new ObjectOutputStream(socket.getOutputStream());
				this.outputStream.flush();
			} catch (IOException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "IOException occured when creating clientSession outputStream");
				return;
			}

		}

		try {
			this.outputStream.writeObject(object);  //serialize && send 
			this.outputStream.flush();
		} catch (IOException ex) {

			if (done) {
				return;
			}

			if (this.socket == null || this.socket.isClosed() || !this.socket.isConnected()) {
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Starting ClientSessionThread without properly connected socket. Stopping ClientSessionThread!!!");
				this.closeSession();
			} else {
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Unexpected IOException occured when writing object into ObjectOutputStream");
			}
		}
	}

	public void closeSession() {
		this.listReference.remove(this);
		this.done = true;
		if (this.socket == null || this.socket.isClosed()) // nothing to close
		{
			return;
		}
		
		try {
			socket.close();
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Unexpected IOException occured when closing client session thread. Socket may not be closed properly");
		}
	}

	/**
	 * Set {@see #listReference}.
	 * 
	 * @param list 
	 */
	public void setListReference(List list) {
		this.listReference = list;
	}

	public boolean isActive() {
		return !this.done;
	}
}
