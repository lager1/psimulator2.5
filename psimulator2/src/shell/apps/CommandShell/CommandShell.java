/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import device.Device;
import filesystem.ArchiveFileSystem;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;


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
	private NormalRead normalRead;
	private InputField inputField;
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
		this.thread.setName("CommandShell/Parser thread");
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

		try {
			this.getShellRenderer().run();
		} catch (InterruptedException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Blocking IO operation stopped");
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost");
			this.quit();
			return null;
		}

		return this.getShellRenderer().getValue();

	}

	public String readInput() {

		try {
			this.getInputField().run();
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Connection with user lost " + ex.toString());
			this.quit();
			return null;
		}

		return this.getInputField().getValue();

	}

	public InputField getInputField() {
		if (this.inputField == null) {
			this.inputField = new InputField(terminalIO, "InputField", this.getParser(), this);
		}

		return this.inputField;
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
		//	this.shellMode = ShellMode.INPUT_FIELD; // testing purposes

		// load history
		String historyPath = "/home/user/history";
		this.loadHistory(this.getShellRenderer().getHistory(), historyPath);

		try {

			while (!quit) {


				switch (this.shellMode) {

					case COMMAND_READ:
						printPrompt();
						line = readCommand();

						if (line != null) {
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "COMMAND READ:" + line);
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
					case INPUT_FIELD:
						line = readInput();

						if (line != null) {
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "INPUT FIELD READ:" + line);
							this.getParser().catchUserInput(line);
						}

						break;

				}
			}

		} catch (Exception ex) {

			if (quit) // if there is a quit request, then it is ok
			{
				return 0;
			} else {
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Exception occured, when reading a line from telnet, closing program: " + "CommandShell");
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
				return -1;
			}
		} finally {

			// save history
			this.saveHistory(this.getShellRenderer().getHistory(), historyPath);

		}

		return 0;
	}

	@Override
	public int quit() {

		if (this.shellRenderer != null) {
			this.shellRenderer.quit();
		}

		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Quiting CommandShell");
		this.quit = true;
		return 0;
	}

	public void saveHistory(final History history, String path) {


		this.device.getFilesystem().runOutputFileJob(path, new OutputFileJob() {

			@Override
			public int workOnFile(OutputStream output) throws Exception {

				PrintWriter historyWriter = new PrintWriter(output);

				List<String> historyList = history.getActiveHistory();

				for (String command : historyList) {
					historyWriter.println(command);
				}

				historyWriter.flush();
				
				return 0;
			}
		});

	}

	public void loadHistory(final History history, String path) {

		this.device.getFilesystem().runInputFileJob(path, new InputFileJob() {

			@Override
			public int workOnFile(InputStream input) throws Exception {

				Scanner sc = new Scanner(input);
				List<String> historyList = new LinkedList<>();

				while (sc.hasNextLine()) {
					historyList.add(sc.nextLine().trim());
				}

				history.setActiveHistory(historyList);

				return 0;
			}
		});

	}
}
