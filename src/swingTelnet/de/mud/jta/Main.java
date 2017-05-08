/*
 * This file is part of "JTA - Telnet/SSH for the JAVA(tm) platform".
 *
 * (c) Matthias L. Jugel, Marcus Meißner 1996-2005. All Rights Reserved.
 *
 * Please visit http://javatelnet.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */
package de.mud.jta;

import de.mud.jta.event.FocusStatusListener;
import de.mud.jta.event.OnlineStatusListener;
import de.mud.jta.event.ReturnFocusRequest;
import de.mud.jta.event.SocketRequest;
import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.PrintJob;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * <B>JTA - Telnet/SSH for the JAVA(tm) platform</B><P>
 * This is the implementation of whole set of applications. It's modular
 * structure allows to configure the software to act either as a sophisticated
 * terminal emulation and/or, adding the network backend, as telnet
 * implementation. Additional modules provide features like scripting or an
 * improved graphical user interface.<P>
 * This software is written entirely in Java<SUP>tm</SUP>.<P>
 * This is the main program for the command line telnet. It initializes the
 * system and adds all needed components, such as the telnet backend and
 * the terminal front end. In contrast to applet functionality it parses
 * command line arguments used for configuring the software. Additionally
 * this application is not restricted in the sense of Java<SUP>tmp</SUP>
 * security.
 * <P>
 * <B>Maintainer:</B> Matthias L. Jugel
 *
 * @version $Id: Main.java 499 2005-09-29 08:24:54Z leo $
 * @author Matthias L. Jugel, Marcus Meissner
 */
public class Main {

  private final static int debug = 0;

  private final static boolean personalJava = false;

  /** holds the last focussed plugin */
  private static Plugin focussedPlugin;

  /** holds the system clipboard or our own */
  private static Clipboard clipboard;

  private static String host, port;
  
  private static ResourceBundle resBundle;
  
  public static int defaultColumns = 100;
  public static int defaultRows = 50;
  public static String hostName = null;
  
  public static void main(String args[]){
     JFrame frame =  Main.run(args);
     
     frame.setVisible(true);
  }
  
