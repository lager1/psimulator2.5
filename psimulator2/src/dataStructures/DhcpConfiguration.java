/*
 * Erstellt am 4.4.2012.
 */

package dataStructures;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;

/**
 *
 * @author Tomas Pitrinec
 */
public class DhcpConfiguration {

	IPwithNetmask subnetAndNetmask;

	/**
	 * Only in server configuration
	 */
	IpAddress rangeStart;

	/**
	 * Only on server configuration.
	 */
	IpAddress rangeEnd;

	IpAddress routers;

	IpAddress broadcast;

	

}
