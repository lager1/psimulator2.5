

package logging.networkEvents;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import logging.Loggable;
import logging.Logger;
import logging.LoggerListener;
import logging.LoggingCategory;
import shared.NetworkObject;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class EventsListener implements Runnable, LoggerListener{

	boolean done = false;
	private final List<ClientSession> clientSessions = Collections.synchronizedList(new LinkedList<ClientSession>());
	private BlockingQueue<NetworkObject> objectsToBroadCast = new LinkedBlockingQueue<>();

	public EventsListener() {
	}
	
	@Override
	public void run() {
		
		while(!done){
			NetworkObject ntwObject = null;
			try {
				// get new object from broadcast queue. 
				// Timeouting prefered because there is no other simple option how to shutdown on blocking operation
				 ntwObject = objectsToBroadCast.poll(2000, TimeUnit.MILLISECONDS);  
			} catch (InterruptedException ex) { }  // does not matter
		
			if(ntwObject == null) // no object reached
				continue;
			
			Logger.log(Logger.DEBUG, LoggingCategory.EVENTS_SERVER, "Object to broadcast recieved. Broadcasting ... ");
			
			synchronized(this.clientSessions){
				for (ClientSession clientSession : clientSessions) { // for all client sessions
					if(clientSession.isActive())
						clientSession.send(ntwObject);
				}
			}
		
		}
		
	}

	public void addClientSession(ClientSession clientSession){
		synchronized(this.clientSessions){  // add after all sending is done
			this.clientSessions.add(clientSession);
		}
	
	}
	
	@Override
	public void listen(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {
		throw new UnsupportedOperationException("Not supported yet."); // @TODO translation object into shared.SimulatorEvent && add into objectsToBroadCast queue
	}

	@Override
	public void listen(String name, int logLevel, LoggingCategory category, String message) {	// probably no use for this method
	}

	public void quit(){
		this.done = true;
	
	}
	
	
}