  /**
   * [-lang cz/en] [host port]
   * @param args
   * @return
   */
  public static JFrame run(String args[]) {
      
      // DELEGATE STD AND ERR OUTPUT ?
      OutputSingleton.out = new DelegatePrintStream(System.out, false);
      OutputSingleton.err = new DelegatePrintStream(System.err, false);
      
      if(args == null)
          args = new String[0];
      
    final Properties options = new Properties();
    try {
      options.load(Main.class.getResourceAsStream("/de/mud/jta/default.conf"));
    } catch (IOException e) {
      de.mud.jta.OutputSingleton.err.println("jta: cannot load default.conf");
    }
    String error = parseOptions(options, args);
    if (error != null) {
      de.mud.jta.OutputSingleton.err.println(error);
      de.mud.jta.OutputSingleton.err.println("usage: de.mud.jta.Main [-plugins pluginlist] "
                         + "[-addplugin plugin] "
                         + "[-config url_or_file] "
                         + "[-lang cz/en]"
                         + "[-term id] [host [port]]");
      return null;
    }
    
    options.setProperty("Terminal.size", "["+defaultColumns +","+defaultRows+"]");

    String cfg = options.getProperty("Main.config");
    if (cfg != null)
      try {
        options.load(new URL(cfg).openStream());
      } catch (IOException e) {
        try {
          options.load(new FileInputStream(cfg));
        } catch (Exception fe) {
          de.mud.jta.OutputSingleton.err.println("jta: cannot load " + cfg);
        }
      }

    host = options.getProperty("Socket.host");
    port = options.getProperty("Socket.port");

    final JFrame frame = new JFrame("jta: " + host + (port.equals("23")?"":" " + port));
    //frame.setLayout(new BorderLayout());

    // setup language stuff
    String lang  = options.getProperty("lang");
    if(lang != null && lang.equalsIgnoreCase("cz"))
        resBundle = ResourceBundle.getBundle("de/resources/Czech", new Locale("cz", "CZ"));
    else
        resBundle = ResourceBundle.getBundle("de/resources/English", new Locale("en", "US"));      // DEFAULT LANGUAGE BUNDLE
    
    // set up the clipboard
    try {
      clipboard = frame.getToolkit().getSystemClipboard();
    } catch (Exception e) {
      de.mud.jta.OutputSingleton.err.println(resBundle.getString("CLIPBOARD_ACCESS_DENIED")); 
      de.mud.jta.OutputSingleton.err.println(resBundle.getString("CLIPBOARD_COPY_PASTE_RESTRICTION"));
      clipboard = new Clipboard("de.mud.jta.Main");
    }

    // configure the application and load all plugins
    final Common setup = new Common(options);

    if (port == null || port.length() == 0) {
      if (setup.getPlugins().containsKey("SSH")) {
        port = "22";
      } else {
        port = "23";
      }
    }

    
    final String usedHostName;
    
    if(hostName == null)
        usedHostName="jta:";
    else{
        usedHostName=hostName;
        hostName = null;  // null static hostName
    }
    
    
    setup.registerPluginListener(new OnlineStatusListener() {
        
      String localHostName = usedHostName;
        
      public void online() {
        frame.setTitle(localHostName + host + (port.equals("23")?"":" " + port));
        
      }

      public void offline() {
        frame.setTitle("jta: offline");
        this.localHostName = "";
      }
    });

    // register a focus status listener, so we know when a plugin got focus
    setup.registerPluginListener(new FocusStatusListener() {
      public void pluginGainedFocus(Plugin plugin) {
        if (Main.debug > 0)
          de.mud.jta.OutputSingleton.err.println("Main: " + plugin + " got focus");
        focussedPlugin = plugin;
      }

      public void pluginLostFocus(Plugin plugin) {
        // we ignore the lost focus
        if (Main.debug > 0)
          de.mud.jta.OutputSingleton.err.println("Main: " + plugin + " lost focus");
      }
    });

    Map componentList = setup.getComponents();
    Iterator names = componentList.keySet().iterator();
    while (names.hasNext()) {
      String name = (String) names.next();
      JComponent c = (JComponent) componentList.get(name);
      if (options.getProperty("layout." + name) == null) {
        de.mud.jta.OutputSingleton.err.println("jta: no layout property set for '" + name + "'");
        frame.add("South", c);
        //frame.add(c, BorderLayout.SOUTH);
      } else
        frame.getContentPane().add(options.getProperty("layout." + name), c);
    }

    if (!personalJava) {

      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent evt) {
          setup.broadcast(new SocketRequest());
          frame.setVisible(false);
          frame.dispose();
          //System.exit(0);
        }
      });

      // add a menu bar
      JMenuBar mb = new JMenuBar();
      JMenu file = new JMenu(resBundle.getString("FILE"));
      file.setMnemonic(KeyEvent.VK_F);
      JMenuItem tmp;
      file.add(tmp = new JMenuItem(resBundle.getString("CONNECT")));
      tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK));
      tmp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            
            StringBuilder initValue = new StringBuilder();
            if(!host.isEmpty() && !port.isEmpty())
                initValue.append(host).append(" ").append(port);
            
            
          String destination =
                  JOptionPane.showInputDialog(frame,
                                              new JLabel(resBundle.getString("DIALOG_HOST_PORT")),
                                               initValue.toString()
                  );
          if (destination != null) {
            int sep = 0;
            if ((sep = destination.indexOf(' ')) > 0 || (sep = destination.indexOf(':')) > 0) {
              host = destination.substring(0, sep);
              port = destination.substring(sep + 1);
            } else {
              host = destination;
            }
            setup.broadcast(new SocketRequest());
            setup.broadcast(new SocketRequest(host, Integer.parseInt(port)));
          }
        }
      });
      file.add(tmp = new JMenuItem(resBundle.getString("DISCONNECT")));
      tmp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          setup.broadcast(new SocketRequest());
        }
      });
      file.addSeparator();
      if (setup.getComponents().get("Terminal") != null) {
        file.add(tmp = new JMenuItem(resBundle.getString("PRINT")));
        tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        tmp.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              PrintJob printJob =
                      frame.getToolkit().getPrintJob(frame, "JTA Terminal", null);
              // return if the user clicked cancel
              if (printJob == null) return;
              ((JComponent) setup.getComponents().get("Terminal"))
                      .print(printJob.getGraphics());
              printJob.end();
          }
        });
        file.addSeparator();
      }
      file.add(tmp = new JMenuItem(resBundle.getString("EXIT")));
      tmp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          frame.dispose();
          //System.exit(0);
        }
      });
      mb.add(file);

      JMenu edit = new JMenu(resBundle.getString("EDIT"));
      edit.add(tmp = new JMenuItem(resBundle.getString("COPY")));
      tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
      tmp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (focussedPlugin instanceof VisualTransferPlugin)
            ((VisualTransferPlugin) focussedPlugin).copy(clipboard);
        }
      });
      edit.add(tmp = new JMenuItem(resBundle.getString("PASTE")));
      tmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
      tmp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (focussedPlugin instanceof VisualTransferPlugin)
            ((VisualTransferPlugin) focussedPlugin).paste(clipboard);
        }
      });
      mb.add(edit);

      Map menuList = setup.getMenus();
      names = menuList.keySet().iterator();
      while (names.hasNext()) {
        String name = (String) names.next();
        mb.add((JMenu) menuList.get(name));
      }

      JMenu help = new JMenu(resBundle.getString("HELP"));
      help.setMnemonic(KeyEvent.VK_HELP);
      help.add(tmp = new JMenuItem(resBundle.getString("GENERAL")));
      tmp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Help.show(frame, options.getProperty("Help.url"));
        }
      });
      mb.add(help);

      frame.setJMenuBar(mb);

    } // !personalJava

    frame.pack();

    if ((new Boolean(options.getProperty("Applet.detach.fullscreen"))
            .booleanValue()))
      frame.setSize(frame.getToolkit().getScreenSize());
    else
      frame.pack();

   // frame.setVisible(true);

    if(debug > 0) 
      de.mud.jta.OutputSingleton.err.println("host: '"+host+"', "+host.length());
    if (host != null && host.length() > 0) {
      setup.broadcast(new SocketRequest(host, Integer.parseInt(port)));
    }
    /* make sure the focus goes somewhere to start off with */
    setup.broadcast(new ReturnFocusRequest());
    return frame;
  }

  /**
   * Parse the command line argumens and override any standard options
   * with the new values if applicable.
   * <P><SMALL>
   * This method did not work with jdk 1.1.x as the setProperty()
   * method is not available. So it uses now the put() method from
   * Hashtable instead.
   * </SMALL>
   * @param options the original options
   * @param args the command line parameters
   * @return a possible error message if problems occur
   */
  private static String parseOptions(Properties options, String args[]) {
    boolean host = false, port = false;
    for (int n = 0; n < args.length; n++) {
      if (args[n].equals("-config"))
        if (!args[n + 1].startsWith("-"))
          options.put("Main.config", args[++n]);
        else
          return "missing parameter for -config";
      else if (args[n].equals("-plugins"))
        if (!args[n + 1].startsWith("-"))
          options.put("plugins", args[++n]);
        else
          return "missing parameter for -plugins";
      else if (args[n].equals("-addplugin"))
        if (!args[n + 1].startsWith("-"))
          options.put("plugins", args[++n] + "," + options.get("plugins"));
        else
          return "missing parameter for -addplugin";
      else if (args[n].equals("-term"))
        if (!args[n + 1].startsWith("-"))
          options.put("Terminal.id", args[++n]);
        else
          return "missing parameter for -term";
      else if (args[n].equals("-lang"))                   // language parameter added
          if (!args[n + 1].startsWith("-"))
              options.put("lang", args[++n]);
          else
          return "missing parameter for -lang";          // end of language parameter
      else if (!host) {
        options.put("Socket.host", args[n]);
        host = true;
      } else if (host && !port) {
        options.put("Socket.port", args[n]);
        port = true;
      } else
        return "unknown parameter '" + args[n] + "'";
    }
    return null;
  }
}
