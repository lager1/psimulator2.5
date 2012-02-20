

package config.AbstractNetwork.AdditionsUI;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz> Lukáš <lukasma1@fit.cvut.cz>
 */
public class UINetworkDevice {
    
    private int x;
    private int y;

    public UINetworkDevice() {
    }

    public UINetworkDevice(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
}
