/*
 * created 28.10.2011
 */
package physicalModule;

import java.util.ArrayList;
import java.util.List;
import networkModule.NetworkModule;
import physicalModule.AbstractInterface;
import psimulator2.WorkerThread;

/**
 * Seznam sitovych rozhrani reprezentujici fyzicke rozhrani
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class PhysicalModule extends WorkerThread {

	private List<AbstractInterface> interfaceList;
	private NetworkModule networkModule;

	public PhysicalModule(NetworkModule networkModule, List<AbstractInterface> ifaces) {
		this.networkModule = networkModule;
		this.interfaceList = ifaces;
	}

	public PhysicalModule(NetworkModule networkModule) {
		this.networkModule = networkModule;
		interfaceList = new ArrayList<AbstractInterface>();
	}

	public void addInterface(AbstractInterface iface) {
		interfaceList.add(iface);
	}

	public boolean removeInterface(AbstractInterface iface) {
		return interfaceList.remove(iface);
	}

	@Override
	protected void doMyWork() { // TODO: dopsat obsluhu prichozich paketu - jen jedno kolecko nebo cyklus?

		for (AbstractInterface iface : interfaceList) {
			if (!iface.isBufferEmpty()) {
				networkModule.acceptPacket(iface.getL2PacketFromBuffer(), iface);
			}
		}

		throw new UnsupportedOperationException("Not implemented completaly yet.");
	}
}
