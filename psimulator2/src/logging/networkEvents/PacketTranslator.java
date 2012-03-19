package logging.networkEvents;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import logging.Loggable;
import logging.Logger;
import logging.LoggerListener;
import logging.LoggingCategory;
import shared.NetworkObject;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import shared.SimulatorEvents.SerializedComponents.SimulatorEvent;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class PacketTranslator implements Runnable, LoggerListener {

	/**
	 * input queue source of non-translated objects, internal queue. Source of objects a listen method.
	 */
	private LinkedBlockingQueue<Object> packetsToTranslate;
	/**
	 * output queue reference
	 */
	private LinkedBlockingQueue<NetworkObject> translatedPackets;
	private boolean quit = false;

	/**
	 *
	 * @param translatedPackets aka output queue
	 */
	public PacketTranslator(LinkedBlockingQueue<NetworkObject> translatedPackets) {
		this.packetsToTranslate = new LinkedBlockingQueue<>();
		this.translatedPackets = translatedPackets;
	}

	/**
	 * add packet for translation into input queue
	 *
	 * @param packet object to be translated
	 */
	private void addPacket(Object packet) {
		try {
			this.packetsToTranslate.offer(packet, 1, TimeUnit.SECONDS);
		} catch (InterruptedException ex) { // this exception should not be thrown because using LinkedBlockedQueue
			Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Cannot add packet for translation into packetToTranslate queue. Packed lost!");
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("PacketTranslator");

		while (!quit) {
			Object packetToTranslate = null;
			try {
				packetToTranslate = packetsToTranslate.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
			}   // no need to handle this exception, just handle the possibility of null object

			if (packetToTranslate == null) {

				// just for testing generate some random Events
				// @TODO COMMENT THIS !!!  FAKE GENERATION OF EVENTS
//				int eventCount = new Random().nextInt(20);
//				for (int i = 0; i < eventCount; i++) {
//
//					SimulatorEvent event = new SimulatorEvent(eventCount, eventCount, eventCount, eventCount, PacketType.TCP, "muhehe: " + i);
//					this.addPacket(event);
//				}
				// END COMMENT !!!

				continue;
			} 
			
			
			NetworkObject translatedObject = translatePacket(packetToTranslate);
			try {
				this.translatedPackets.offer(translatedObject, 1, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {  // this exception should not be thrown because using LinkedBlockedQueue
				Logger.log(Logger.WARNING, LoggingCategory.EVENTS_SERVER, "Cannot add translated packet into translatedPackets queue. Packed lost!");
			}

		}



	}

	private NetworkObject translatePacket(Object object) {

		if(object instanceof NetworkObject) // if object is NetworkObject => send directly
			return (NetworkObject) object;
		
		// else create NetworkObject aka SimulatorEvent
		
		SimulatorEvent event = new SimulatorEvent();
		event.setCableId(1);
		event.setDestId(2);
		event.setDetailsText("bla bla");
		event.setPacketType(PacketType.TCP);
		event.setSourcceId(3);
		event.setTimeStamp(1234);


		throw new UnsupportedOperationException();
	}

	@Override
	public void listen(Loggable caller, int logLevel, LoggingCategory category, String message, Object object) {
		this.addPacket(object); // add packet object into translation queue
	}

	@Override
	public void listen(String name, int logLevel, LoggingCategory category, String message) {  // NO USE FOR THIS METHOD
	}

	public void stop() {
		this.quit = true;
		Logger.log(Logger.INFO, LoggingCategory.EVENTS_SERVER, "Stopping PacketTranslator");
	}
}
