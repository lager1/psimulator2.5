package config.Network;

import java.io.Serializable;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public interface AbstractComponentModel extends Identifiable, Serializable{
    
    /**
     * Returns unique ID
     * @return 
     */
    @Override
    public Integer getId();
    
    /**
     * Returs HwType of component
     * @return 
     */
    public HwTypeEnum getHwType();

    public void setHwType(HwTypeEnum hwType);

    public void setId(Integer id);
    
}
