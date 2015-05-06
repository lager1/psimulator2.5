package dataStructures.configurations;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import dataStructures.ipAddresses.IpNetmask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Michal Horacek
 */
public class DhcpServerConfiguration {

    public IpAddress broadcast = null;
    public HashMap<String, String> options;
    public ArrayList<DhcpSubnetConfiguration> subnets;
    private HashMap<IPwithNetmask, LinkedList<IPwithNetmask>> addresses;
    public String defaultLeaseTime = "7200";
    public String maxLeaseTime = "7200";

    public DhcpServerConfiguration() {
        subnets = new ArrayList<>();
        options = new HashMap<>();
        addresses = null;
    }

    public void addSubnet(String subnet, String netMask,
                          ArrayList<DhcpConfigRange> ranges, HashMap<String, String> subnetOptions) {

        DhcpSubnetConfiguration sub = new DhcpSubnetConfiguration();

        // Vytvoreni prislusnych objektu ze stringu.
        // Pokud nektery string neni validni adresa nebo maska, vraci se null
        IpAddress addr = IpAddress.correctAddress(subnet);
        sub.subnetMask = IpNetmask.correctNetmask(netMask);
        sub.options = new HashMap<>(subnetOptions);

        // Kontrola, zda zadane udaje jsou validni IP adresy a maska
        if (addr == null || sub.subnetMask == null) {
            return;
        }

        sub.subnetAndNetmask = new IPwithNetmask(addr, sub.subnetMask);

        for (DhcpConfigRange range : ranges) {
            IpAddress start = IpAddress.correctAddress(range.rangeStart);
            IpAddress end = IpAddress.correctAddress(range.rangeEnd);

            // kontrola, ze start a end jsou validni adresy
            if (start == null || end == null) {
                continue;
            }

            // Kontrola, zda pridelovane adresy rangeStart az rangeEnd spadaji do dane podsite
            if (!sub.subnetAndNetmask.isInMyNetwork(start)
                    || !sub.subnetAndNetmask.isInMyNetwork(end)) {
                continue;
            }

            // Pokud je adresa rangeEnd nizsi nez rangeStart, ulozi se jako rozsah adres pro podsit
            // jen jedina adresa - rangeStart
            if (start.getBits() > end.getBits()) {
                end = new IpAddress(end.getByteArray());
            }

            sub.ranges.add(new DhcpRange(start, end));
        }


        //sub.currentAddress = sub.rangeStart;
        subnets.add(sub);
    }

    private void initAddresses() {
        addresses = new HashMap<>();
        LinkedList<IPwithNetmask> q;
        IpAddress currentAddress;

        for (DhcpSubnetConfiguration subnet : subnets) {
            q = new LinkedList<>();

            for (DhcpRange range : subnet.ranges) {
                currentAddress = range.rangeStart;

                while (!currentAddress.equals(IpAddress.nextAddress(range.rangeEnd))) {
                    q.add(new IPwithNetmask(currentAddress, subnet.subnetMask));
                    currentAddress = IpAddress.nextAddress(currentAddress);
                }
            }

            addresses.put(subnet.subnetAndNetmask.getNetworkNumber(), q);
        }
    }

    public HashMap<IPwithNetmask, LinkedList<IPwithNetmask>> getAddressess() {
        if (addresses == null) {
            initAddresses();
        }

        return addresses;
    }

    public DhcpSubnetConfiguration getSubnetConfiguration(IPwithNetmask addr) {
        for (DhcpSubnetConfiguration subnetConf : subnets) {
            if (subnetConf.subnetAndNetmask.isInMyNetwork(addr.getIp())
                    && subnetConf.subnetMask.equals(addr.getMask())) {
                return subnetConf;
            }
        }

        return null;
    }
}
