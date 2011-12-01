/*
 * Erstellt am 28.10.2011.
 */

package dataStructures.ipAdresses;


/**
 *
 * @author neiss
 */
public class IPwithNetmask {

    private IpAdress ip;
    private IpNetmask mask;

	public IPwithNetmask(String adr) {
        ip=new IpAdress(adr);
        dopocitejMasku();
    }

    public IPwithNetmask(String adr, String maska) {
        ip=new IpAdress(adr);
        mask=new IpNetmask(maska);
    }

    public IPwithNetmask(String adr, int maska) {
        ip=new IpAdress(adr);
        mask=new IpNetmask(maska);
    }

    /**
     * Vytvori adresu smaskou ze zadaneho Stringu, kde muze nebo nemusi byt zadana adresa za lomitkem. <br />
     * Je-li moduloMaska nastaveno na true, maska za lomitkem se vymoduluje 32. POZOR: tzn., ze i
     * maska /32 se vymoduluje na /0! (takhle funguje LinuxIfconfig i LinuxRoute)<br />
     * Je-li modulo maska nastaveno na false, musi byt maska spravna, tzn. v intervalu <0,32>. <br />
     * Na chybny vstupy to hazi SpatnaMaskaException nebo SpatnaAdresaException, pricemz, kdyz
     * je spatny oboje, ma SpatnaAdresaException prednost. <br />
     * @param adrm
     * @param defMaska - pocet bitu
     * @param moduloMaska
     */
    public IPwithNetmask(String adrm, int defMaska, boolean moduloMaska) {
        //nejdriv se pro jistotu zkontrolujou zadany hodnoty:
        if(moduloMaska && defMaska<-1 && defMaska>32){
            throw new RuntimeException("V programu nastala chyba, kontaktujte prosim tvurce softwaru.");
        }

        int lomitko = adrm.indexOf('/');
        if (lomitko == -1) { // retezec neobsahuje lomitko
            ip=new IpAdress(adrm); //nastavuje se adresa
            if(defMaska<0){
                ip.dopocitejMasku();
            }else{
                mask=new IpNetmask(defMaska);
            }
        } else {  // je to s lomitkem, musi se to s nim zparsovat
            String adr = adrm.substring(0, lomitko);
            ip=new IpAdress(adr); //nastavuje se uz tady, aby prvni vyjimka se hazela na adresu
            String maska = adrm.substring(lomitko + 1, adrm.length());
            int m;
            //kontrola masky, jestli je to integer:
            try {
                m = Integer.parseInt(maska);
            } catch (NumberFormatException ex) {
                throw new BadNetmaskException();
            }
            if (moduloMaska) { //pripadne prepocitani masky:
                m = m % 32;
            }
            mask=new IpNetmask(m);  // nastaveni masky
        }
    }

	/**
     * Dopocita masku, podle tridy IP. Vyuziva se v konstruktoru, kdyz neni maska zadana.
     */
    private void dopocitejMasku(){
        mask=ip.dopocitejMasku();
    }

	public String getBroadcastAsString() {
        return IpAdress.bytesToString(broadcast());
    }

	/**
     * vraci 32 bitu adresy broadcastu site
     * @return
     */
    private int broadcast() {
        return (cisloSite() | (~(mask.getBytes())));
    }

	private int cisloSite() { //vraci 32bit integer
        return mask.getBytes() & ip.getBytes();
    }



}
