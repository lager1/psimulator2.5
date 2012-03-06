/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import device.Device;
import exceptions.TelnetConnectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import logging.Logger;
import logging.LoggingCategory;


import shell.apps.TerminalApplication;
import telnetd.io.BasicTerminalIO;

/**
 *
 * @author Martin Lukáš
 */
public class CommandShell extends TerminalApplication {

	public static final int DEFAULT_MODE = 0;
	public static final int CISCO_USER_MODE = 0; // alias na ten defaultni
	public static final int CISCO_PRIVILEGED_MODE = 1;
	public static final int CISCO_CONFIG_MODE = 2;
	public static final int CISCO_CONFIG_IF_MODE = 3;
	private ShellRenderer shellRenderer;
	private History history = new History();
	public String prompt = "default promt:~# ";
	private boolean quit = false;
	private AbstractCommandParser parser;
	private Object locker = new Object();
	/**
	 * Stav shellu, na linuxuje to furt defaultni 0, na ciscu se to meni podle toho (enable, configure terminal atd.).
	 * Dle stavu se bude resit napovidani a historie.
	 */
	private int mode = DEFAULT_MODE;

	public CommandShell(BasicTerminalIO terminalIO, Device device) {
		super(terminalIO, device);
		this.shellRenderer = new ShellRenderer(terminalIO, this);
		this.parser = device.createParser(this);
	}

	public History getHistory() {
		return history;
	}

	public void setHistory(History history) {
		this.history = history;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

//    public List<String> getCommandList() {
//        return this.pocitac.getCommandList();
//    }
	/**
	 * method that read till \r\n occured
	 *
	 * @return whole line without \r\n
	 */
	public String readLine() {

		String ret = null;
		try {
			ret = shellRenderer.handleInput();
		} catch (TelnetConnectionException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost");
		}

		return ret;
	}

	public String readCharacter() {
		try {
			return String.valueOf((char) this.terminalIO.read());
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "IOException, cannot read a single character from terminal");
		}

		return "";
	}

	/**
	 * method used to printLine to the terminal, this method call print(text+"\r\n") nothing more
	 *
	 * @param text text to be printed to the terminal
	 */
	public void printLine(String text) {

		this.print((text + "\r\n"));



	}

	/**
	 * method used to print text to the terminal
	 *
	 * @param text text to be printed to the terminal
	 *
	 */
	public void print(String text) {
		try {
			terminalIO.write(text);
			terminalIO.flush();
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, text);
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost.");
		}


	}

	/**
	 * method that print lines with delay
	 *
	 * @param lines
	 * @param delay in milliseconds
	 *
	 */
	public void printWithDelay(String lines, int delay) {
		try {
			BufferedReader input = new BufferedReader(new StringReader(lines));
			String singleLine = "";
			while ((singleLine = input.readLine()) != null) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException ex) {
					System.err.println("Thread interruped exception occured in printWithDelay method");
				}

				printLine(singleLine);
			}
		} catch (IOException ex) {
			System.err.println("IO exception occured in printWithDelay method");
		}

	}

	/**
	 * just print prompt
	 */
	public void printPrompt() {
		if (parser.writePrompt()) {
			print(prompt);
		}
	}

	/**
	 * close session, terminal connection will be closed
	 */
	public void closeSession() {
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Close session called");
		quit = true;
	}

	public void setParser(AbstractCommandParser parser) {
		this.parser = parser;
	}

	@Override
	public final int run() {

		try {
			terminalIO.setLinewrapping(true);
			terminalIO.setAutoflushing(true);
			terminalIO.eraseScreen();
			terminalIO.homeCursor();
		} catch (IOException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
		}


		String line;


		while (!quit) {
			try {
				printPrompt();

				line = readLine();
				this.history.add(line);

				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "PRECETL JSEM :" + line);

				synchronized (locker) {
						parser.processLine(line, mode);
					}

				terminalIO.flush();
			} catch (Exception ex) {

				if (quit) // if there is a quit request, then it is ok
				{
					return 0;
				} else {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Exception occured, when reading a line from telnet, closing program: " + "CommandShell");
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
					return -1;
				}

			}
		}

		return 0;
	}

	@Override
	public int quit() {
		this.quit = true;
		return 0;
	}
}
