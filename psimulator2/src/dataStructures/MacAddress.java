/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import java.util.Arrays;

/**
 * Implementation of mac address. Vnitrni representace je pomoci integeru, protoze s bytama byl problem se znainkem.
 *
 * @author neiss
 */
public class MacAddress {

	private final int[] representation;

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
	 * Klasickej konstuktor, ocekava mac adresu oddelenou cimkoliv, ale po dvou znakach. Deli se to podle tretiho znaku.
	 *
	 * @param address
	 */
	public MacAddress(String address) {
		this.representation = stringToBytes(address, address.charAt(2));
	}

	/**
	 * Predpoklada mac adresu oddelenou zadanym oddelovacem (napr. pomlckou apod.)
	 *
	 * @param address
	 * @param delimiter
	 */
	public MacAddress(String address, char delimiter) {
		this.representation = stringToBytes(address, delimiter);
	}

	/**
	 * Konstruktor jen privatni pro staticky metody, napr. pro getRandomMac().
	 *
	 * @param representation
	 */
	private MacAddress(int[] representation) {
		this.representation = representation;
	}

// vypisy a porovnavani ---------------------------------------------------------------------------
	/**
	 * Vrati klasickej vypis s oddelenim dvojteckama.
	 *
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

	/**
	 * Porovnavani.
	 *
	 * @param other
	 * @return
	 */
	public boolean isLessOrEqualThan(MacAddress other) {
		return isByteLessThan(other, 0);
	}

	private boolean isByteLessThan(MacAddress other, int cisloBytu) {
		if (representation[cisloBytu] < other.representation[cisloBytu]) {
			return true;
		} else if (representation[cisloBytu] > other.representation[cisloBytu]) {
			return false;
		} else if (cisloBytu < 5) {
			return isByteLessThan(other, cisloBytu + 1);
		} else {
			return true;
		}
	}

// staticky metody ----------------------------------------------------------------------------------------
	/**
	 * Returns true, if given mac address is broadcast
	 *
	 * @param mac
	 * @return
	 */
	public static boolean isBroadcast(MacAddress mac) {
		for (int i = 0; i < 6; i++) {
			if (mac.representation[i] != 255) {
				return false;
			}
		}
		return true;
	}

	public static MacAddress broadcast() {
		return new MacAddress("ff:ff:ff:ff:ff:ff");
	}

	public static MacAddress getRandomMac() {
		int[] representation = new int[6];
		for (int i = 0; i < 6; i++) {
			representation[i] = (int) (Math.random() * 256);
		}

		MacAddress vratit = new MacAddress(representation);
		if (vratit.equals(broadcast())) {	// kdyby se nahodou vygenerovala broadcastova, tak se musi generovat znova
			return getRandomMac();
		} else {
			return vratit;
		}
	}

// privatni staticky metody -------------------------------------------------------------------------------
	private static int[] stringToBytes(String adr, char delimiter) {
		int[] vratit = new int[6];
		String[] pole = adr.split("\\" + delimiter);
		if (pole.length != 6) {
			throw new BadMacException("Neni to sest bytu, nezparsovala se mac adresa " + adr);
		}
		for (int i = 0; i < 6; i++) {
			vratit[i] = stringToByte(pole[i]);
		}
		return vratit;
	}

	/**
	 * Vytvori z jednoho bytu jako stringu bajt jako integer.
	 *
	 * @param s
	 * @return
	 */
	private static int stringToByte(String s) {
		if (s.length() != 2) {
			throw new BadMacException("Tenhleten bajt nema spravnou dylku: " + s);
		}
		try {
			return Integer.parseInt(s, 16);
		} catch (NumberFormatException ex) {
			throw new BadMacException("Tenhleten bajt nema spravnou dylku: " + s);
		}
	}

	private static String byteToString(int bajt) {
		String v = Integer.toHexString(bajt);
		if (v.length() < 2) {
			return "0" + v;
		} else {
			return v;
		}
	}
}
