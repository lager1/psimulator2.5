/*
 * created 1.2.2012
 */
package networkModule.L4;

import applications.Application;
import dataStructures.IpPacket;
import dataStructures.ipAddresses.IpAddress;
import java.util.HashMap;
import java.util.Map;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.Layer;
import networkModule.TcpIpNetMod;

/**
 * Implementace transportni vrstvy sitovyho modulu. <br />
 * Nebezi v vlastnim vlakne, je to vlastne jen rozhrani mezi aplikacema a 3. vrstvou.
 *
 * Pozor: Porty jsou sdileny napric protokoly (ICMP, TCP, UDP), proto nelze poslouchat na 80 pres TCP, UDP a ICMP najednout.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TransportLayer extends Layer implements Loggable {

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
		super(netMod);
		this.icmphandler = new IcmpHandler(netMod);
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
		applications.get(port).receivePacket(packet);
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
//		TODO: tady se to bude cyklit, pokud budou pouzivany vsechny porty
		if (applications.containsKey(portCounter)) {
			portCounter++;
			return getFreePort();
		} else {
			return portCounter++;
		}
	}
}
