package shared.SimulatorEvents.SerializedComponents;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public enum PacketType {
	DHCP,		// blue
	DNS,		// blue
	UDP,        // blue	
    TCP,        // green
    ICMP,       // gray
    ARP,        // yellow
    GENERIC,    // pink
	ETHERNET,	// shouldn't happen, black
	IP;		    // shouldn't happen, black
}
