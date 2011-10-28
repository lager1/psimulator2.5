/*
 * created 28.10.2011
 */
package networkDevice;

import java.util.ArrayList;
import java.util.List;
import physicalModule.AbstractNetworkInterface;
import psimulator2.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicalInterfaces extends WorkerThread {
	
	private List<AbstractNetworkInterface> ifaces;

	public PhysicalInterfaces(List<AbstractNetworkInterface> ifaces) {
		this.ifaces = ifaces;
	}
	
	public PhysicalInterfaces() {
		ifaces = new ArrayList<AbstractNetworkInterface>();
	}
	
	public void addInterface(AbstractNetworkInterface iface) {
		ifaces.add(iface);
	}
	
	public boolean removeInterface(AbstractNetworkInterface iface) {
		return ifaces.remove(iface);
	}

	@Override
	protected void doMyWork() { // TODO: dopsat obsluhu prichozich paketu - predavat je sitovemu modulu
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
