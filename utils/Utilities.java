/*
 * Erstellt am 6.3.2012.
 */
package utils;

import dataStructures.packets.L3.IcmpPacket;
import dataStructures.packets.L3.IpPacket;
import dataStructures.packets.L4Packet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tomas Pitrinec
 */
public class Utilities {

    public static final int deviceNameAlign = 8;

    public static boolean availablePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /*
                     * should not be thrown
                     */
                }
            }
        }

        return false;
    }

    /**
     * Prints Stack Trace to String. Prejato z http://www.rgagnon.com/javadetails/java-0029.html.
     *
     * @param e
     * @return
     */
    public static String stackToString(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "stack trace ---\r\n" + sw.toString()
                    + "---------------\r\n";
        } catch (Exception e2) {
            return "Error in stackToString";
        }
    }

    /**
     * Splits number to powers of 2
     *
     * @param c
     * @return
     */
    public static String splitToPowersOf2(int c) {
        String ret = "";
        for (int i = 0; i < 31; i++) {
            if ((c & (1 << i)) != 0) {
                if (ret.equals("")) {
                    ret += (1 << i);
                } else {
                    ret += " + " + (1 << i);
                }
            }
        }
        if (ret.equals("")) {
            ret = "0";
        }
        return ret;
    }

    /**
     * Splits number to logarithms of 2
     *
     * @param c
     * @return
     */
    public static String splitToLogarithmsOf2(int c) {
        String ret = "";
        for (int i = 0; i < 31; i++) {
            if ((c & (1 << i)) != 0) {
                if (ret.equals("")) {
                    ret += (log2(1 << i));
                } else {
                    ret += ", " + (log2(1 << i));
                }
            }
        }
        if (ret.equals("")) {
            ret = "Zadny chybovy kod nebyl zadan.";
        }
        return ret;
    }

    private static int log2(int num) {
        return (int) (Math.log(num) / Math.log(2));
    }

    public static int md(int c) {
        return (1 << c);
    }

    /**
     * Zaokrouhluje na tri desetinna mista.
     *
     * @param d
     * @return
     */
    public static double round(double d) {
        return ((double) Math.round(d * 1000)) / 1000;
    }

    public static boolean isInteger(String ret) {
        try {
            int a = Integer.parseInt(ret);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Uspi aktualni vlakno na pocet ms.
     *
     * @param ms
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            // ok
        }
    }

    /**
     * Puts current thread to sleep state for specified nanoseconds
     *
     * @param ns
     */
    public static void sleepNano(int ns) {
        try {
            Thread.sleep(0, ns);
        } catch (InterruptedException ex) {
            // ok
        }
    }

    /**
     * Dorovna zadanej String mezerama na zadanou dylku. Kdyz je String delsi nez zadana dylka, tak nic neudela a String
     * vrati nezmenenej. Protoze String se nikdy nemeni, ale vzdy se vytvori novej, se zadavany, Stringem se nic
     * nestane.
     *
     * @param ret
     * @param length
     * @return
     */
    public static String alignFromRight(String ret, int length) {
        int alignCount = length - ret.length();
        for (int i = 0; i < alignCount; i++) {
            ret = ret + " ";
        }
        return ret;
    }

    /**
     * Checks if string begins with number.
     * Expecting non-static method
     *
     * @param s
     * @return
     * @throws java.lang.IncompatibleClassChangeError:
     */
    public static boolean beginsWithNumber(String s) {
        if (s.length() == 0) {
            return false;
        }

        if (Character.isDigit(s.charAt(0))) {
            return true;
        } else {
            return false;
        }
    }

    public static String threadName() {
        return Thread.currentThread().getName();
    }

    /**
     * Aligns from left with spaces up to specified length
     *
     * @param ret
     * @param length
     * @return
     */
    public static String alignFromLeft(String ret, int length) {
        //if (ret.length() >= dylka) return ret;
        int alignCount = length - ret.length();
        String s = "";
        for (int i = 0; i < alignCount; i++) {
            s += " ";
        }
        return s + ret;
    }

    /**
     * Converts byte to unsigned integer;
     *
     * @param b
     * @return
     */
    public static int byteToInt(byte b) {
        int a = b & 0xff;
        return a;
    }

    /**
     * Splits line into words (space is used as delimiter)
     *
     * @autor Stanislav Řehák
     */
    public static List<String> splitLine(String line) {
        line = line.trim();
        String[] whiteChars = {" ", "\t"};
        for (int i = 0; i < whiteChars.length; i++) {
            while (line.contains(whiteChars[i] + whiteChars[i])) {
                line = line.replace(whiteChars[i] + whiteChars[i], whiteChars[i]);
            }
        }
        String[] pole = line.split(" ");
        return Arrays.asList(pole);
    }

    /**
     * Returns true if packet is ICMP REQUEST.
     *
     * @param packet
     * @return
     */
    public static boolean isPacketIcmpRequest(IpPacket packet) {
        if (packet.data != null && packet.data.getType() == L4Packet.L4PacketType.ICMP) {
            IcmpPacket p = (IcmpPacket) packet.data;
            if (p.type == IcmpPacket.Type.REQUEST) {
                return true;
            }

        }
        return false;
    }
}
