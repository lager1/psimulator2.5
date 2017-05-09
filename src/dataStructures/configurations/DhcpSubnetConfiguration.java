package dataStructures.configurations;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Michal Horacek
 */
public class DhcpSubnetConfiguration {

    public IPwithNetmask subnetAndNetmask;
    public IpNetmask subnetMask;
    public List<DhcpRange> ranges;
    public HashMap<String, String> options;
    
    public DhcpSubnetConfiguration() {
        ranges = new ArrayList<>();
    }
}
