/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents cable
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Cable implements SmartRunnable {

	public final int id;	// id z konfiguraku

	private final Connector firstCon = new Connector(this);
	private final Connector secondCon = new Connector(this);

	WorkerThread worker = new WorkerThread(this);

	/**
	 * Delay in milliseconds
	 */
	private long delay;

	/**
	 * Creates cable with random delay time.
	 * @param id
	 */
	public Cable(int id) {
		this.id = id;
		this.delay = (long) Math.random() * 10;
	}

	/**
	 * Creates cable with given delay time.
	 * @param id
	 * @param delay
	 */
	public Cable(int id, long delay) {
		this.id = id;
		this.delay = delay;
	}

	/*
	 * Sets first connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public boolean setFirstInterface(SimulatorSwitchport swport) {
		assert swport != null;
		boolean res = firstCon.connectInterface(swport);
		if (res) {
			swport.connector = firstCon;
		}
		return res;
	}

	/*
	 * Sets second connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public boolean setSecondInterface(SimulatorSwitchport swport) {
		assert swport != null;
		boolean res = secondCon.connectInterface(swport);
		if (res) {
			swport.connector = secondCon;
		}
		return res;
	}

	@Override
	public void doMyWork() {
		L2Packet packet;
		boolean firstIsEmpty = true;
		boolean secondIsEmpty = true;

		do {
			Switchport first = firstCon.getInterface(); // mohlo by to byt vne while-cyklu, ale co kdyz nekdo zapoji kabel (konektor) do rozhrani a my budem chtit, aby se to rozjelo?
			Switchport second = secondCon.getInterface();

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
