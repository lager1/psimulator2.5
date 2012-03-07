/*
 * created 28.10.2011
 */
package physicalModule;

import dataStructures.L2Packet;
import logging.Logger;
import logging.LoggingCategory;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents cable
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Cable implements SmartRunnable {

	public final int configID;	// id z konfiguraku
	/**
	 * ID of first connector device.
	 * (pro posilani paketu Martinovi)
	 */
	private int idFirstDevice;
	/**
	 * ID of second connector device.
	 * (pro posilani paketu Martinovi)
	 */
	private int idSecondDevice;

	private SimulatorSwitchport firstCon;
	private SimulatorSwitchport secondCon;

	WorkerThread worker = new WorkerThread(this);

	/**
	 * Delay in milliseconds
	 */
	private long delay;

	/**
	 * Creates cable with random delay time.
	 * @param configID
	 */
	public Cable(int configID) {
		this.configID = configID;
		this.delay = (long) Math.random() * 10;
	}

	/**
	 * Creates cable with given delay time.
	 * @param id
	 * @param delay
	 */
	public Cable(int id, long delay) {
		this.configID = id;
		this.delay = delay;
	}

	/*
	 * Sets first connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public void setFirstSwitchport(SimulatorSwitchport swport) {
		swport.cabel = this;
		this.firstCon = swport;
	}

	/*
	 * Sets second connector with given interface.
	 * Also sets interface's connector to correct connector.
	 * @param iface cannot be null
	 * @return true if connector was empty and now is connected to interface.
	 */
	public void setSecondSwitchport(SimulatorSwitchport swport) {
		swport.cabel = this;
		this.secondCon = swport;
	}

	@Override
	public void doMyWork() {
		L2Packet packet;
		boolean firstIsEmpty = true;
		boolean secondIsEmpty = true;

		do {
			Switchport first = firstCon; // mohlo by to byt vne while-cyklu, ale co kdyz nekdo zapoji kabel (konektor) do rozhrani a my budem chtit, aby se to rozjelo?
			Switchport second = secondCon;

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

	public void setFirstDeviceId(Integer id) {
		this.idFirstDevice = id;
	}

	public void setSecondDeviceId(Integer id) {
		this.idSecondDevice = id;
	}

	/**
	 * Vraci to switchport na druhym konci kabelu nez je ten zadanej.
	 * Returns switchport on the other end of the cable, where is the given.
	 * @param one
	 * @return
	 */
	public SimulatorSwitchport getTheOtherSwitchport(Switchport one) {
		if (one == firstCon) {
			return secondCon;
		} else if (one == secondCon) {
			return firstCon;
		} else {
			Logger.log(Logger.ERROR, LoggingCategory.PHYSICAL, "Byla spatne zavolana metoda getTheOtherSwitchport na kabelu s configID " + configID);
			return null;
		}
	}
}
