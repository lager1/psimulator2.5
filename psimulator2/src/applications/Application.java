/*
 * created 28.10.2011
 */
package applications;

import dataStructures.IpPacket;
import device.Device;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L4.TransportLayer;
import networkModule.TcpIpNetMod;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 * Represents network application which listens on specified port.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class Application implements SmartRunnable, Loggable {
	public final int PID;
	public final String name;
	protected final Device device;
	protected final WorkerThread worker;
	protected Integer port = null; // TODO: doresit, zda sem nedat seznam portu
	protected final TransportLayer transportLayer;

	protected final List<IpPacket> buffer = Collections.synchronizedList(new LinkedList<IpPacket>());


	public Application(String name, Device device) {
		this.name = name;
		this.device = device;

		this.PID = device.getFreePID();
		this.worker = new WorkerThread(this);
		if (device.getNetworkModule().isStandardTcpIpNetMod()) {
			this.transportLayer =  ((TcpIpNetMod) device.getNetworkModule()).transportLayer;
		} else {
			Logger.log(this, Logger.ERROR, LoggingCategory.GENERIC_APPLICATION, "Vytvari se sitova aplikace pro device, ktery nema TcpIpNetMod!", null);
			this.transportLayer = null;
		}
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Integer getPort() {
		return port;
	}

	/**
	 * Starts aplication by turning on listening on port.
	 * TransportLayer returns port if port is null and saves it.
	 */
	public void start() {
		this.port = transportLayer.registerApplication(this, port);
		atStart();
	}

	/**
	 * Exit the application. <br />
	 * Don't call this method from commands! Call device.exitApplication() instead.
	 */
	public void stop() {
		atExit();
		transportLayer.unregisterApplication(port);
	}

	/**
	 * Exit the application without calling atExit(). <br />
	 * Don't call this method from commands! Call device.killApplication() instead.
	 */
	public void kill() {
		transportLayer.unregisterApplication(port);
	}

	/**
	 * Implement this function to run some commands right before application start. <br />
	 * (treba pro nejake kontroly atd.)
	 */
	public abstract void atStart();

	/**
	 * Implement this function to run some commands right before application exit. <br />
	 * (treba pro nejake vypisy pri ukonceni aplikace)
	 */
	public abstract void atExit();

//	/**
//	 * Restarts the application. Calls stop() and then start()
//	 */
//	public boolean restart() {
//		boolean stop = stop();
//		boolean start = start();
//		return start && stop;
//	}

	public String getName() {
		return name;
	}

	public int getPID() {
		return PID;
	}

	public void receivePacket(IpPacket packet) {
		buffer.add(packet);
		worker.wake();
	}
}
