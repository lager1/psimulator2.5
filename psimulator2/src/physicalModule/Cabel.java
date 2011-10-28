/*
 * created 28.10.2011
 */
package physicalModule;

import networkDataStructures.L2Packet;

/**
 * Represents cabel
 * 
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class Cabel {
	AbstractNetworkInterface first;
	AbstractNetworkInterface second;

	/**
	 * Delay in millisends
	 */
	long delay;
	
	public Cabel() {
		this.delay = (long) Math.random() * 10;
	}
	
	public void setFirstInterface(AbstractNetworkInterface iface) {
		this.first = iface;
	}
	
	public void setSecondInterface(AbstractNetworkInterface iface) {
		this.second = iface;
	}
	
	/**
	 * Transport packet to the interface at the end of the cabel.
	 * @param packet for transport
	 * @param iface source inteface (sending interface)
	 * @return 
	 */
	public boolean transportPacket(L2Packet packet, AbstractNetworkInterface iface) {
		AbstractNetworkInterface target = getTarget(iface);
		if (target == null) return false;
		
		makeDelay();
		
		target.addIncomingPacketToBuffer(packet);
		return true;
	}
	
	/**
	 * Returns the other interface (interface at the end of the cabel).
	 * @param iface sending interface
	 * @return 
	 */
	private AbstractNetworkInterface getTarget(AbstractNetworkInterface iface) {
		if (first.equals(iface)) {
			return second;
		} else {
			return first;
		}
	}

	private void makeDelay() {
		// TODO: implement it
	}
}
