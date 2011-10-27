/*
 * Erstellt am 27.10.2011.
 */

package networkModule;

import psimulator2.WorkerThread;

/**
 *
 * @author neiss
 */
public abstract class Layer extends WorkerThread {

    protected NetworkModule networkModule;

    public Layer(NetworkModule networkModule) {
        this.networkModule = networkModule;
    }

    public NetworkModule getNetworkModule() {
        return networkModule;
    }
}
