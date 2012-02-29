package config.Serializer;

import config.Components.NetworkModel;
import java.io.File;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractNetworkSerializer {
    public void saveNetworkModelToFile(NetworkModel networkModel, File file) throws SaveLoadException;
    public NetworkModel loadNetworkModelFromFile(File file) throws SaveLoadException;
}
