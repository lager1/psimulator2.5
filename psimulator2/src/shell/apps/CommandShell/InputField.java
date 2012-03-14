

package shell.apps.CommandShell;

import java.io.IOException;
import telnetd.io.BasicTerminalIO;
import telnetd.io.toolkit.ActiveComponent;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class InputField extends ActiveComponent{

	
	
	
	public InputField(BasicTerminalIO io, String name) {
		super(io, name);
	}

	
	
	@Override
	public void run() throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void draw() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
