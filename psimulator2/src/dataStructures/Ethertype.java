/*
 * created 26.1.2012
 */

package dataStructures;

/**
 * Ethertype of Ethernet II frame.
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public enum Ethertype {
	IPv4,
	IPv6,
	ARP,
	WoL,
	RARP,
	MAC_CONTROL;
}
