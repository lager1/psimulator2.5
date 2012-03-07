/*
 * created 6.3.2012
 */
package commands;

import device.Device;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.NetMod;
import networkModule.TcpIpNetMod;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class AbstractCommand implements Loggable {

	public final AbstractCommandParser parser;
	private String name;

	/**
	 * Konstruktor prikazu. Nedavat zadny slozity veci / na to je metoda
	 *
	 * @param parser
	 */
	public AbstractCommand(AbstractCommandParser parser) {
		this.parser = parser;
	}

	/**
	 * Samotny spusteni prikazu, nebude se to vsechno uz delat v konstruktoru.
	 *
	 * @return
	 */
	public abstract void run();

	/**
	 * Predavani uzivatelskyho vstupu prave bezicimu commandu.
	 *
	 * @param input
	 */
	public abstract void catchUserInput(String input);

	public String nextWord() {
		return parser.nextWord();
	}

	public String nextWordPeek() {
		return parser.nextWordPeek();
	}

	/**
	 * Zkratka: vrati hodnotu ukazatele do seznamu slov.
	 *
	 * @return
	 */
	protected int getRef() {
		return parser.ref;
	}

	/**
	 * Zkratka na vraceni pocitace.
	 *
	 * @return
	 */
	protected Device getDevice() {
		return parser.device;
	}

	/**
	 * Vraci jmeno prikazu
	 *
	 * @return
	 */
	public String getName() {
		if (name == null) {
			Logger.log(this.getClass().getName(), Logger.ERROR, LoggingCategory.GENERIC_COMMANDS, "Prikaz teto tridy nevraci jmeno.");
		}
		return name;
	}

	/**
	 * Zkratka na vraceni TCP/IP sitovyho modulu.
	 *
	 * @return
	 */
	protected TcpIpNetMod getNetMod() {
		NetMod nm = parser.device.getNetworkModule();
		if (nm.isStandardTcpIpNetMod()) {
			return (TcpIpNetMod) nm;
		} else {
			Logger.log(getDescription(), Logger.ERROR, LoggingCategory.GENERIC_COMMANDS, "Prikaz zavolal TcpIpNetmod, kterej ale device nema.");
			return null;
		}
	}

	@Override
	public String getDescription() {
		return getDevice().getName() + ": command " + getName() + ": ";
	}

	/**
	 * Zkratka pro vypisovani do shellu.
	 */
	protected void printLine(String s) {
		parser.getShell().printLine(s);
	}

	/**
	 * Zkratka pro vypisovani do shellu.
	 */
	protected void print(String s) {
		parser.getShell().print(s);
	}

	protected void printWithDelay(String s, int delay) {
		parser.getShell().printWithDelay(s, delay);
	}
}
