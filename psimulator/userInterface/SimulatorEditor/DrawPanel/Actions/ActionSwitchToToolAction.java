package psimulator.userInterface.SimulatorEditor.DrawPanel.Actions;

import java.awt.event.ActionEvent;
import javax.swing.undo.UndoManager;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ActionSwitchToToolAction extends AbstractDrawPanelAction{

    private static final long serialVersionUID = -7724838771317442692L;
    private MainTool mainTool;

    public ActionSwitchToToolAction(UndoManager undoManager, DrawPanelInnerInterface drawPanel,
            MainWindowInnerInterface mainWindow, MainTool mainTool) {
        super(undoManager, drawPanel, mainWindow);

        this.mainTool = mainTool;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        drawPanel.doSetToolInEditorToolBar(mainTool);
    }

}
