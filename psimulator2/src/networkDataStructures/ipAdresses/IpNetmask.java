/*
 * Erstellt am 28.10.2011.
 */

package networkDataStructures.ipAdresses;

import networkDataStructures.ipAdresses.IpAdress;

/**
 *
 * @author neiss
 */
public class IpNetmask extends IpAdress{

    public IpNetmask(String dlouhejFormat) {
        super(dlouhejFormat);
        if(! jeMaskou(bytes)){
            throw new BadNetmaskException();
        }
    }

    /**
     * Ocekava to masku jako integer, kolik prvnich bitu maj bejt jednicky
     * @param maska
     */
    public IpNetmask(int numberOfBits) {
        if (numberOfBits > 32 || numberOfBits < 0) {
            throw new BadNetmaskException();
        }
        this.bytes = 0;
        for (int i = 0; i < numberOfBits; i++) {
            bytes = bytes | 1 << (31 - i);
        }
    }

    /**
     * Vraci true, kdyz je zadany integer maskou, tzn., kdyz jsou to nejdriv jednicky a pak nuly.
     * @param maska
     * @return
     */
    private static boolean jeMaskou(int maska) {
        int i = 0;
        while (i < 32 && (maska & 1) == 0) { //tady prochazeji nuly
            i++;
            maska = maska >> 1;
        }
        while (i < 32 && (maska & 1) == 1) { //tady prochazeji jednicky
            i++;
            maska = maska >> 1;
        }
        if (i == 32) {
            return true;
        }
        return false;
    }



}
