/*
 * created 31.1.2012
 */
package networkModule.L3;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IpAddress;
import java.util.HashMap;
import java.util.Set;
import networkModule.L2.EthernetInterface;

/**
 * Represents ARP cache table.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
class ArpCache {

	class ArpRecord {

		long timeStamp;
		MacAddress mac;
		EthernetInterface iface;

		public ArpRecord(MacAddress mac, EthernetInterface iface) {
			this.timeStamp = System.currentTimeMillis();
			this.mac = mac;
			this.iface = iface;
		}
	}
	private HashMap<IpAddress, ArpRecord> cache = new HashMap<IpAddress, ArpRecord>();

	/**
	 * For platform formatted output only.
	 * @return
	 */
	public HashMap<IpAddress, ArpRecord> getCache() {
		return cache;
	}

	/**
	 * Returns MacAddres iff there is a record pair IpAddress <> MacAddress AND timeout is not off. It removes ArpRecord
	 * and returns null, if timeout is off. Return null if there is no record with given IpAddress.
	 *
	 * @param ip
	 * @return
	 */
	public MacAddress getMacAdress(IpAddress ip) {
		ArpRecord record = getRecord(ip);
		if (record == null) {
			return null;
		}

		return record.mac; // since MacAddress and IpAddress is never changed I don't have return copy of it
	}

	/**
	 * Updates ARP cache.
	 *
	 * @param ip key for update
	 * @param mac value
	 */
	public void updateArpCache(IpAddress ip, MacAddress mac, EthernetInterface iface) {
		ArpRecord record = new ArpRecord(mac, iface);
		cache.put(ip, record);
	}

	/**
	 * Returns ArpRecord iff there is a record pair IpAddress <> MacAddress AND timeout is not off. It removes ArpRecord
	 * and returns null, if timeout is off. Return null if there is no record with given IpAddress.
	 *
	 * @param ip
	 * @return
	 */
	private ArpRecord getRecord(IpAddress ip) {
		ArpRecord record = cache.get(ip);

		if (record == null) {
			return null;
		}

		long now = System.currentTimeMillis();
		if ((now - record.timeStamp) > 14400000) { // cisco default is 14400s
			cache.remove(ip);
			return null;
		}

		return record;
	}

	@Override
	public String toString() {
		String s = "";
		MacAddress mac;
		Set<IpAddress> set = cache.keySet();
		for (IpAddress ip : set) {
			mac = getMacAdress(ip);
			if (mac != null) {
				s += ip + "\t" + mac + "\n";
			}
		}
		return s;
	}
}
