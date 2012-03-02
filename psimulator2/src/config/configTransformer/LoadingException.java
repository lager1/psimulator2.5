/*
 * Erstellt am 2.3.2012.
 */
package config.configTransformer;

/**
 *
 * @author neiss
 */
public class LoadingException extends RuntimeException {

	/**
	 * Creates a new instance of
	 * <code>LoadingException</code> without detail message.
	 */
	public LoadingException() {
	}

	/**
	 * Constructs an instance of
	 * <code>LoadingException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public LoadingException(String msg) {
		super(msg);
	}
}
