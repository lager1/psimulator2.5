/*
 * Erstellt am 13.3.2012.
 */
package utils;

import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Budik. Vzbudi zaregitrovanej SmartRunnable.
 *
 * @author Tomas Pitrinec
 */
public class Alarm implements SmartRunnable, Loggable{

	/**
	 * Jen casy. Mam to kvuli razeni.
	 */
	Queue<WakeableItem> clients;
	protected final WorkerThread worker;

	public Alarm() {
		clients = new PriorityQueue<>();
		this.worker = new WorkerThread(this);
	}

	@Override
	public void doMyWork() {

		while (!clients.isEmpty()) {

			long absNextTime = clients.peek().absTime;
			long relativeTimeToSleep = absNextTime - System.currentTimeMillis();


			if (relativeTimeToSleep <= 0) {	// kdyby budik zaspal, rovnou vzbudit
				wakeObject(clients.poll());

			} else { //nezaspal, jde spat:

				// spani:
				try {
					Logger.log(this, Logger.DEBUG, LoggingCategory.ALARM, "Jdu spat.", null);
					Thread.sleep(relativeTimeToSleep);
					// po vyspani probudim objekt:
					wakeObject(clients.poll());

				} catch (InterruptedException ex) {	// pri spani me nekdo prerusil
					// kdyz me neco vzbudi, musim znovu prepocitat dylku spanku - jdu na zacatek
				}
			}
		}
	}

	private void wakeObject(WakeableItem item) {
		if (item == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.ALARM, "Ke vzbuzeni mam objekt null, prusvih.", null);
		} else {
			Logger.log(this, Logger.DEBUG, LoggingCategory.ALARM, "Jdu vzbudit objekt", item.obj);
			item.obj.wake();
		}
	}

	/**
	 * Zaregistruje vzbuzeni.
	 *
	 * @param client klient, kterej se ma vzbudit
	 * @param relTime za jak dlouho se ma vzbudit (v milisekundach)
	 */
	public void registerWake(Wakeable client, long relTime) {
		long absTimeToWake = System.currentTimeMillis() + relTime;
		clients.add(new WakeableItem(absTimeToWake, client));
		Logger.log(this, Logger.DEBUG, LoggingCategory.ALARM, "Zaregistroval jsem objekt, mam ho vzbudit za "+relTime+" ms.", client);

		worker.wake();
	}

	@Override
	public String getDescription() {
		return "System alarm";
	}




	/**
	 * Polozka fronty ke vzbuzeni.
	 */
	private class WakeableItem implements Comparable<WakeableItem> {

		public final long absTime;
		public final Wakeable obj;

		public WakeableItem(long absTime, Wakeable obj) {
			this.absTime = absTime;
			this.obj = obj;
		}

		@Override
		public int compareTo(WakeableItem o) {
			return ((Long) absTime).compareTo(o.absTime);
		}
	}
}
