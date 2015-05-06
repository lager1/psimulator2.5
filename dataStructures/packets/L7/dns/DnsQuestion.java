/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures.packets.L7.dns;

/**
 *
 * @author miohal
 */
public class DnsQuestion {
    public String qName;
    public DnsType qType;
    public String qClass;
    
    public DnsQuestion(String query) {
        this.qName = query;
        this.qType = DnsType.A;
        this.qClass = "IN";
    }
    
    public DnsQuestion(String query, DnsType type) {
        this.qName = query;
        this.qType = type;
        this.qClass = "IN";
    }
}
