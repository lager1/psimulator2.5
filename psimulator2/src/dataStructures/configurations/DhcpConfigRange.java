package dataStructures.configurations;

/**
 * Helper class used to load subnet range configuration while parsing dhcpd.conf file
 * 
 * @author Michal Horacek
 */
public class DhcpConfigRange {

    public String rangeStart;
    public String rangeEnd;
    
    public DhcpConfigRange(String start, String end) {
        this.rangeStart = start;
        this.rangeEnd   = end;
    }
}