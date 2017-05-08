package psimulator.userInterface;

import java.awt.Component;
//import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;

import psimulator.dataLayer.DataLayerFacade;
import psimulator.dataLayer.Enums.ObserverUpdateEventType;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.dataLayer.Singletons.ColorMixerSingleton;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.UndoRedo;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.Zoom;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanelState;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 * @author lager1
 */
public final class ToolBar extends JToolBar implements Observer {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3003452553897338438L;

	private DataLayerFacade dataLayer;
    
    private JButton jButtonNew;
    private JButton jButtonClose;
    private JButton jButtonOpen;
    private JButton jButtonSave;
    private JButton jButtonSaveAs;
    private JButton jButtonUndo;
    private JButton jButtonRedo;
    private JButton jButtonZoomIn;
    private JButton jButtonZoomOut;
    private JButton jButtonZoomReset;
    private JButton jButtonPreferences;
    private JButton jButtonStartSimulation;
    private JButton jButtonStopSimulation;
    private JButton jButtonServerLog;
    //
    //private ButtonGroup modeButtonGroup;
    //private JToggleButton jToggleButtonEditor;
    //private JToggleButton jToggleButtonSimulator;
    //
    //private Font font;
    
    //private Color backgroundColor = new Color(198, 83, 83);
    
    
    public ToolBar(DataLayerFacade dataLayer){
        super();
        this.dataLayer = dataLayer;
        
                
        // add this ToolBar as observer to languageManager
        dataLayer.addLanguageObserver((Observer)this);
        
        // add this as observer to preferences manager
        dataLayer.addPreferencesObserver((Observer)this);
       
        // tool bar is not possible to move
        this.setFloatable(false);
        
        jButtonNew = new JButton();
        
        jButtonClose = new JButton();
        jButtonOpen = new JButton();
        jButtonSave = new JButton();
        jButtonSaveAs = new JButton();
        
        jButtonPreferences = new JButton();
        
        jButtonUndo = new JButton();
        jButtonUndo.setActionCommand(UndoRedo.UNDO.toString());
        
        jButtonRedo = new JButton();
        jButtonRedo.setActionCommand(UndoRedo.REDO.toString());
        
        jButtonZoomIn = new JButton();
        jButtonZoomIn.setActionCommand(Zoom.IN.toString());
        
        jButtonZoomOut = new JButton();
        jButtonZoomOut.setActionCommand(Zoom.OUT.toString());
                
        jButtonZoomReset = new JButton();
        jButtonZoomReset.setActionCommand(Zoom.RESET.toString());

        jButtonStartSimulation = new JButton();
        jButtonStartSimulation.setActionCommand(UserInterfaceMainPanelState.SIMULATOR.toString());

        jButtonStopSimulation = new JButton();
        jButtonStopSimulation.setActionCommand(UserInterfaceMainPanelState.EDITOR.toString());

        jButtonServerLog = new JButton();
        
        //modeButtonGroup = new ButtonGroup();
        
        //jToggleButtonEditor = new JToggleButton();
        //jToggleButtonEditor.setActionCommand(UserInterfaceMainPanelState.EDITOR.toString());
        //jToggleButtonSimulator = new JToggleButton();
        //jToggleButtonSimulator.setActionCommand(UserInterfaceMainPanelState.SIMULATOR.toString());


        
        //modeButtonGroup.add(jToggleButtonEditor);
        //modeButtonGroup.add(jToggleButtonSimulator);
        
        this.add(jButtonNew);
        this.add(jButtonClose);
        this.addSeparator();
        
        this.add(jButtonOpen);
        this.add(jButtonSave);
        this.add(jButtonSaveAs);
        this.addSeparator();
        
        this.add(jButtonPreferences);
        this.addSeparator();
        
        this.add(jButtonZoomIn);
        this.add(jButtonZoomOut);
        this.add(jButtonZoomReset);
        this.addSeparator();
        
        this.add(jButtonUndo);
        this.add(jButtonRedo);
        this.addSeparator();
        this.add(jButtonStartSimulation);
        this.add(jButtonStopSimulation);
        this.add(jButtonServerLog);

        
        this.add(Box.createHorizontalGlue());
        //this.add(jToggleButtonEditor);
        //this.add(jToggleButtonSimulator);
        
        setTextsToComponents();
        
        // apply background color
        this.setBackground(ColorMixerSingleton.mainToolbarColor);
        Component [] comp = this.getComponents();
        
        for(Component c : comp){
            c.setBackground(ColorMixerSingleton.mainToolbarColor);
            // tool icon cannot be marked (ugly frame)
            c.setFocusable(false);
        }
        
        updateIconSize(dataLayer.getToolbarIconSize());
    }

