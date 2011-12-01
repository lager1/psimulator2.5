/*
 * Erstellt am 27.10.2011.
 */

package dataStructures.ipAdresses;

/**
 * Representation of IPv4 IP adress.
 * @author neiss
 */
public class IpAdress {

    /**
     * Inner representation of ip adress.
     */
    protected int bytes;

    /**
     * Creates an IP adress from String in format 1.2.3.4
     * @param ret
     */
    public IpAdress(String ret) {
        bytes=stringToBytes(ret);
    }

    /**
     * Only for construktor in IpNetmask
     */
    protected IpAdress() {
    }

    /**
     * Returns a copy of this IP.
     * @return
     */
    public IpAdress copy(){
        IpAdress v= new IpAdress();
        v.bytes=this.bytes;
        return v;
    }

    public int getBytes(){
        return bytes;
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
     * Vrati dopocitanou masku, podle tridy IP. Vyuziva se v konstruktoru, kdyz neni maska zadana.
     */
    public IpNetmask dopocitejMasku(){
        int bajt = jakoPole()[0]; //tady je ulozenej prvni bajt adresy
        /*
            A 	0 	0–127    	255.0.0.0 	7 	24 	126 	16 777 214
            B 	10 	128-191 	255.255.0.0 	14 	16 	16384 	65534
            C 	110 	192-223 	255.255.255.0 	21 	8 	2 097 152 	254
            D 	1110 	224-239 	multicast
            E 	1111 	240-255 	vyhrazeno jako rezerva
        */
        if( bajt<128 ) return new IpNetmask(8);
        if( bajt>=128 && bajt<192 ) return new IpNetmask(16);
        //if(bajt >= 192)
            return new IpNetmask(24);
        //System.out.println("1. bajt je "+ bajt+" a tak jsem nastavil "+ pocetBituMasky());
    }


    /**
     * Ze stringu ve tvaru 1.2.3.4 vrati int vnitrni representace.
     * Pritom samozrejme zkontroluje spravnost a kdyztak vyhodi vyjimku.
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
     * Vrati ip jako pole integeru.
     * @param cislo
     * @return
     */
    protected int[] jakoPole() { //prevadi adresu do citelny podoby
        int[] pole = new int[4];
        int tmp;
        for (int i = 0; i < 4; i++) {
            tmp = bytes & (255 << (3 - i) * 8);
            pole[i] = tmp >>> ((3 - i) * 8);
        }
        return pole;
    }









}
