/*
 * created 1.2.2012
 */
package networkModule.L4;

import applications.Application;
import dataStructures.IpPacket;
import java.util.HashMap;
import java.util.Map;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.TcpIpNetMod;

/**
 * Implementace transportni vrstvy sitovyho modulu. <br />
 * Nebezi v vlastnim vlakne, je to vlastne jen rozhrani mezi aplikacema a 3. vrstvou.
 *
 * Pozor: Porty jsou sdileny napric protokoly (ICMP, TCP, UDP), proto nelze poslouchat na 80 pres TCP, UDP a ICMP najednout.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TransportLayer implements Loggable {

	public final TcpIpNetMod netMod;
	public final IcmpHandler icmphandler;
	/**
	 * List of registred applications. <br />
	 * Key - port or session number <br />
	 * Value - listening application
	 *
	 */
	private final Map<Integer, Application> applications = new HashMap<>();
	private int portCounter = 1025;
	private static final int portMAX = 65_535;

	public TransportLayer(TcpIpNetMod netMod) {
		this.netMod = netMod;
		this.icmphandler = new IcmpHandler(this);
	}

	public IPLayer getIpLayer() {
		return netMod.ipLayer;
	}

	/**
	 * This method should be called from L4.
	 * @param packet
	 */
	public void receivePacket(IpPacket packet) {
		if (packet.data == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Prisel mi sem paket, ktery nema zadna L4 data! Zahazuji packet:", packet);
			return;
		}

		switch (packet.data.getType()) {
			case ICMP:
				Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Prisel ICMP paket", packet);
				icmphandler.handleReceivedIcmpPacket(packet);
				break;

			case TCP:
				Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "TCP handler neni implementovan! Zahazuji packet:", packet);
				break;

			case UDP:
				Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "UDP handler neni implementovan! Zahazuji packet:", packet);
				break;
			default:
				Logger.log(this, Logger.WARNING, LoggingCategory.TRANSPORT, "Prisel mi sem paket neznameho L4 typu: Zahazuji packet:", packet);
		}
	}

	/**
	 * Forward incomming packet to application listening on given port.
	 *
	 * @param packet
	 * @param port
	 */
	protected void forwardPacketToApplication(IpPacket packet, int port) {
		Application app = applications.get(port);
		if (app != null) {
			app.receivePacket(packet);
		} else {
			Logger.log(this, Logger.IMPORTANT, LoggingCategory.TRANSPORT, "Zahazuju paket, protoze neni tu zaregistrovana zadna aplikace, ktera by ho obslouzila. Neobsluhovany port: "+port, packet);
		}
	}

	/**
	 * Register application to listen and accept packets.
	 *
	 * @param app to register
	 * @param port listen on this port
	 */
	public int registerApplication(Application app, Integer port) {
		if (port == null) {
			port = getFreePort();
		}
		applications.put(port, app);
		return port;
	}

	/**
	 * Unregister application from transport layer. <br />
	 * Application is specified by port number.
	 *
	 * @param port
	 */
	public void unregisterApplication(int port) {
		applications.remove(port);
	}

	@Override
	public String getDescription() {
		return netMod.getDevice().getName() + ": TcpIpLayer";
	}

	/**
	 * Returns unused port number.
	 *
	 * @return
	 */
	private Integer getFreePort() {
		if (portCounter > portMAX) {
			portCounter = portCounter - portMAX + 1024;
			Logger.log(this, Logger.INFO, LoggingCategory.TRANSPORT, "Resetting portCounter.", null);
		}
//		TODO: ranna implementace! tady se to bude cyklit, pokud budou pouzivany vsechny porty
		if (applications.containsKey(portCounter)) {
			portCounter++;
			return getFreePort();
		} else {
			return portCounter++;
		}
	}
}
