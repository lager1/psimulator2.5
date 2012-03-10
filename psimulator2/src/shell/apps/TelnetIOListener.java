package shell.apps;

import commands.AbstractCommandParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.LoggingCategory;
import shell.ShellUtils;
import shell.SignalCatchAble;
import shell.SignalCatchAble.Signal;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class TelnetIOListener extends Thread {

	final BasicTerminalIO terminalIO;
	List<SignalCatchAble> listeners;
	Integer lastInput;
	private boolean shutdown = false;

	public TelnetIOListener(BasicTerminalIO terminalIO) {
		this.terminalIO = terminalIO;
		this.listeners = new ArrayList<>(3);
	}

	public void shutdown() {
		this.shutdown = true;
		try {
			this.join();
		} catch (InterruptedException ex) {
			Logger.getLogger(TelnetIOListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void run() {

		shutdown = false;

		while (!shutdown) {
			try {

				lastInput = terminalIO.read(); // blocking read

				if (ShellUtils.isPrintable(lastInput)) {
					terminalIO.write((char) lastInput.byteValue());
				} else {
					handleControlCodes(lastInput);
				}

			} catch (IOException ex) {
				Logger.getLogger(TelnetIOListener.class.getName()).log(Level.SEVERE, null, ex);
			}

		}

	}

	public void registerListener(SignalCatchAble catchable) {
		this.listeners.add(catchable);
	}

	public void removeListener(SignalCatchAble catchable) {
		this.listeners.remove(catchable);
	}

	private void handleControlCodes(int input) {

		Signal signal = null;

		switch (input) {
			case TerminalIO.CTRL_C:
				logging.Logger.log(logging.Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+C");
				signal = Signal.CTRL_C;
				break;
			case TerminalIO.CTRL_Z:
				logging.Logger.log(logging.Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+Z");
				signal = Signal.CTRL_Z;
				break;
			default:
				return;
		}

		for (SignalCatchAble signalCatchAble : listeners) {
			if (signalCatchAble != null) {
				signalCatchAble.catchSignal(signal);
			}
		}

	}

}
