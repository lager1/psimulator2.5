/*
 * created 28.10.2011
 */
package applications;

import dataStructures.packets.IpPacket;
import dataStructures.PacketItem;
import device.Device;
import java.net.NetworkInterface;
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
 * Represents network application which listens on specified port. Aplikace bezi v jednom vlakne, posloucha a obsluhuje
 * pozadavky. Pro aplikaci, ktera sama od sebe neco dela (nejen obsluhuje pozadavky), bude jeste jina abstraktni trida
 * od tyhle podedena.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 * @author Tomas Pitrinec
 */
public abstract class Application implements SmartRunnable, Loggable {

// parametry aplikace:

	public final int PID;
	public final String name;
	protected final Device device;
	protected WorkerThread worker;
	protected Integer port = null; // nebude potreba seznam portu?
	protected final TransportLayer transportLayer;

	/**
	 * buffer prichozich paketu ze site
	 */
	protected final List<PacketItem> buffer = Collections.synchronizedList(new LinkedList<PacketItem>());

	/**
	 * Jestli aplikace prave bezi
	 */
	private boolean running = false;


	public Application(String name, Device device) {
		this.name = name;
		this.device = device;

		this.PID = device.getFreePID();
		if (device.getNetworkModule().isStandardTcpIpNetMod()) {
			this.transportLayer =  ((TcpIpNetMod) device.getNetworkModule()).transportLayer;
		} else {
			Logger.log(this, Logger.ERROR, LoggingCategory.GENERIC_APPLICATION, "Vytvari se sitova aplikace pro device, ktery nema TcpIpNetMod!", null);
			this.transportLayer = null;
		}
	}

	/**
	 * Predavani paketu ze site aplikaci.
	 * @param packet
	 */
	public void receivePacket(PacketItem packetItem) {
		if (running) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.GENERIC_APPLICATION, getName() + "Prisel paket", packetItem.packet);
			buffer.add(packetItem);
			worker.wake();
		} else {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_APPLICATION, getName() + "Prisel paket, ackoliv aplikace jiz ", packetItem.packet);
		}
	}





// abstraktni metody, ktery bude nutno implementovat v potomcich: ----------------------------------------------------

	/**
	 * Implement this function to run some commands right before application start. <br />
	 * (treba pro nejake kontroly, vypisy atd.)
	 */
	protected abstract void atStart();

	/**
	 * Implement this function to run some commands right after application exit. <br />
	 * (treba pro nejake vypisy pri ukonceni aplikace)
	 */
	protected abstract void atExit();

	/**
	 * Sem se davaj veci, ktery musi konkretni aplikace udelat v kazdym pripade, tj. i kdyz je zabita.
	 */
	protected abstract void atKill();



// verejny metody na startovani a ukoncovani: ----------------------------------------------------------------------

	/**
	 * Starts aplication by turning on listening on port.
	 */
	public synchronized void start() {
		if (running) {
			Logger.log(this, Logger.WARNING, LoggingCategory.GENERIC_APPLICATION, getName() + "Znovu spustena jiz bezici aplikace, to by nemelo nikdy nastat.", null);
		} else {
			running = true;
			this.worker = new WorkerThread(this);
			device.registerApplication(this);
			this.port = transportLayer.registerApplication(this, port);
			atStart();
		}
	}

	/**
	 * Exit the application. <br />
	 * Muze to zavolat jak sama ta aplikace (predevsim dvouvlaknova), tak nekdo zvenku, proto synchronized.
	 */
	public synchronized void exit() {
		if (running) {
			running = false;
			priUkonceni();
			atExit();
			atKill();
			Logger.log(this, Logger.DEBUG, LoggingCategory.GENERIC_APPLICATION, getName() + " exit", null);
		}
	}

	/**
	 * Exit the application without calling atExit(). <br />
	 * Muze to zavolat jak sama ta aplikace (predevsim dvouvlaknova), tak nekdo zvenku, proto synchronized.
	 */
	public synchronized void kill() {
		if (running) {
			running = false;
			priUkonceni();
			atKill();
			Logger.log(this, Logger.DEBUG, LoggingCategory.GENERIC_APPLICATION, getName() + " kill", null);
		}
	}

	/**
	 * Jen vytazeny spolecny veci ze dvou predchozich metod.
	 */
	private void priUkonceni(){
		transportLayer.unregisterApplication(port);
		worker.die();
		device.unregisterApplication(this);
	}


//gettry a settry: --------------------------------------------------------------------------------------------------

	public void setPort(int port) {
		this.port = port;
	}

	public Integer getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	public int getPID() {
		return PID;
	}

	public boolean isRunning() {
		return running;
	}

}
