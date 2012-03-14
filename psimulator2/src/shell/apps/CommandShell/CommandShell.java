/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import device.Device;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;
import logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;


import shell.apps.TerminalApplication;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;
import telnetd.io.toolkit.Editfield;

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
	private NormalRead normalRead;
	public String prompt = "default promt:~# ";
	private boolean quit = false;
	private AbstractCommandParser parser;
	private ShellMode shellMode = ShellMode.COMMAND_READ;
	private Thread thread;  // thread running blocking IO operations
	/**
	 * Stav shellu, na linuxuje to furt defaultni 0, na ciscu se to meni podle toho (enable, configure terminal atd.).
	 * Dle stavu se bude resit napovidani a historie.
	 */
	private int mode = DEFAULT_MODE;

	public CommandShell(BasicTerminalIO terminalIO, Device device) {
		super(terminalIO, device);
		this.thread = Thread.currentThread();
	}

	/**
	 * get active shell mode
	 *
	 * @return
	 */
	public ShellMode getShellMode() {
		return shellMode;
	}

	/**
	 * set active shell mode
	 *
	 * @param shellMode
	 */
	public void setShellMode(ShellMode shellMode) {
		this.shellMode = shellMode;
		this.thread.interrupt();  // wake up possibly IO waiting thread

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
	 * method that read command from command line
	 *
	 * @return whole line without \r\n
	 */
	public String readCommand() {

		String ret = null;
		try {
			this.getShellRenderer().run();
			ret = this.getShellRenderer().getValue();
		} catch (InterruptedException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Blocking IO operation stopped");
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost");
		}

		return ret;
	}

	public ShellRenderer getShellRenderer() {
		if (this.shellRenderer == null) {
			this.shellRenderer = new ShellRenderer(this, terminalIO, "ShellRenderer");
		}
		return shellRenderer;
	}

	/**
	 * method that read a single printable character from telnet input and handle control codes properly
	 *
	 * @return
	 */
	public char readPrintableCharacter() throws IOException {

		while (true) {

			int input = this.terminalIO.read();

			if (ShellUtils.isPrintable(input)) {
				return (char) input;
			}

			ShellUtils.handleSignalControlCodes(this.getParser(), input);
		}
	}

	/**
	 * method that read everything from telnet input like unprintable characters, control codes ... its up to you to
	 * handle that that
	 *
	 * @return
	 * @throws IOException
	 */
	public int rawRead() throws IOException {
		return this.terminalIO.read();
	}

	/**
	 * determine if there is something to read
	 *
	 * @return
	 */
	public boolean available() {
		return this.terminalIO.avaiable();
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

			this.handleUnexpectedInput();  // just for sure, for example if text.lenght==0

			for (int i = 0; i < text.length(); i++) {
				char ch = text.charAt(i);
				terminalIO.write(ch);
				terminalIO.flush();

				this.handleUnexpectedInput();  // handle unexpected input between characters print
			}

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
		if (this.getParser().isCommandRunning()) {
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

	public AbstractCommandParser getParser() {
		if (this.parser == null) {
			this.parser = this.device.createParser(this);
		}
		return parser;
	}

	public void setParser(AbstractCommandParser parser) {
		this.parser = parser;
	}

	private NormalRead getNormalRead() {
		if (this.normalRead == null) {
			this.normalRead = new NormalRead(terminalIO, "NormalRead", this.getParser(), this);
		}
		return this.normalRead;

	}

	@Override
	public final int run() {

		try {
			terminalIO.setLinewrapping(true);
			terminalIO.setAutoflushing(true);
			//terminalIO.eraseScreen();
			//terminalIO.homeCursor();
		} catch (IOException ex) {
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
		}


		String line;

		this.shellMode = ShellMode.COMMAND_READ; // default start reading a command
		//this.shellMode = ShellMode.NORMAL_READ; // testing purposes
		//this.shellMode = ShellMode.INPUT_FIELD; // testing purposes

		while (!quit) {
			try {

				switch (this.shellMode) {

					case COMMAND_READ:
						printPrompt();
						line = readCommand();

						if (line != null) {
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "PRECETL JSEM PRIKAZ:" + line);
							this.getParser().processLine(line, mode);
						}
						break;
					case NORMAL_READ:
						try {
							this.getNormalRead().run();
						} catch (InterruptedException ex) {
							Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Blocking IO operation stopped");
						}
						break;
					case INPUT_FIELD:	// @TODO not handled yet
						break;

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

	public void handleUnexpectedInput() throws IOException {

		while (terminalIO.avaiable()) {  // there is unexpected input to be handled

			int input = terminalIO.read();

			if (ShellUtils.isPrintable(input)) {
				terminalIO.write((char) input);
			} else {
				ShellUtils.handleSignalControlCodes(this.getParser(), input);
			}

		}

	}
}
