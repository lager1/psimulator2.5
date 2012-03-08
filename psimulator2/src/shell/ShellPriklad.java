/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell;

/**
 * Toto je jen priklad shellu, jak si predstavujem to rozhrani. Nejak to, Martine, podle toho predelej, pak tohle
 * smazem. nebo si z toho udelej interface..
 * TODO: ShellPriklad prijde smazat.
 *
 * @author neiss
 */
public class ShellPriklad {

	public static int DEFAULT_MODE = 0;
	public static int CISCO_USER_MODE = 0; // alias na ten defaultni
	public static int CISCO_PRIVILEGED_MODE = 1;
	public static int CISCO_CONFIG_MODE = 2;
	public static int CISCO_CONFIG_IF_MODE = 3;
	/**
	 * Jakmile se zavola nejakej prikaz, tak by se melo nastavit na true. Az bude false (nastavi prikaz), tak se zase
	 * zacne vypisovat prompt.
	 *
	 * (( commandIsRunning == true ) tak se prompt nevypisuje a chytaji se signaly a uzivatelsky vstup po Enter, nevola
	 * se processLine!
	 */
	private boolean commandIsRunning = false;
	/**
	 * Prompt, ktery se bude pridavat po zkonceni prikazu.
	 */
	private String prompt = "";
	/**
	 * Stav shellu, na linuxuje to furt defaultni 0, na ciscu se to meni podle toho (enable, configure terminal atd.).
	 * Dle stavu se bude resit napovidani a historie.
	 */
	private int mode = DEFAULT_MODE;

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	/**
	 * Prikaz oznamuje ukonceni prace, aby shell zase zacal vypisovat prompt.
	 */
	public void commandEnded() {
		this.commandIsRunning = false;
	}

	/**
	 * Vypise radek.
	 *
	 * @param line
	 */
	public void writeLine(String line) {
	}

	/**
	 * Vypise zadanej text, neodradkuje.
	 *
	 * @param s
	 */
	public void write(String s) {
	}

	/**
	 * Nastavi promt, na ciscu je potreba ho menit.
	 *
	 * @param promt
	 */
	public void setPromt(String promt) {
	}

	/**
	 * Ve starym simulatoru se to jmenovalo vypisPoRadcich. Vypisuje to po radcich se zadanym zpozdenim. Implementaci
	 * muzes okopirovat ze staryho
	 *
	 * @param lines
	 * @param delayTime
	 */
	public void writeDelayedLines(String lines, int delayTime) {
	}
}
