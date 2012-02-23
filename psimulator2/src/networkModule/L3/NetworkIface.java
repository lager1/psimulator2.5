/*
 * Erstellt am 22.2.2012.
 */

package networkModule.L3;

/**
 * Representation of network interface in 3th layer in Network Module.
 * Representuje prakticky jen nastaveni interfacu, samotna trida nic nedela.
 * @author neiss
 */
public class NetworkIface {
	
	public final String name;
	public boolean isUp=true; //je nahozene

	public NetworkIface(String name) {
		this.name = name;
	}
	
	

}
