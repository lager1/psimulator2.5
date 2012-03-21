/*
 * Erstellt am 20.3.2012.
 */

package physicalModule;

import dataStructures.L2Packet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.jnetpcap.Pcap;
import utils.SmartRunnable;
import utils.WorkerThread;

/**
 *
 * @author Tomas Pitrinec
 */
public class PacketSender implements SmartRunnable {

	Pcap pcap;
	RealSwitchport swport;
	WorkerThread worker;
	private final List<L2Packet> buffer = Collections.synchronizedList(new LinkedList<L2Packet>());



// konstruktory: ----------------------------------------------------------------------------------------------------

	public PacketSender(Pcap pcap, RealSwitchport swport) {
		this.pcap = pcap;
		this.swport = swport;
		worker = new WorkerThread(this);
	}





// verejny metody pro prijimani paketu od jinejch vlaken:

	public void sendPacket(L2Packet packet){
		buffer.add(packet);
		worker.wake();
	}





// privatni metody pro sitovou komunikaci: -----------------------------------------------------

	private void sendOnRealInface(L2Packet packet){
		throw new UnsupportedOperationException("Not supported yet.");
	}




// Ostatni verejny metody: ------------------------------------------------------------------------------------------

	@Override
	public void doMyWork() {
		while (!buffer.isEmpty()){
			sendOnRealInface(buffer.remove(0));
		}
	}

	@Override
	public String getDescription() {
		return swport.physicMod.device.getName()+": PacketSender";
	}

}
