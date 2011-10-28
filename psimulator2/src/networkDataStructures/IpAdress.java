/*
 * Erstellt am 28.10.2011.
 */

package networkDataStructures;

/**
 *
 * @author neiss
 */
public class IpAdress extends IpNeboMaska{

    public IpAdress(int bytes) {
        super(bytes);
    }

    public IpAdress(String adrret){
        bytes=stringToBytes(adrret);
    }





}
