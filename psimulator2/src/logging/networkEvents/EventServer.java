package logging.networkEvents;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import logging.Logger;
import logging.LoggingCategory;

/**
 * server socket thread --- connection listener
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class EventServer implements Runnable {

	private int port;

	public EventServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		ServerSocket ss;

		try {
			ss = new ServerSocket(port);
		} catch (IOException ex) {
			Logger.log(Logger.ERROR, LoggingCategory.EVENTS_SERVER, "IOException occured when creating server socket");
			return;
		}

		while (true) {
			Socket clientSocket;
			try {
				clientSocket = ss.accept();
			} catch (IOException ex) {
				if (!ss.isClosed()) {
					Logger.log(Logger.ERROR, LoggingCategory.EVENTS_SERVER, "IOException occured when creating client socket");
				}
				return;
			}

			ClientSession clientSession = new ClientSession(clientSocket);

			// @TODO create new thread for logger listening and for sending events to clients

		}



	}
}
