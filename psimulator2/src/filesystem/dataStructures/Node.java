

package filesystem.dataStructures;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public abstract class Node {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public abstract String toString();
	
}
