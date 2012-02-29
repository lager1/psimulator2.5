package config.Network;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface PositionInterface {
    public int getDefaultZoomXPos();
    public int getDefaultZoomYPos();
    
    public void setDefaultZoomXPos(int defaultZoomXPos);
    public void setDefaultZoomYPos(int defaultZoomYPos);
}
