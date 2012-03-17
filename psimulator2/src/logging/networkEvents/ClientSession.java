package logging.networkEvents;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import logging.Logger;
import logging.LoggingCategory;
import shared.NetworkObject;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ClientSession {

	private Socket socket;
	private boolean done = false;
	private ObjectOutputStream outputStream;

	public ClientSession(Socket socket) {
		this.socket = socket;
	}

	public void send(NetworkObject object) {

		if (this.outputStream == null) {

			if (this.socket == null || this.socket.isClosed() || !this.socket.isConnected()) {
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Starting ClientSessionThread without properly connected socket. Stopping ClientSessionThread!!!");
				this.closeSession();
				return;
			}
			try {
				this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(ClientSession.class.getName()).log(Level.SEVERE, null, ex);
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
}
