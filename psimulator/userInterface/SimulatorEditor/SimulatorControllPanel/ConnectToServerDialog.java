package psimulator.userInterface.SimulatorEditor.SimulatorControllPanel;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;

import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Singletons.ImageFactory.ImageFactorySingleton;
import psimulator.userInterface.Dialogs.AbstractPropertiesDialog;
import psimulator.userInterface.SimulatorEditor.DrawPanel.SwingComponents.WaitLayerUI;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 * @author lager1
 */
public final class ConnectToServerDialog extends AbstractPropertiesDialog {

	private static final long serialVersionUID = -7539290373922798227L;
	final WaitLayerUI waitLayerUI = new WaitLayerUI();
    private boolean connectingActive = false;

    public ConnectToServerDialog(Component mainWindow, DataLayerFacade dataLayer) {
        super(mainWindow, dataLayer);

        // set icon
        this.setIconImage(ImageFactorySingleton.getInstance().getImageIcon("/resources/toolbarIcons/32/kwifimanager.png").getImage());
        
        // set title
        this.setTitle(dataLayer.getString("CONNECT_TO_SERVER"));

        // initialize
        initialize();
        waitLayerUI.start();	// start the animation
    }

    /**
     * Call when connected. The dialog is disposed
     */
    public void connected() {
    	
        connectingActive = false;

        // stop animation
        waitLayerUI.stop();

        // dispose
        thisDialog.setVisible(false);
        thisDialog.dispose();    //closes the window
    }

    /**
     * Call when connecting failed. The animation is stopped and components are
     * enabled
     */
    public void connectingFailed() {
        connectingActive = false;

        // stop animation
        waitLayerUI.stop();

        // inform user
        JOptionPane.showMessageDialog(parentComponent,
                dataLayer.getString("CONNECTION_ESTABLISH_NOT_SUCCESSFUL"),
                dataLayer.getString("WARNING"),
                JOptionPane.WARNING_MESSAGE);
    }

    @Override
    protected void copyValuesFromGlobalToLocal() {
    }

    @Override
    protected void copyValuesFromFieldsToLocal() {
    }

    @Override
    protected void copyValuesFromLocalToGlobal() {
    }

    @Override
    protected boolean hasChangesMade() {
        return false;
    }

    @Override
    protected void windowClosing() {
        if (connectingActive) {
            dataLayer.getSimulatorManager().doDisconnect();
            //
            connectingActive = false;
        }
    }

    @Override
    protected void setDefaultJButton() {

    }

    @Override
    protected JPanel createMainPanel() {
        JPanel wrapPanel = new JPanel();
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        wrapPanel.add(new JLayer<>(mainPanel, waitLayerUI));
        return wrapPanel;
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel addressesPanel = new JPanel();
        addressesPanel.setMinimumSize(new Dimension(500, 500));
        return addressesPanel;
    }
}
