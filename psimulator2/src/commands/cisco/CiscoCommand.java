/*
 * created 6.3.2012
 */

package commands.cisco;

import commands.AbstractCommand;
import commands.AbstractCommandParser;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class CiscoCommand extends AbstractCommand {

	public final CiscoCommandParser parser;

	public CiscoCommand(AbstractCommandParser parser) {
		super(parser);
		this.parser = (CiscoCommandParser) parser;
	}
}
