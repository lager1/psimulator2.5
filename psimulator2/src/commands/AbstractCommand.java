/*
 * created 6.3.2012
 */

package commands;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class AbstractCommand {

	public final AbstractCommandParser parser;

	public AbstractCommand(AbstractCommandParser parser) {
		this.parser = parser;
	}

	public String nextWord() {
		return parser.nextWord();
	}

	public String nextWordPeek() {
		return parser.nextWordPeek();
	}

	protected int getRef() {
		return parser.ref;
	}

	public abstract void catchUserInput(String input);
}
