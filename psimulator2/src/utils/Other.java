/*
 * Erstellt am 6.3.2012.
 */
package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Tomas Pitrinec
 */
public class Other {

	/**
	 * Klasicky printStackTrace hazi do stringu. Prejato z http://www.rgagnon.com/javadetails/java-0029.html.
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
	 * Rozlozi cislo na mocniny dvojky. Pouzivam pri vypisovani navratovyho kodu.
	 *
	 * @param c
	 * @return
	 */
	public static String rozlozNaMocniny2(int c) {
		String vratit = "";
		for (int i = 0; i < 31; i++) {
			if ((c & (1 << i)) != 0) {
				if (vratit.equals("")) {
					vratit += (1 << i);
				} else {
					vratit += " + " + (1 << i);
				}
			}
		}
		if (vratit.equals("")) {
			vratit = "0";
		}
		return vratit;
	}

	public static String rozlozNaLogaritmy2(int c) {
		String vratit = "";
		for (int i = 0; i < 31; i++) {
			if ((c & (1 << i)) != 0) {
				if (vratit.equals("")) {
					vratit += (log2(1 << i));
				} else {
					vratit += ", " + (log2(1 << i));
				}
			}
		}
		if (vratit.equals("")) {
			vratit = "Zadny chybovy kod nebyl zadan.";
		}
		return vratit;
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
	public static double zaokrouhli(double d) {
		return ((double) Math.round(d * 1000)) / 1000;
	}

	public static boolean jeInteger(String ret) {
        try {
            int a = Integer.parseInt(ret);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
