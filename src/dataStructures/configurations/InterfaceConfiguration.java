package dataStructures.configurations;

import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;

/**
 * Data structure containing information parsed from the networking configuration file
 * 
 * @author Michal Horacek
 */
public class InterfaceConfiguration {
	public String ifaceName;
	public String type;
	public String inetType;
	public IpAddress address;
	public IpNetmask mask;
	public IpAddress broadcast;
        public IpAddress gateway;
}
