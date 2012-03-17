package logging.networkEvents;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import logging.Logger;
import logging.LoggingCategory;

/**
 * server socket thread --- connection listener
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class EventServer implements Runnable {

	private int port;
	private EventsListener listener;
	private boolean quit = false;
	private ServerSocket serverSocket;

	public EventServer(int port) {
		this.port = port;
		this.listener = new EventsListener();
	}

	public EventsListener getListener() {
		return listener;
	}

	@Override
	public void run() {
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ex) {
			Logger.log(Logger.ERROR, LoggingCategory.EVENTS_SERVER, "IOException occured when creating server socket");
			return;
		}

		Logger.log(Logger.DEBUG, LoggingCategory.EVENTS_SERVER, "EventServer server socket successfully created.");
		
		// now is the right time to start EventListener thread
		this.listener.run();
		
		while (!quit) {
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException ex) {
				if (!quit) {
					Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "IOException occured when creating client socket. No other client socket will be created");
				}
				return;
			}

			ClientSession clientSession = new ClientSession(clientSocket);

			this.listener.addClientSession(clientSession);

		}

	}
	
	public void stop(){
		this.quit = true;
		try {
			this.serverSocket.close();
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "IOException occured when closing server socket");
		}
	
	}
}
