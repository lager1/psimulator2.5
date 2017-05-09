package psimulator.userInterface;

//import java.util.*;

import java.io.*;

import javax.swing.JTextArea;

/**
 * Implements console-based log file tailing, or more specifically, tail following:
 * it is somewhat equivalent to the unix command "tail -f"
 */
public class Tail implements LogFileTailerListener {
    /**
     * The log file tailer
     */
    private LogFileTailer tailer;

    /**
     * Creates a new Tail instance to follow the specified file
     */
    public Tail(String filename, JTextArea textArea) {
        tailer = new LogFileTailer(new File(filename), 500, false, textArea);
        tailer.addLogFileTailerListener(this);
        tailer.start();
    }

    /**
     * A new line has been added to the tailed log file
     *
     * @param line The new line that has been added to the tailed log file
     */

    public String newLogFileLine(String line) {
        return line;
    }

    public LogFileTailer getFileTailer() {
        return tailer;
    }
}