    @Override
    public void update(Observable o, Object o1) {
        // find out what event type occured
        switch((ObserverUpdateEventType)o1){
            case LANGUAGE:
                this.setTextsToComponents();
                break;
            case ICON_SIZE:
                this.updateIconSize(dataLayer.getToolbarIconSize());
                break;
		default:
			break;
        }
    }
    
    /**
     * Updates images on toolbar buttons according to size
     * @param size 
     */
    public void updateIconSize(ToolbarIconSizeEnum size){
        String path = "/resources/toolbarIcons/";
        
        switch(size){
            case TINY:
                path += "16";
                //font = new Font("Tahoma", 0, 10);
                break;
            case SMALL:
                path += "22";
                //font = new Font("Tahoma", 0, 12);
                break;
            case MEDIUM:
                path += "32";
                //font = new Font("Tahoma", 0, 14);
                break;
            case LARGE:
                path += "48";
                //font = new Font("Tahoma", 0, 18);
                break;
        }
        path += "/";

        jButtonNew.setIcon(new ImageIcon(getClass().getResource(path+"filenew.png")));
        jButtonClose.setIcon(new ImageIcon(getClass().getResource(path+"fileclose.png")));
        jButtonOpen.setIcon(new ImageIcon(getClass().getResource(path+"folder_green_open.png")));
        jButtonSave.setIcon(new ImageIcon(getClass().getResource(path+"filesave.png")));
        jButtonSaveAs.setIcon(new ImageIcon(getClass().getResource(path+"filesaveas.png")));
        jButtonUndo.setIcon(new ImageIcon(getClass().getResource(path+"undo.png")));
        jButtonRedo.setIcon(new ImageIcon(getClass().getResource(path+"redo.png")));
        jButtonZoomIn.setIcon(new ImageIcon(getClass().getResource(path+"viewmag+.png")));
        jButtonZoomOut.setIcon(new ImageIcon(getClass().getResource(path+"viewmag-.png")));
        jButtonZoomReset.setIcon(new ImageIcon(getClass().getResource(path+"viewmag1.png")));
        jButtonPreferences.setIcon(new ImageIcon(getClass().getResource(path+"configure.png")));
        //jToggleButtonEditor.setIcon(new ImageIcon(getClass().getResource(path+"editor.png")));
        //jToggleButtonSimulator.setIcon(new ImageIcon(getClass().getResource(path+"exec.png")));
        jButtonStartSimulation.setIcon(new ImageIcon(getClass().getResource(path+"backend_start.png")));
        jButtonStopSimulation.setIcon(new ImageIcon(getClass().getResource(path+"backend_stop.png")));
        jButtonServerLog.setIcon(new ImageIcon(getClass().getResource(path+"server_log.png")));
        
        //jToggleButtonEditor.setFont(font);
        //jToggleButtonSimulator.setFont(font);
    }
    
    
    public void setUndoEnabled(boolean enabled){
        jButtonUndo.setEnabled(enabled);
    }
    
    public void setRedoEnabled(boolean enabled){
        jButtonRedo.setEnabled(enabled);
    }
    
    public void setZoomInEnabled(boolean enabled){
        jButtonZoomIn.setEnabled(enabled);
    }
    
    public void setZoomOutEnabled(boolean enabled){
        jButtonZoomOut.setEnabled(enabled);
    }
    
    public void setZoomResetEnabled(boolean enabled){
        jButtonZoomReset.setEnabled(enabled);
    }
    
    public void setSimulatorSelected(boolean selected){
        //jToggleButtonSimulator.setSelected(selected);
        jButtonStartSimulation.setVisible(false);
        jButtonStopSimulation.setVisible(true);
        jButtonServerLog.setVisible(true);
    }
    
    public void setEditorSelected(boolean selected){
        //jToggleButtonEditor.setSelected(selected);
        jButtonStartSimulation.setVisible(true);
        jButtonStopSimulation.setVisible(false);
        jButtonServerLog.setVisible(false);
    }

    public void setStartSimulationEnabled(boolean enabled){
        jButtonStartSimulation.setEnabled(enabled);
    }

    public void setStopSimulationEnabled(boolean enabled){
        jButtonStopSimulation.setEnabled(enabled);
    }

    public void setServerLogVisible(boolean enabled){
        jButtonServerLog.setVisible(enabled);
    }

    public void setProjectRelatedButtonsEnabled(boolean enabled, UserInterfaceMainPanelState userInterfaceState){
        jButtonClose.setEnabled(enabled);
        jButtonSave.setEnabled(enabled);
        jButtonSaveAs.setEnabled(enabled);

        jButtonStartSimulation.setEnabled(enabled);
        jButtonStopSimulation.setEnabled(enabled);

        //jToggleButtonEditor.setVisible(enabled);
        //jToggleButtonSimulator.setVisible(enabled);
        
        if(userInterfaceState == UserInterfaceMainPanelState.EDITOR){
            jButtonUndo.setVisible(enabled);
            jButtonRedo.setVisible(enabled);
            jButtonServerLog.setEnabled(false);
        }else{
            jButtonUndo.setVisible(false);
            jButtonRedo.setVisible(false);
            jButtonServerLog.setEnabled(true);
        }
    }

