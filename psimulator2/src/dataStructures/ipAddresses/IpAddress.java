/*
 * Erstellt am 27.10.2011.
 */

package dataStructures.ipAddresses;


/**
 * Representation of IPv4 IP adress.
 * @author Tomas Pitrinec
 */
public class IpAddress {

    /**
     * Inner representation of ip adress.
     */
    protected int bits;

    /**
     * Creates an IP adress from String in format 1.2.3.4
     * @param ret
     */
    public IpAddress(String ret) {
        bits=stringToBits(ret);
    }

    /**
     * Only for construktor classes in this package.
     */
    protected IpAddress(){}

	/**
	 * Returns inner representation.
	 * @return
	 */
    public int getBits(){
        return bits;
    }

    /**
     * Vrati IP adresu jako string ve formatu 1.2.3.4
     * @return
     */
    @Override
    public String toString(){
        return bitsToString(bits);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IpAddress other = (IpAddress) obj;
        if (this.bits != other.bits) {
            return false;
        }
        return true;
    }

	/**
     * Vrati long hodnotu z adresy. Vhodne pro porovnavani adres.
     * @param ip
     * @return
     * @author Stanislav Řehák
     */
    public long getLongRepresentation() {
        long l = 0;
        String[] pole = toString().split("\\.");
        l += Long.valueOf(pole[0]) * 256 * 256 * 256;
        l += Long.valueOf(pole[1]) * 256 * 256;
        l += Long.valueOf(pole[2]) * 256;
        l += Long.valueOf(pole[3]);
        return l;
    }


// staticky metody pro ruzny prevadeni a tak: ----------------------------------------------------------------------------------------

    /**
     * Vrati adresu utvorenou ze stringu, kdyz je zadanej string ip adresou, jinak vrati null.
     * @param ret
     * @return
     */
    public static IpAddress correctAddress(String ret){
        try{
            IpAddress ip = new IpAddress(ret);
            return ip;
        } catch (BadIpException bIP){
            return null;
        }
    }

    /**
     * Vrati adresu o jedna vetsi.
     * Udelany metosou pokus - omyl, ale testy prosly.
     * @param p
     * @return adresu o jednicku vetsi, maska bude 255.0.0.0
     * @author Stanislav Řehák
     */
    public static IpAddress nextAddress(IpAddress p){
        int nova=(int) ( (long)(p.bits) + 1L );
        return createIpFromBits(nova);
    }

    /**
     * Negates given ip.
     * Neguje ip adresu, tzn. tam, kde driv byly jednicky dava nuly a naopak.
     * Nahrada za Standovu starou vratMaskuZWildCard, narozdil od ni ale uz nezkouma, jestli je vysledek maskou.
     * @param ip
     * @return
     */
    public static IpAddress negateAddress(IpAddress ip){
        return createIpFromBits(~ip.bits);
    }

    /**
     * Vytvori adresu ze integeru vnitrni reprezentace (z bitu)
     * @param r - ta vnitrni reprezentace
     * @return
     */
    protected static IpAddress createIpFromBits(int r){
        IpAddress vratit = new IpAddress();
        vratit.bits = r;
        return vratit;
    }


    /**
     * Ze stringu ve tvaru 1.2.3.4 vrati int vnitrni representace.
     * Pritom samozrejme zkontroluje spravnost a kdyztak vyhodi vyjimku.
     * @param ret
     */
    protected static int stringToBits(String adr) throws BadIpException{
        if (!adr.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) {
            throw new BadIpException("Spatna IP: \""+adr+"\"");
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
     * Bity prevadi na pole integeru.
     * @param cislo
     * @return
     */
    protected static int[] bitsToArray(int bits) { //prevadi adresu do citelny podoby
        int[] pole = new int[4];
        int tmp;
        for (int i = 0; i < 4; i++) {
            tmp = bits & (255 << (3 - i) * 8);
            pole[i] = tmp >>> ((3 - i) * 8);
        }
        return pole;
    }

    /**
     * Pole integeru prevadi na string.
     * @param array
     * @return
     */
    protected static String arrayToString(int[] array) {
        String ret = array[0] + "." + array[1] + "." + array[2] + "." + array[3];
        return ret;
    }

    /**
     * Ze zadanejch bitu vytvori string.
     * @param bits
     * @return
     */
    protected static String bitsToString(int bits) {
        return arrayToString(bitsToArray(bits));
    }








}
