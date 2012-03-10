/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface SignalCatchAble {

	public enum Signal {
		/**
		 * Ctrl+C
		 */
		CTRL_C,
		/**
		 * Ctrl+Z
		 */
		CTRL_Z,
		/**
		 * CTRL+D
		 */
		CTRL_D

	}


	public void catchSignal(Signal signal);

}
