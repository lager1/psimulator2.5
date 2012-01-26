/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import psimulator2.SmartRunnable;
import psimulator2.WorkerThread;

/**
 * Represents cabel
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Cabel implements SmartRunnable {
	AbstractInterface first;
	AbstractInterface second;

	WorkerThread worker = new WorkerThread(this);

	/**
	 * Delay in milliseconds
	 */
	long delay;

	/**
	 * Creates cabel with random delay time.
	 */
	public Cabel() {
		this.delay = (long) Math.random() * 10;
	}

	/**
	 * Creates cabel with given delay time.
	 * @param delay
	 */
	public Cabel(long delay) {
		this.delay = delay;
	}

	/**
	 * Creates cabel with 2 connected interfaces and with delay time.
	 * @param first cannot be null
	 * @param second cannot be null
 	 * @param delay
	 */
	public Cabel(AbstractInterface first, AbstractInterface second, long delay) {
		assert first != null;
		assert second != null;

		this.first = first;
		this.first.cabel = this;
		this.second = second;
		this.second.cabel = this;

		this.delay = delay;
	}

	/*
	 * Sets first interface with given iface.
	 * Also sets given interface to this cabel.
	 * @param iface cannot be null
	 */
	public void setFirstInterface(AbstractInterface iface) {
		assert iface != null;
		this.first = iface;
		iface.cabel = this;
	}

	/*
	 * Sets second interface with given iface.
	 * Also sets given interface to this cabel.
	 * @param iface cannot be null
	 */
	public void setSecondInterface(AbstractInterface iface) {
		assert iface != null;
		this.second = iface;
		iface.cabel = this;
	}

	public void doMyWork() {
		L2Packet packet;
		boolean firstIsEmpty = true;
		boolean secondIsEmpty = true;

		do {
			if ((first != null) && !first.isEmptyBuffer()) {
				packet = first.popPacket();
				if (second != null) {
					makeDelay();
					second.receivePacket(packet);
				}
				firstIsEmpty = first.isEmptyBuffer();
			}

			if ((second != null) && !second.isEmptyBuffer()) {
				packet = second.popPacket();
				if (first != null) {
					makeDelay();
					first.receivePacket(packet);
				}
				secondIsEmpty = second.isEmptyBuffer();
			}

		} while (!firstIsEmpty || !secondIsEmpty);
	}

	private void makeDelay() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException ex) {
			// ok
		}
	}
}
