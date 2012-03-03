package config.SimulatorEvents.Serializer;

import config.Serializer.SaveLoadException;
import config.SimulatorEvents.SerializedComponents.SimulatorEventsWrapper;
import java.io.File;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractSimulatorEventsSaveLoadInterface {
     public void saveEventsToFile(SimulatorEventsWrapper simulatorEvents, File file) throws SaveLoadException;
     public SimulatorEventsWrapper loadEventsFromFile(File file) throws SaveLoadException;
}
