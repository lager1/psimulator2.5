/*
 * Erstellt am 20.3.2012.
 */

package physicalModule;

import org.jnetpcap.Pcap;

/**
 *
 * @author Tomas Pitrinec
 */
public class PacketCatcher implements Runnable {

	Pcap pcap;
	RealSwitchport swport;
	Thread myThread;

	
	public PacketCatcher(Pcap pcap, RealSwitchport swport) {
		this.pcap = pcap;
		this.swport = swport;
		myThread = new Thread(this);
		myThread.start();
	}



	@Override
	public void run() {

	}

}
