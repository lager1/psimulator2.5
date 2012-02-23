/*
 * created 1.2.2012
 */

package networkModule.L4;

import networkModule.Layer;
import networkModule.NetMod;

/**
 * Implementace transportni vrstvy sitovyho modulu.
 * Nebezi v vlastnim vlakne, je to vlastne jen rozhrani mezi aplikacema a 3. vrstvou.
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class TcpIpLayer extends Layer {

	public TcpIpLayer(NetMod netMod) {
		super(netMod);
	}
}
