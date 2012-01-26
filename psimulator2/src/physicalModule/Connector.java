/*
 * created 26.1.2012
 */
package physicalModule;

/**
 * One end of the cable.
 * When someone wants to connect cable to interface, he must connect this connector (of cable) to interface.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Connector {

	private AbstractInterface iface;
	private Cable cable;

	public Connector(Cable cable) {
		this.cable = cable;
	}

	public Cable getCable() {
		return cable;
	}

	public AbstractInterface getInterface() {
		return iface;
	}

	/**
	 * Connects given interface to connenctor.
	 *
	 * @param iface
	 * @return true if successfully connected, false when there is already connected interface.
	 */
	public boolean connectInterface(AbstractInterface iface) {
		if (iface != null) {
			return false;
		}
		this.iface = iface;
		iface.connector = this;
		return true;
	}

	/**
	 * Releases connector from interaface.
	 *
	 * @return true, if released; false if connector is already disconnected.
	 */
	public boolean disconnectInterface() {
		if (iface != null) {
			iface = null;
			return true;
		}
		return false;
	}
}