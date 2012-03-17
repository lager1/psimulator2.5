package logging.networkEvents;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import logging.Logger;
import logging.LoggingCategory;
import shared.NetworkObject;

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

	public ClientSession(Socket socket) {
		this.socket = socket;
	}

	/**
	 * transmission object throught connected socket and initialized outputstream
	 * @param object 
	 */
	public void send(NetworkObject object) {

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
			} catch (IOException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "IOException occured when creating clientSession outputStream");
				return;
			}

		}

		try {
			this.outputStream.writeObject(object);  //serialize && send 
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
