package logging.networkEvents;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import logging.Loggable;
import logging.Logger;
import logging.LoggerListener;
import logging.LoggingCategory;
import shared.NetworkObject;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;

/**
 * EventsListener is a LoggerListener running in own thread, because broadcasting events over remote transmission can be
 * time consuming operation.
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class EventsListener implements Runnable, LoggerListener {

	boolean done = false;
	private final List<ClientSession> clientSessions = Collections.synchronizedList(new LinkedList<ClientSession>());
	/**
	 * NetworkObject queue aka messages to be sent
	 */
	private BlockingQueue<NetworkObject> objectsToBroadCast = new LinkedBlockingQueue<>();

	public EventsListener() {
	}

	private void sendNetworkObject(NetworkObject object) {
		try {
			this.objectsToBroadCast.offer(object, 1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Cannot add object into broadcasting queue");  // @TODO test if this exception occur only exceptionaly
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("EventsListener");
		while (!done) {

			NetworkObject ntwObject = null;
			try {
				// get new object from broadcast queue. 
				// Timeouting prefered because there is no other simple option how to shutdown on blocking operation
				ntwObject = objectsToBroadCast.poll(2000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
			}  // does not matter

			if (ntwObject == null) // no object reached
			{

				// just for testing generate some random Events
				// @TODO COMMENT THIS !!!
//				int eventCount = new Random().nextInt(20);
//				for (int i = 0; i < eventCount; i++) {
//
//					SimulatorEvent event = new SimulatorEvent(eventCount, eventCount, eventCount, eventCount, PacketType.TCP, "muhehe: " + i);
//					this.sendNetworkObject(event);
//				}
				// END COMMENT !!!


				continue;
			}

			Logger.log(Logger.DEBUG, LoggingCategory.EVENTS_SERVER, "Object for broadcasting recieved. Broadcasting ... ");

			synchronized (this.clientSessions) {
				for (ClientSession clientSession : clientSessions) { // for all client sessions
					if (clientSession.isActive()) {
						clientSession.send(ntwObject);
					}
				}
			}

		}

	}

	public void addClientSession(ClientSession clientSession) {
		synchronized (this.clientSessions) {  // better exclusive access 
			this.clientSessions.add(clientSession);
			clientSession.setListReference(clientSessions);
		}

	}

	@Override
	public void listen(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {

		// @TODO translation object into shared.SimulatorEvent

		SimulatorEvent event = new SimulatorEvent();
		event.setCableId(1);
		event.setDestId(2);
		event.setDetailsText("bla bla");
		event.setPacketType(PacketType.TCP);
		event.setSourcceId(3);
		event.setTimeStamp(1234);

		this.sendNetworkObject(event);

	}

	@Override
	public void listen(String name, int logLevel, LoggingCategory category, String message) {	// probably no use for this method
	}

	public void quit() {
		this.done = true;

	}
}
