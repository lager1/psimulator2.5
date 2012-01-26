/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import java.util.Arrays;

/**
 * Implementation of mac address. 
 * Vnitrni representace je pomoci integeru, protoze s bytama byl problem se znainkem.
 * @author neiss
 */
public class MacAddress {

    private int[] representation;
    
    /**
     * Vyjimka pro tvoreni mac adresy.
     */
    public static class BadMacException extends RuntimeException {
        public BadMacException(String msg) {
            super(msg);
        }
    }
    
// Konstruktory -----------------------------------------------------------------------------------------------

    /**
     * Klasickej konstuktor, ocekava mac adresu oddelenou dvojteckama.
     * @param address 
     */
    public MacAddress(String address) {
        this.representation = stringToBytes(address, ':');
    }
    
    /**
     * Predpoklada mac adresu oddelenou zadanym oddelovacem (napr. pomlckou apod.)
     * @param address
     * @param delimiter 
     */
    public MacAddress(String address, char delimiter) {
        this.representation = stringToBytes(address, delimiter);
    }

// vypisy a porovnavani ---------------------------------------------------------------------------
    
    /**
     * Vrati klasickej vypis s oddelenim dvojteckama.
     * @return 
     */
    @Override
    public String toString() {
        String vratit = "";
        for (int i = 0; i < 6; i++) {
            vratit += byteToString(representation[i]);
            vratit += ":";
        }
        return vratit.substring(0, vratit.length() - 1);//aby se odmazala ta posledni dvojtecka
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MacAddress other = (MacAddress) obj;
        if (!Arrays.equals(this.representation, other.representation)) {
            return false;
        }
        return true;
    }
    
//    public boolean isLessThan(MacAddress other){
//        
//    }
//    
//    private boolean isByteLessThan

// staticky metody ----------------------------------------------------------------------------------------
    
    /**
     * Returns true, if given mac address is broadcast
     * @param mac
     * @return 
     */
    public static boolean isBroadcast(MacAddress mac){
        for(int i=0;i<6;i++){
            if(mac.representation[i]!=255) return false;
        } 
        return true;
    }
    
// privatni staticky metody -------------------------------------------------------------------------------

    private static int[] stringToBytes(String adr, char delimiter) {
        int [] vratit = new int[6];
        String[] pole = adr.split("\\" + delimiter);
        if (pole.length != 6)
            throw new BadMacException("Neni to sest bytu!");
        for (int i = 0; i < 6; i++) {
            vratit [i]= stringToByte(pole[i]);
        }
        return vratit;
    }

    private static int stringToByte(String s) {
        if (s.length() != 2) {
            throw new BadMacException("Nejakej byte nema spravnou dylku.");
        }
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException ex) {
            throw new BadMacException("Mac se nepodarilo zparsovat.");
        }
    }

    private static String byteToString(int bajt) {
        String v = Integer.toHexString(bajt);
        if(v.length()<2) return "0"+v;
        else return v;
    }
}
