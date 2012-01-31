/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import psimulator2.SmartRunnable;
import psimulator2.WorkerThread;

/**
 * Represents cable
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Cable implements SmartRunnable {

	Connector firstCon = new Connector(this);
	Connector SecondCon = new Connector(this);

	WorkerThread worker = new WorkerThread(this);

	/**
	 * Delay in milliseconds
	 */
	private long delay;

	/**
	 * Creates cable with random delay time.
	 */
	public Cable() {
		this.delay = (long) Math.random() * 10;
	}

	/**
	 * Creates cable with given delay time.
	 * @param delay
	 */
	public Cable(long delay) {
		this.delay = delay;
	}

	/*
	 * Sets first connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public boolean setFirstInterface(Switchport iface) {
		assert iface != null;
		boolean res = firstCon.connectInterface(iface);
		if (res) {
			iface.connector = firstCon;
		}
		return res;
	}

	/*
	 * Sets second connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public boolean setSecondInterface(Switchport iface) {
		assert iface != null;
		boolean res = SecondCon.connectInterface(iface);
		if (res) {
			iface.connector = SecondCon;
		}
		return res;
	}

	public void doMyWork() {
		L2Packet packet;
		boolean firstIsEmpty = true;
		boolean secondIsEmpty = true;

		do {
			Switchport first = firstCon.getInterface(); // mohlo by to byt vne while-cyklu, ale co kdyz nekdo zapoji kabel (konektor) do rozhrani a my budem chtit, aby se to rozjelo?
			Switchport second = SecondCon.getInterface();

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
