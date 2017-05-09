package dataStructures.configurations;

import dataStructures.ipAddresses.IpAddress;

/**
 *
 * @author Michal Horacek
 */
public class DhcpRange {
    public IpAddress rangeStart;
    public IpAddress rangeEnd;
    
    public DhcpRange(IpAddress start, IpAddress end) {
        this.rangeStart = start;
        this.rangeEnd   = end;
    }
}
