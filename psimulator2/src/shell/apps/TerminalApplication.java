/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shell.apps;

import device.AbstractDevice;
import telnetd.io.BasicTerminalIO;

/**
 * class that should be inherited when programming a terminal application like command shell or text editor
 * @author Martin Lukáš
 */
public abstract class TerminalApplication {

    protected BasicTerminalIO terminalIO;
    protected AbstractDevice device;

    public TerminalApplication(BasicTerminalIO terminalIO, AbstractDevice device) {
        this.terminalIO = terminalIO;
        this.device=device;
    }
    
    /**
     *  execute application
     * @param terminalIO
     * @param pocitac
     * @return return exit value of program retValue == 0 ==> OK , retValue <-1  ==> fail
     */
    protected abstract int run();

}