    /**
     * Adds action listener to jButtonNew
     * @param listener Action listener
     */
    public void addNewProjectActionListener(ActionListener listener){
        jButtonNew.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jButtonClose
     * @param listener Action listener
     */
    public void addCloseActionListener(ActionListener listener){
        jButtonClose.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jButtonOpen
     * @param listener Action listener
     */
    public void addOpenActionListener(ActionListener listener){
        jButtonOpen.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jButtonSave
     * @param listener Action listener
     */
    public void addSaveActionListener(ActionListener listener){
        jButtonSave.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jButtonSaveAs
     * @param listener Action listener
     */
    public void addSaveAsActionListener(ActionListener listener){
        jButtonSaveAs.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jButton preferences
     * @param listener Action listener
     */
    public void addPreferencesActionListener(ActionListener listener){
        jButtonPreferences.addActionListener(listener);
    }
    
    /**
     * Adds action listener to jButtonUndo and jButtonRedo
     * @param listener Action listener
     */
    public void addUndoRedoActionListener(ActionListener listener){
        jButtonUndo.addActionListener(listener);
        jButtonRedo.addActionListener(listener);
    }
    
    /**
     * Adds action listener to Zoom Buttons
     * @param listener Action listener
     */
    public void addZoomActionListener(ActionListener listener){
        jButtonZoomIn.addActionListener(listener);
        jButtonZoomOut.addActionListener(listener);
        jButtonZoomReset.addActionListener(listener);
    }

    /**
     * Adds action listener to editor and simulator toggle buttons
     * @param listener 
     */
    public void addSimulatorEditorActionListener(ActionListener listener){
        //jToggleButtonEditor.addActionListener(listener);
        //jToggleButtonSimulator.addActionListener(listener);
        
        jButtonStartSimulation.addActionListener(listener);
        jButtonStopSimulation.addActionListener(listener);
    }

    /**
     * Adds action listener to Server log button
     * @param listener Action listener
     */
    public void addServerLogActionListener(ActionListener listener){
        jButtonServerLog.addActionListener(listener);
    }

    
    
    /*
    jButtonStartSimulation.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // if connected
            if (simulatorManagerInterface.isConnectedToServer()) {
                // disconnect
                simulatorManagerInterface.doDisconnect();
            } else {
                // open connect dialog
                connectToServerDialog = new ConnectToServerDialog(mainWindow.getMainWindowComponent(), dataLayer);

                // set visible
                connectToServerDialog.setVisible(true);
            }
        }
    });
    */
    
    /*
    public void addBackendActionListener(ActionListener listener){
        jButtonBackendConnection.addActionListener(listener);
    }
	*/
    
    
    ////////------------ PRIVATE------------///////////
    
    private void setTextsToComponents() {
        jButtonNew.setToolTipText(dataLayer.getString("NEW_PROJECT"));
        jButtonClose.setToolTipText(dataLayer.getString("CLOSE"));
        jButtonOpen.setToolTipText(dataLayer.getString("OPEN"));
        jButtonSave.setToolTipText(dataLayer.getString("SAVE"));
        jButtonSaveAs.setToolTipText(dataLayer.getString("SAVE_AS"));
        jButtonUndo.setToolTipText(dataLayer.getString("UNDO"));
        jButtonRedo.setToolTipText(dataLayer.getString("REDO"));
        jButtonZoomIn.setToolTipText(dataLayer.getString("ZOOM_IN"));
        jButtonZoomOut.setToolTipText(dataLayer.getString("ZOOM_OUT"));
        jButtonZoomReset.setToolTipText(dataLayer.getString("ZOOM_RESET"));
        jButtonPreferences.setToolTipText(dataLayer.getString("PREFERENCES"));
        //
        //jToggleButtonEditor.setText(dataLayer.getString("EDITOR"));
        //jToggleButtonSimulator.setText(dataLayer.getString("SIMULATOR"));
        //jToggleButtonEditor.setToolTipText(dataLayer.getString("SWITCHES_TO_EDITOR"));
        //jToggleButtonSimulator.setToolTipText(dataLayer.getString("SWITCHES_TO_SIMULATOR"));
        //
        jButtonStopSimulation.setToolTipText(dataLayer.getString("SWITCHES_TO_EDITOR"));
        jButtonStartSimulation.setToolTipText(dataLayer.getString("SWITCHES_TO_SIMULATOR"));
        jButtonServerLog.setToolTipText(dataLayer.getString("SERVER_LOG"));
    }
}
