/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import java.util.List;
import dataStructures.L2Packet;
import physicalModule.AbstractSwitchport;
import psimulator2.SmartRunnable;
import psimulator2.WorkerThread;

/**
 *
 * @author neiss
 */
public abstract class L3layer extends Layer implements SmartRunnable {

	private WorkerThread worker = new WorkerThread(this);

    public L3layer(NetMod netMod) {
        super(netMod);
    }

    /**
     * Prijima pakety od linkovy vrstvy.
     */
    public abstract void receivePacket();




}
