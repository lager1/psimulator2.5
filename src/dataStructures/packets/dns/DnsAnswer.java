/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures.packets.dns;

import dataStructures.ipAddresses.IpAddress;

/**
 *
 * @author Michal Horacek
 */
public class DnsAnswer {
	public String aName;
	public DnsType aType;
	public String aClass;
	public String aData;
	
	public DnsAnswer(String name, DnsType type, String addr) {
		this.aName = name;
		this.aType = type;
		this.aData = addr;
		this.aClass = "IN";
	}
}
