package psimulator.userInterface;

import java.awt.Dimension;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;

/**
*
* @author lager1
*/

public class ServerLog {
    private JFrame logFrame;
    private JTextArea textArea;
    private File log;
    private DataLayerFacade dataLayer;

    /**
     * Set up variables, window and display text
     *
     * @param log       - server log file
     * @param dataLayer
     */

    ServerLog(File log, DataLayerFacade dataLayer) {
        this.log = log;
        this.dataLayer = dataLayer;
        init();
        displayText();
    }

    /**
     * Initialize main frame and text area
     */
    private void init() {
        logFrame = new JFrame(dataLayer.getString("SERVER_LOG"));    // set up main frame
        logFrame.setSize(1024, 500);
        logFrame.setIconImage(ImageFactorySingleton.getInstance().getImageIcon("/resources/toolbarIcons/32/server_log.png").getImage());
        logFrame.setMinimumSize(new Dimension(400, 400));    // set minimum size

        textArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);    // scrollbar

        logFrame.add(scroll);                // add scrollbar
        logFrame.setVisible(true);            // set on screen
    }


    private void displayText() {
        Tail t = new Tail(log.toString(), textArea);    // continuously read given file and append to specified text area
    }
}
