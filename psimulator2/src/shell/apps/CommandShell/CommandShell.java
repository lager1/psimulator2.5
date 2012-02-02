/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import commands.AbstractCommandParser;
import device.AbstractDevice;
import exceptions.TelnetConnectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import shell.apps.TerminalApplication;
import telnetd.io.BasicTerminalIO;
import utils.TestLogger;

/**
 *
 * @author Martin Lukáš
 */
public class CommandShell extends TerminalApplication {

    private ShellRenderer shellRenderer;
    private History history = new History();
    public boolean vypisPrompt = true; // v ciscu obcas potrebuju zakazat si vypisovani promptu
    public String prompt = "default promt:~# ";
    private boolean ukoncit = false;
    private AbstractCommandParser parser;
    private Object zamek;

    public CommandShell(BasicTerminalIO terminalIO, AbstractDevice device) {
        super(terminalIO, device);
        this.shellRenderer = new ShellRenderer(terminalIO, this);
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

//    public List<String> getCommandList() {
//        return this.pocitac.getCommandList();
//    }
    /**
     * method that read  till \r\n occured
     * @return whole line without \r\n
     */
    public String readLine() throws TelnetConnectionException {
        return shellRenderer.handleInput();
    }

    public String readCharacter() {
        try {
            return String.valueOf((char) this.terminalIO.read());
        } catch (IOException ex) {
            System.err.println("IOException, cannot read a single character from terminal");
        }

        return "";
    }

    /**
     * method used to printLine to the terminal, this method call print(text+"\r\n") nothing more
     * @param text text to be printed to the terminal
     */
    public void printLine(String text) throws TelnetConnectionException {
        this.print((text + "\r\n"));
    }

    /**
     * method used to print text to the terminal
     * @param text text to be printed to the terminal
     * @throws TelnetConnectionException
     */
    public void print(String text) throws TelnetConnectionException {
        try {
            terminalIO.write(text);
            terminalIO.flush();

            TestLogger.logMessage(text, TestLogger.TYPE.DEBUG, TestLogger.SOURCE.TELNET);
        } catch (IOException ex) {
            throw new TelnetConnectionException("Method CommandShell.print failed");
        }
    }

    /**
     * method that print lines with delay
     * @param lines
     * @param delay in milliseconds
     *
     */
    public void printWithDelay(String lines, int delay) throws TelnetConnectionException {
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
    public void printPrompt() throws TelnetConnectionException {
        if (vypisPrompt) {
            print(prompt);
        }
    }

    /**
     * close session, terminal connection will be closed
     */
    public void closeSession() {
        TestLogger.logMessage("Close session called", TestLogger.TYPE.DEBUG, TestLogger.SOURCE.TELNET);
        ukoncit = true;
    }

    public void setParser(AbstractCommandParser parser) {
        this.parser = parser;
    }

    @Override
    public final int run() {

        try {
            terminalIO.setAutoflushing(true);
            terminalIO.eraseScreen();
            terminalIO.homeCursor();
        } catch (IOException ex) {
            Logger.getLogger(CommandShell.class.getName()).log(Level.SEVERE, null, ex);
        }


        String radek;


        while (!ukoncit) {
            try {
                printPrompt();

                radek = readLine();
                this.history.add(radek);

                TestLogger.logMessage("PRECETL JSEM :" + radek, TestLogger.TYPE.DEBUG, TestLogger.SOURCE.TELNET);

                synchronized (zamek) {
//                parser.zpracujRadek(radek);
                }

                terminalIO.flush();
            } catch (Exception ex) {
                return -1;
            }
        }

        return 0;
    }
}
