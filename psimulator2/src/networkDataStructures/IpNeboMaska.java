/*
 * Erstellt am 27.10.2011.
 */

package networkDataStructures;

/**
 *
 * @author neiss
 */
public abstract class IpNeboMaska {

    /**
     * Inner representation of ip adress.
     */
    protected int bytes;

    public IpNeboMaska(int bytes) {
        this.bytes = bytes;
    }

    /**
     * Ze stringu ve tvaru 1.2.3.4 vrati int vnitrni representace.
     * @param ret
     */
    protected int stringToBytes(String adr) throws BadIpException{
        if (!adr.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) {
            throw new BadIpException();
        }
        //uz vim, ze se to sklada z cisel, pokracuju tedy:
        String[] pole = adr.split("\\."); //pole Stringu s jednotlivejma cislama
        int bajt; //aktualni bajt
        int vysledek=0;
        for (int i = 0; i < 4; i++) {
            bajt = Integer.valueOf(pole[i]);
            if (bajt < 0 || bajt > 255) { //podle me ta kontrola musi bejt takhle
                throw new BadIpException();
            }
            vysledek = vysledek | bajt << (8 * (3 - i));
        }
        return vysledek;
    }

    /**
     * Vrati IP adresu jako string ve formatu 1.2.3.4
     * @return
     */
    @Override
    public String toString() {
        int[]pole=jakoPole();
        String ret = pole[0] + "." + pole[1] + "." + pole[2] + "." + pole[3];
        return ret;
    }

    /**
     * Vrati ip jako pole integeru.
     * @param cislo
     * @return
     */
    protected int[] jakoPole() { //prevadi masku nebo adresu do citelny podoby
        int[] pole = new int[4];
        int tmp;
        for (int i = 0; i < 4; i++) {
            tmp = bytes & (255 << (3 - i) * 8);
            pole[i] = tmp >>> ((3 - i) * 8);
        }
        return pole;
    }



    /**
     * Copy construktor.
     * @return
     */
    @Override
    public IpNeboMaska clone() {
        return new IpNeboMaska(bytes);
    }

    public int getBytes() {
        return bytes;
    }

    protected class BadIpException extends RuntimeException{

    }



    

    



}
