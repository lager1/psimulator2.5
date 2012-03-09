/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import exceptions.TelnetConnectionException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import logging.Logger;

import logging.LoggingCategory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;
import telnetd.io.toolkit.ActiveComponent;
import telnetd.io.toolkit.InputFilter;

/**
 *
 * @author Martin Lukáš
 */
public class ShellRenderer extends ActiveComponent {

	private static Pattern printablePatter = Pattern.compile(ShellRenderer.getPrintableRegExp());
	private InputFilter inputFilter;
	private CommandShell commandShell;
	private BasicTerminalIO termIO;
	private int cursor = 0;
	private StringBuilder sb = new StringBuilder(50); //buffer načítaného řádku, čtecí buffer

	public ShellRenderer(CommandShell commandShell, BasicTerminalIO termIO, String name) {
		super(termIO, name);
		this.commandShell = commandShell;
		this.termIO = termIO;
	}

	/**
	 * hlavní funkce zobrazování shellu a čtení z terminálu, reakce na různé klávesy ENETER, BACKSCAPE, LEFT ....
	 *
	 * @return vrací přečtenou hodnotu z řádku, příkaz
	 * @throws TelnetConnectionException
	 */
	public void run() throws Exception {

		this.sb.setLength(0); // clear string builder
		boolean konecCteni = false; // příznak pro ukončení čtecí smyčky jednoho příkazu
		List<String> nalezenePrikazy = new LinkedList<String>(); // seznam nalezenych příkazů po zmáčknutí tabu
		this.cursor = 0;



		while (!konecCteni) {

			try {

				int inputValue = termIO.read();

				if (isPrintable(inputValue)) {  // is a regular character like abc...
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, " Tisknul jsem znak: " + String.valueOf((char) inputValue) + " ,který má kód: " + inputValue);
					termIO.write(inputValue);
					sb.insert(cursor, (char) inputValue);
					cursor++;
					draw();
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor);
					continue; // continue while
				}



				if (inputValue != TerminalIO.TABULATOR) {
					nalezenePrikazy = new LinkedList<String>(); // vyčistím pro další hledání
				}

				switch (inputValue) { // HANDLE CONTROL CODE

					case TerminalIO.TABULATOR:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno TABULATOR");
						this.handleTabulator(nalezenePrikazy);
						break;

					case TerminalIO.DEL:
					case TerminalIO.DELETE:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DEL/DELETE");
						termIO.eraseLine();
						termIO.moveLeft(termIO.getColumns());  // kdyby byla lepsi cesta jak smazat řádku, nenašel jsem
						this.cursor = 0;
						this.sb.setLength(0);
						this.commandShell.printPrompt();
						break;

					case TerminalIO.LEFT:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno LEFT");
						moveCursorLeft(1);
						break;
					case TerminalIO.RIGHT:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno RIGHT");
						moveCursorRight(1);
						break;
					case TerminalIO.UP:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno UP");
						this.handleHistory(TerminalIO.UP);
						break;
					case TerminalIO.DOWN:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DOWN");
						this.handleHistory(TerminalIO.DOWN);
						break;

					case TerminalIO.BACKSPACE:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno BACKSPACE");
						if (cursor != 0) {
							sb.deleteCharAt(cursor - 1);
							moveCursorLeft(1);
							draw();
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Backspace upravil pozici kurzoru na: " + cursor);
						}
						break;
					case TerminalIO.CTRL_W:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+W");
						while (cursor != 0) {
							sb.deleteCharAt(cursor - 1);
							moveCursorLeft(1);
							draw();
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "CTRL+W upravil pozici kurzoru na: " + cursor);

							if (cursor != 0 && Character.isSpaceChar(sb.charAt(cursor - 1))) // delete until space is found
							{
								break; // break while
							}

						}
						break; // break switch
					case TerminalIO.CTRL_C:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+C");
						konecCteni=true;
						termIO.write(BasicTerminalIO.CRLF);
						this.commandShell.getParser().catchSignal(AbstractCommandParser.Signal.INT);  // SEND CTRL_C SIGNAL 
						break;
					case TerminalIO.CTRL_Z:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+Z");
						this.commandShell.getParser().catchSignal(AbstractCommandParser.Signal.ENDZ);  // SEND CTRL_Z SIGNAL
						break;

					case TerminalIO.CTRL_L:	// clean screen
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+L");
						this.clearScreen();
						break;

					case TerminalIO.ENTER:
						konecCteni = true;
						termIO.write(BasicTerminalIO.CRLF);
						break;

					case -1:
					case -2:
						Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Shell renderer read Input(Code):" + inputValue);
						konecCteni = true;
						break;
				}

				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor + "Interpretován řídící kod: " + inputValue);


			} catch (IOException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, ex.toString());
			} catch (UnsupportedOperationException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Unsuported exception catched in ShellRenderer: " + ex.toString());
			}

		}



	}

	public String getValue() {
		return this.sb.toString();
	}

	private static boolean isPrintable(int znakInt) {

		String s = String.valueOf((char) znakInt);

		Matcher matcher = ShellRenderer.printablePatter.matcher(s);

		return matcher.find();
	}

	private static String getPrintableRegExp() {
		return "\\p{Print}";
	}

	/**
	 * funkce která překreslí řádek od pozice kurzoru až do jeho konce dle čtecího bufferu
	 *
	 * @throws IOException
	 */
	@Override
	public void draw() throws IOException {

		termIO.eraseToEndOfScreen();
		termIO.write(sb.substring(cursor, sb.length()));
		termIO.moveLeft(sb.length() - cursor);
	}

	/**
	 * překreslí celou řádku, umístí kurzor na konec řádky
	 *
	 * @throws IOException
	 */
	private void drawLine() throws IOException {

		moveCursorLeft(cursor);
		termIO.eraseToEndOfScreen();
		this.cursor = 0;
		termIO.write(sb.toString());
		this.cursor = sb.length();

	}

	/**
	 * funkce obsluhující historii, respektive funkce volaná při přečtení kláves UP a DOWN
	 *
	 * @param key typ klávesy který byl přečten
	 * @throws IOException
	 */
	private void handleHistory(int key) throws IOException, TelnetConnectionException {
		if (!(key == TerminalIO.UP || key == TerminalIO.DOWN)) // historie se ovládá pomocí šipek nahoru a dolů, ostatní klávesy ignoruji
		{
			return;
		}

		termIO.eraseLine();
		termIO.moveLeft(100);  // kdyby byla lepsi cesta jak smazat řádku, nenašel jsem

		this.commandShell.printPrompt();

		if (key == TerminalIO.UP) {
			//  this.sb.setLength(0);
			this.commandShell.getHistory().handlePrevious(this.sb);
		} else if (key == TerminalIO.DOWN) {
			//  this.sb.setLength(0);
			this.commandShell.getHistory().handleNext(this.sb);
		}

		termIO.write(this.sb.toString());
		termIO.moveLeft(100);
		termIO.moveRight(sb.length() + this.commandShell.prompt.length());
		this.cursor = sb.length();

	}

	/**
	 * funkce obstarávající posun kurzoru vlevo. Posouvá "blikající" kurzor, ale i "neviditelný" kurzor značící pracovní
	 * místo v čtecím bufferu
	 */
	private void moveCursorLeft(int times) {

		for (int i = 0; i < times; i++) {
			if (cursor == 0) {
				return;
			} else {
				try {
					termIO.moveLeft(1);
					cursor--;
				} catch (IOException ex) {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, ex.toString());

				}


			}
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "VLEVO, pozice: " + cursor);

		}

	}

	/**
	 * funkce obstarávající posun kurzoru vpravo. Posouvá "blikající" kurzor, ale i "neviditelný" kurzor značící
	 * pracovní místo v čtecím bufferu
	 */
	private void moveCursorRight(int times) {

		for (int i = 0; i < times; i++) {


			if (cursor >= this.sb.length()) {
				return;
			} else {
				try {
					termIO.moveRight(1);
					cursor++;
				} catch (IOException ex) {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, "VPRAVO, pozice: " + cursor);

				}


			}
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "VPRAVO, pozice: " + cursor);


		}
	}

	/**
	 *
	 * @param nalezenePrikazy seznam nalezených příkazů z předchozího hledání, pokud prázdný, tak jde o první stisk
	 * tabulatoru
	 */
	private void handleTabulator(List<String> nalezenePrikazy) throws IOException, TelnetConnectionException {

		if (!nalezenePrikazy.isEmpty() && nalezenePrikazy.size() > 1) { // dvakrat zmacknuty tab a mám více než jeden výsledek

			termIO.write(TerminalIO.CRLF); // nový řádek

			for (String nalezeny : nalezenePrikazy) {
				termIO.write(nalezeny + "  ");
			}

			termIO.write(TerminalIO.CRLF); // nový řádek
			this.commandShell.printPrompt();
			termIO.write(this.sb.toString());


			return;
		}


// nové hledání

		String hledanyPrikaz = this.sb.substring(0, cursor);
//        List<String> prikazy = this.commandShell.getCommandList();
//
//
//        for (String temp : prikazy) {
//            if (temp.startsWith(hledanyPrikaz)) {
//                nalezenePrikazy.add(temp);
//            }
//
//        }
//
//        if (nalezenePrikazy.isEmpty()) // nic jsem nenašel, nic nedělám :)
//        {
//            return;
//        }
//
//
//        if (nalezenePrikazy.size() == 1) // našel jsem jeden odpovídající příkaz tak ho doplním
//        {
//
//            String nalezenyPrikaz = nalezenePrikazy.get(0);
//            String doplnenyPrikaz = nalezenyPrikaz.substring(hledanyPrikaz.length(), nalezenyPrikaz.length());
//
//            sb.insert(cursor, doplnenyPrikaz);
//
//            int tempCursor = cursor;
//            drawLine();
//
//            moveCursorLeft(sb.length() - (tempCursor + doplnenyPrikaz.length()));
//
//        }
//

	}

	private void clearScreen() throws IOException, TelnetConnectionException {
		this.termIO.eraseScreen();
		termIO.setCursor(0, 0);
		this.commandShell.printPrompt();
		this.cursor = 0;
		drawLine();

	}
}
