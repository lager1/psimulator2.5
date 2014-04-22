package psimulator.userInterface;

import java.awt.Component;

import javax.swing.JRootPane;

import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import psimulator.userInterface.SimulatorEditor.UserInterfaceMainPanelState;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Graph.Graph;
import shared.Components.NetworkModel;
import shared.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface MainWindowInnerInterface {
    /**
     * Updates Undo and Redo APP buttons according to undo manager
     */
    public void updateUndoRedoButtons();
    /**
     * Updates ZoomIn and ZoomOut APP buttons according to zoom manager
     */
    public void updateZoomButtons();
    /**
     * Updates icons in toolbar according to size 
     * @param size Size to update to
     */
    public void updateToolBarIconsSize(ToolbarIconSizeEnum size);
    
    /**
     * Get root pane from main winfow
     * @return 
     */
    public JRootPane getRootPane();

    /**
     * Get main window component. Use for creating dialogs which has to have mainWindow
     * as parent.
     * @return 
     */
    public Component getMainWindowComponent();
    
    /**
     * Call when user wants to save events.
     * @param simulatorEventsWrapper 
     */
    public void saveEventsAction(SimulatorEventsWrapper simulatorEventsWrapper);
    
    /**
     * Call when user wants to load events.
     * @return 
     */
    public SimulatorEventsWrapper loadEventsAction();

    /**
     * Updates jPanelUserInterfaceMain according to userInterfaceState. If
     * changing to SIMULATOR or EDITOR state, graph cannot be null.
     *
     * @param graph Graph to set into jPanelUserInterfaceMain, can be null if
     * userInterfaceState will be WELCOME
     * @param userInterfaceState State to change to.
     * @param changingSimulatorEditor if true, the graph is kept untouched
     */
	public void refreshUserInterfaceMainPanel(Graph graph, NetworkModel networkModel, UserInterfaceMainPanelState userInterfaceState, boolean changingSimulatorEditor);
}
