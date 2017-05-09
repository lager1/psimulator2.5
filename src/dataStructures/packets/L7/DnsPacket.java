package dataStructures.packets.L7;

import dataStructures.packets.L7.dns.DnsAnswer;
import dataStructures.packets.L7.dns.DnsQuestion;
import dataStructures.packets.PacketData;

import java.util.ArrayList;
import java.util.List;

import shared.SimulatorEvents.SerializedComponents.PacketType;

/**
 * @author Michal Horacek
 */
public class DnsPacket implements PacketData {

    //TODO ID
    public int qsCount;
    public int anCount;
    public int authCount;
    public int addCount;
    public DnsQuestion question;
    public List<DnsAnswer> answers;
    public List<DnsAnswer> authority;
    public List<DnsAnswer> additional;
    public DnsPacketType type;
    public boolean authoritativeAnswer;
    public DnsStatus status;

    public DnsPacket(DnsPacketType type, int qsCount, DnsQuestion question) {
        this.type = type;
        this.qsCount = qsCount;
        this.question = question;
        this.addCount = 0;
        this.authCount = 0;
        this.anCount = 0;
        this.answers = new ArrayList<>();
        this.authority = new ArrayList<>();
        this.additional = new ArrayList<>();
        this.status = DnsStatus.NO_ERROR;
        this.authoritativeAnswer = false;
    }

    @Override
    public int getSize() {
        // odhad velikosti paketu
        // header 12 + priblizne 20 za kazdy posilany zaznam
        return 12 + 20 * (1 + answers.size() + authority.size() + additional.size());
    }

    public boolean containsData(String cmp, List<DnsAnswer> section) {
        for (DnsAnswer answer : section) {
            if (answer.aData.equals(cmp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getEventDesc() {
        String s = "=== DNS === \n";
        s += "type: " + type + "  ";
        s += "qsCount: " + qsCount + "  ";
        s += "anCount: " + answers.size() + "  ";
        s += "authCount: " + authority.size() + "  ";
        s += "addCount: " + additional.size() + "  ";
        s += "\n";
        s += "QUESTION SECTION:\n";
        s += question.qName + "\t" + question.qClass + "\t" + question.qType;
        s += "\n";
        if (answers.size() > 0) {
            s += "ANSWER SECTION:\n";
            for (DnsAnswer answer : answers) {
                s += answer.aName + "\t" + answer.aClass + "\t"
                        + answer.aType + "\t" + answer.aData;
                s += "\n";
            }
        }
        if (authority.size() > 0) {
            s += "AUTHORITY SECTION\n";
            for (DnsAnswer answer : authority) {
                s += answer.aName + "\t" + answer.aClass + "\t"
                        + answer.aType + "\t" + answer.aData;
                s += "\n";
            }
        }
        if (additional.size() > 0) {
            s += "ADDITIONAL SECTION\n";
            for (DnsAnswer answer : additional) {
                s += answer.aName + "\t" + answer.aClass + "\t"
                        + answer.aType + "\t" + answer.aData;
                s += "\n";
            }
        }
        return s;
    }

    @Override
    public PacketType getPacketEventType() {
        return PacketType.DNS;
    }

    public enum DnsPacketType {

        QUERY,
        ANSWER,
    }

    public enum DnsStatus {

        /**
         * No error occurred during processing the query
         */
        NO_ERROR,
        /**
         * Server was unable to interpret the query
         */
        FORMAT_ERROR,
        /**
         * Server problem while processing query
         */
        SERVER_FAILURE,
        /**
         * Queried domain name does not exist on corresponding authoritative
         * server
         */
        NAME_ERROR,

        /**
         * Server does not support this kind of query
         */
        NOT_IMPLEMENTED,
    }
}
