/*
 * created 31.1.2012
 */
package networkModule.L3;

import dataStructures.MacAddress;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import java.util.HashMap;
import java.util.Set;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L2.EthernetInterface;

/**
 * Represents ARP cache table.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class ArpCache implements Loggable {

	private final Device device;
	/**
	 * Valid record time in ms.
	 */
	private int validRecordTime = 20_000;
	/**
	 * HashMap of records. <br/>
	 * Key - IP address <br />
	 * Value - ArpRecord
	 */
	private HashMap<IpAddress, ArpRecord> cache = new HashMap<>();

	public ArpCache(Device device) {
		this.device = device;
	}

	public class ArpRecord {

		public final long timeStamp;
		public final MacAddress mac;
		public final EthernetInterface iface;

		public ArpRecord(MacAddress mac, EthernetInterface iface) {
			this.timeStamp = System.currentTimeMillis();
			this.mac = mac;
			this.iface = iface;
		}
	}

	/**
	 * For platform formatted output only.
	 *
	 * @return
	 */
	public HashMap<IpAddress, ArpRecord> getCache() {
		return cache;
	}

	/**
	 * Returns MacAddres iff there is a record pair IpAddress with MacAddress AND timeout is not off. <br />
	 * It removes ArpRecord and returns null, if timeout is off. <br />
	 * Return null if there is no record with given IpAddress.
	 *
	 * @param ip
	 * @return
	 */
	public MacAddress getMacAdress(IpAddress ip) { // TODO: tady bych spravne mel resit i rozhrani!!! a delat neco jen kdyz to odpovida
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
		Logger.log(this, Logger.INFO, LoggingCategory.ARP_CACHE, "Updating ARP cache: IP: "+ip.toString()+" MAC: "+mac, null);
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
		long time = now - record.timeStamp;
		if (time > validRecordTime) {
			// cisco default is 14400s, here it has to be much smaller,
			// because when someone change his IP address a his neighbour begins to send packets to him, he should ask again
			// with ARP req
			Logger.log(this, Logger.INFO, LoggingCategory.ARP_CACHE, "Deleting old record for IP: "+ip+" and MAC: "+record.mac+ " out of date = "+(time-validRecordTime) + " ms.", null);
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

	@Override
	public String getDescription() {
		return device.getName()+" ArpCache";
	}
}
