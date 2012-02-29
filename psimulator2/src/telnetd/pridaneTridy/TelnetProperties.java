/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package telnetd.pridaneTridy;

import config.Network.HwComponentModel;
import java.util.Collection;
import java.util.Properties;

/**
 * x
 *
 * @author Martin Lukáš
 */
public class TelnetProperties {

	

    public enum Shell {

        LINUX, CISCO
    }
    private Properties properties = new Properties();

    public TelnetProperties(Collection<HwComponentModel> devices, int startPort) {
        commonSetup();

        StringBuilder allListeners = new StringBuilder();

        for (HwComponentModel networkDevice : devices) {
            addListener(networkDevice.getIDAsString(), startPort++ );
            allListeners.append(",").append(networkDevice.getIDAsString());
        }

        properties.setProperty("listeners", allListeners.toString());

    }

    private void commonSetup() {

        properties.setProperty("terminals", "vt100,ansi,windoof,xterm");
        properties.setProperty("term.vt100.class", "telnetd.io.terminal.vt100");
        properties.setProperty("term.vt100.aliases", "default,vt100-am,vt102,dec-vt100");
        properties.setProperty("term.ansi.class", "telnetd.io.terminal.ansi");
        properties.setProperty("term.ansi.aliases", "color-xterm,xterm-color,vt320,vt220,linux,screen");
        properties.setProperty("term.windoof.class", "telnetd.io.terminal.Windoof");
        properties.setProperty("term.windoof.aliases", "");
        properties.setProperty("term.xterm.class", "telnetd.io.terminal.xterm");
        properties.setProperty("term.xterm.aliases", "");
        properties.setProperty("shells", "std");
        //  properties.setProperty("shell.std.class", "telnetd.shell.DummyShell");
        properties.setProperty("shell.std.class", "shell.TelnetSession");

    }

    public Properties getProperties() {

        return properties;

    }

    private void addListener(String name, int port) {

        properties.setProperty(name + ".loginshell", "std");
        properties.setProperty(name + ".port", String.valueOf(port));
        properties.setProperty(name + ".floodprotection", "5");
        properties.setProperty(name + ".maxcon", "25");
        properties.setProperty(name + ".time_to_warning", "3600000");
        properties.setProperty(name + ".time_to_timedout", "60000");
        properties.setProperty(name + ".housekeepinginterval", "1000");
        properties.setProperty(name + ".inputmode", "character");
        properties.setProperty(name + ".connectionfilter", "none");


    }
}

/*
 * ==============DEFAULTNÍ KONFIGURACE Z NÁVODU NA WEBU PROJEKTU================
 * #Unified telnet proxy properties #Daemon configuration example. #Created:
 * 15/11/2004 wimpi
 *
 *
 * ############################ # Telnet daemon properties #
 * ############################
 *
 * ##################### # Terminals Section # #####################
 *
 * # List of terminals available and defined below
 * terminals=vt100,ansi,windoof,xterm
 *
 * # vt100 implementation and aliases
 * term.vt100.class=net.wimpi.telnetd.io.terminal.vt100
 * term.vt100.aliases=default,vt100-am,vt102,dec-vt100
 *
 * # ansi implementation and aliases
 * term.ansi.class=net.wimpi.telnetd.io.terminal.ansi
 * term.ansi.aliases=color-xterm,xterm-color,vt320,vt220,linux,screen
 *
 * # windoof implementation and aliases
 * term.windoof.class=net.wimpi.telnetd.io.terminal.Windoof
 * term.windoof.aliases=
 *
 * # xterm implementation and aliases
 * term.xterm.class=net.wimpi.telnetd.io.terminal.xterm term.xterm.aliases=
 *
 * ################## # Shells Section # ##################
 *
 * # List of shells available and defined below shells=dummy
 *
 * # shell implementations shell.dummy.class=net.wimpi.telnetd.shell.DummyShell
 *
 * ##################### # Listeners Section # #####################
 * listeners=std
 *
 *
 * # std listener specific properties
 *
 * #Basic listener and connection management settings std.port=6666
 * std.floodprotection=5 std.maxcon=25
 *
 *
 * # Timeout Settings for connections (ms) std.time_to_warning=3600000
 * std.time_to_timedout=60000
 *
 * # Housekeeping thread active every 1 secs std.housekeepinginterval=1000
 *
 * std.inputmode=character
 *
 * # Login shell std.loginshell=dummy
 *
 * # Connection filter class std.connectionfilter=none
 */
