package psimulator.userInterface.SimulatorEditor.DrawPanel.Actions;

import java.awt.event.ActionEvent;
import javax.swing.undo.UndoManager;
import psimulator.userInterface.MainWindowInnerInterface;
import psimulator.userInterface.SimulatorEditor.DrawPanel.DrawPanelInnerInterface;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ActionFitToSize extends AbstractDrawPanelAction{

    private static final long serialVersionUID = -8718640816579931905L;

    public ActionFitToSize(UndoManager undoManager, DrawPanelInnerInterface drawPanel, MainWindowInnerInterface mainWindow) {
        super(undoManager, drawPanel, mainWindow);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        drawPanel.doFitToGraphSize();
        //System.out.println("Fit to size nefunguje");
    }

}
