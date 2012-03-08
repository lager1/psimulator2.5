/*
 * created 5.3.2012
 */

package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.*;
import networkModule.L3.NatAccessList.AccessList;
import networkModule.L3.NatPool.Pool;

/**
 * TODO: poresit generovani portu kvuli kolizim s poslouchajicima aplikacema.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatTable {

	private final IPLayer ipLayer;

	List<Record> table = new ArrayList<>();
    /**
     * seznam poolu IP.
     */
    public NatPool lPool;
    /**
     * seznam seznamAccess-listu
     * (= kdyz zdrojova IP patri do nejakeho seznamAccess-listu, tak se bude zrovna natovat)
     */
    public NatAccessList lAccess;
    /**
     * seznam prirazenych poolu k access-listum
     */
    public NatPoolAccess lPoolAccess;
    /**
     * Seznam soukromych (inside) rozhrani.
     */
    Map<String, NetworkInterface> inside = new HashMap<>();
    /**
     * Verejne (outside) rozhrani.
     */
    NetworkInterface outside;
    private boolean linux_nastavena_maskarada = false;
//    /**
//     * citac, odkud mam rozdavat porty
//     */
//    private int citacPortu = 1025;
    boolean debug = false;


    public NatTable(IPLayer ipLayer) {
        lAccess = new NatAccessList();
        lPool = new NatPool(this);
        lPoolAccess = new NatPoolAccess();
		this.ipLayer = ipLayer;
    }

	public List<Record> getNatTable() {
		return table;
	}

//    /**
//     * True, pokud je uz tam je zdrojova ip v tabulce.
//     * @param in adresa, kterou chceme porovnavat
//     * @param staticRule udava, jestli se ma hledat jen mezi statickymi (true) nebo dynamickymi (false)
//     * @return
//     */
//    private boolean jeTamZdrojova(IpAddress in, boolean staticRule) {
//        for (Record record : table) {
//            if (record.staticRule == staticRule && record.in.jeStejnaAdresaSPortem(in)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * True, pokud je uz tam je prelozena ip v tabulce.
//     * @param out adresa, kterou chceme porovnavat
//     * @param staticRule udava, jestli se ma hledat jen mezi statickymi (true) nebo dynamickymi (false)
//     * @return
//     */
//    private boolean jeTamPrelozena(IpAddress out, boolean staticke) {
//        for (Record zaznam : table) {
//            if (zaznam.staticRule == staticke && zaznam.out.jeStejnaAdresaSPortem(out)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Vrati pozici pro pridani do tabulky.
     * Radi se to dle out adresy vzestupne.
     * @param out
     * @return index noveho zaznamu
     */
    private int dejIndexVTabulce(IpAddress out) {
        int index = 0;
        for (Record zaznam : table) {
            if (out.getLongRepresentation() < zaznam.out.getLongRepresentation()) {
                break;
            }
            index++;
        }
        return index;
    }

//    /**
//     * Hleda mezi statickymi pravidly, jestli tam je zaznam pro danou IP.
//     * @param zdroj
//     * @return zanatovana IP <br />
//     *         null pokud nic nenaslo
//     */
//    public IpAddress najdiStatickePravidloIn(IpAddress zdroj) {
//        for (Record zaznam : table) {
//            if (zaznam.staticRule && zaznam.in.jeStejnaAdresa(zdroj)) {
//                return zaznam.out;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Hleda mezi statickymi pravidly, jestli tam je zaznam pro danou IP.
//     * @param zdroj
//     * @return odnatovana IP <br />
//     *         null pokud nic nenaslo
//     */
//    public IpAddress najdiStatickePravidloOut(IpAddress zdroj) {
//        for (Record zaznam : table) {
//            if (zaznam.staticRule && zaznam.out.jeStejnaAdresa(zdroj)) {
//                return zaznam.in;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Vrati true, pokud muze odnatovat adr pomoci statickeho nebo dynamickeho pravidla.
//     * Jinak false.
//     * @param adr
//     * @return
//     */
//    public boolean mamZaznamOutProIp(IpAddress adr) {
//        IpAddress ip = odnatujZdrojovouIpAdresu(adr);
//        if (ip == null) {
//            return false;
//        }
//        return true;
//    }

    /**
     * Reprezentuje jeden radek v NAT tabulce.
     */
    public class Record {

        /**
         * Zdrojova ip.
         */
        public final IpAddress in;
        /**
         * Zdrojova prelozena ip.
         */
        public final IpAddress out;
        /**
         * Potreba pro vypis v ciscu. Je null, kdyz se vkladaji staticka pravidla.
         */
        public final IpAddress target;
        /**
         * Vlozeno staticky - true, dynamicky - false.
         */
        public final boolean staticRule;
        /**
         * Cas vlozeni v ms (pocet ms od January 1, 1970)
         */
        private long timestamp;

        public Record(IpAddress in, IpAddress out, boolean staticke) {
            this.in = in;
            this.out = out;
            this.target = null;
            this.staticRule = staticke;
            this.timestamp = System.currentTimeMillis();
        }

        public Record(IpAddress in, IpAddress out, IpAddress cil, boolean staticke) {
            this.in = in;
            this.out = out;
            this.target = cil;
            this.staticRule = staticke;
            this.timestamp = System.currentTimeMillis();
        }

		@Deprecated
        public IpAddress vratIn() {
            return in;
        }

		@Deprecated
        public IpAddress vratOut() {
            return out;
        }

        public long getTimestamp() {
            return timestamp;
        }

		@Deprecated
        public boolean jeStaticke() {
            return staticRule;
        }

        /**
         * Obnovi zaznam na dalsich 10s.
         */
        public void touch() {
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Vrati outside rozhrani.
     * @return
     */
    public NetworkInterface getOutside() {
        return outside;
    }

    /**
     * Vrati seznam inside rozhrani.
     * @return
     */
    public Collection<NetworkInterface> getInside() {
        return inside.values();
    }

    /**
     * Vrati tabulku.
     * @return
     */
    public List<Record> getTable() {
        return table;
    }

//    /**
//     * Vrati paket se prelozenou zdrojovou IP adresou.
//     * @param paket
//     * @return
//     */
//    public Paket zanatuj(Paket paket) {
//        paket.zdroj = zanatujZdrojovouIpAdresu(paket, true);
//        return paket;
//    }
//
//    /**
//     * Pokud je zaznam pro cilovou adresu v NAT tabulce, tak se to prelozi na spravnou soukromou.
//     * Jinak se vrati paket zpet nezmenen.
//     * @param paket
//     * @return
//     */
//    public Paket odnatuj(Paket paket) {
//        IpAdresa prelozena = odnatujZdrojovouIpAdresu(paket.cil);
//        if (debug) {
//            pc.vypis("puvodni paket:   " + paket);
//        }
//        if (prelozena == null) {
//            return paket;
//        }
//        paket.cil = prelozena;
//        if (debug) {
//            pc.vypis("prelozeny paket: " + paket);
//        }
//        deleteOldDynamicRecords();
//        return paket;
//    }

    /**
     * Vrati true, pokud je mozne danou adresu prelozit. Jinak vrati false.
     * @param adr
     * @return true - musi byt adr v access-listu, k nemu priraznem pool s alespon 1 volnou IP adresou. <br />
     */
    public boolean lzePrelozit(IpAddress adr) {

        AccessList access = lAccess.vratAccessListIP(adr);
        if (access == null) {
            return false;
        }

        Pool pool = lPool.vratPoolZAccessListu(access);
        if (pool == null) {
            return false;
        }

        IpAddress nova = pool.dejIp(true);
        if (nova == null) {
            return false;
        }

        return true;
    }

//    /**
//     * Dle teto metody se bude pocitac rozhodovat, co delat s paketem.
//     * Nevola se hned zanatuj, pac musime rozlisovat, kdy natovat, kdy nenatovat a kdy vratit Destination Host Unreachable.
//     * @param zdroj
//     * @return 0 - ano natovat se bude <br />
//     *         1 - ne, nemam pool - vrat zpatky Destination Host Unreachable <br />
//     *         2 - ne, dosli IP adresy z poolu - vrat zpatky Destination Host Unreachable
//     *         3 - ne, vstupni neni soukrome nebo vystupni neni outside <br />
//     *         4 - ne, zdrojova Ip neni v seznamu access-listu, tak nechat normalne projit bez natovani <br />
//     *         5 - ne, neni nastaveno outside rozhrani
//     */
//    public int mamNatovat(IpAddress zdroj, NetworkInterface vstupni, NetworkInterface vystupni) {
//
//        boolean vstupniJeInside = false;
//        for (NetworkInterface iface : inside) {
//            if (iface.jmeno.equals(vstupni.jmeno)) {
//                vstupniJeInside = true;
//            }
//        }
//        if (outside == null) {
//            return 5;
//        }
//        if ((!vystupni.name.equals(outside.name)) || vstupniJeInside == false) {
//            return 3;
//        }
//        //-----------------------------------------------------------------------------
//
//        if (najdiStatickePravidloIn(zdroj) != null) {
//            return 0;
//        }
//
//        // neni v access-listech
//        AccessList acc = lAccess.vratAccessListIP(zdroj);
//        if (acc == null) {
//            return 4;
//        }
//
//        // kdyz neni prirazen pool
//        Pool pool = lPool.vratPoolZAccessListu(acc);
//        if (pool == null) {
//            return 1;
//        }
//
//        IpAddress adr = pool.dejIp(true);
//        if (adr == null) {
//            return 2;
//        }
//
//        return 0;
//    }

//    /**
//     * Kontrola, jestli paket prisel do pocitace z outside site.
//     * @param prichoziRozhrani
//     * @return true - paket z outside site <br />
//     *         false - paket odjinud - ne-odnatovavat nebo kdyz neni nastavene outside rozhrani
//     */
//    public boolean mamOdnatovat(NetworkInterface prichoziRozhrani) {
//        if (outside == null) {
//            if (debug) {
//                pc.vypis("Verejne je null; prichozi rozhrani: " + prichoziRozhrani.jmeno + " pc:" + pc.jmeno);
//            }
//            return false;
//        }
//        if (prichoziRozhrani.jmeno.equals(outside.name)) {
//            if (debug) {
//                pc.vypis("prichozi rozhrani '" + prichoziRozhrani.jmeno + "' je verejne, natuji; verejne je " + outside.jmeno);
//            }
//            return true;
//        }
//        if (debug) {
//            pc.vypis("prichozi rozhrani '" + prichoziRozhrani.jmeno + "', nenatuji; verejne je " + outside.jmeno);
//        }
//        return false;
//    }

//    /**
//     * Vrati IpAdresu, ktera se pouzije jako zdrojova pri odeslani paketu.
//     * Nejdriv se projde natovaci table, jestli to tam uz neni. Kdyz neni,
//     * tak se zkusi vygenerovat novy zaznam. Musi mit ale spravne nakonfigurovany access-listy+pooly.
//     * Tato metoda se vola, kdyz uz vim, ze ma prirazeny pool+access-list, tak uz to ma vzdycky vratit prelozenou adresu.
//     * V teto metode se take mazou stare dynamicke zaznamy - jako prvni.
//     * @param ip
//     * @param natovani - true, kdyz natuju, false - kdyz se jen ptam, zda je volno v poolu
//     * @return Adresu - na kterou se to ma prelozit <br />
//     *         null - kdyz dosel pool IP adress, tak se ma vratit odesilateli Destination Host Unreachable,
//     *                null by to melo vratit pouze pri natovani==false nebo kdyz neni zadnej pouzitelnej pool
//     */
//    private IpAdresa zanatujZdrojovouIpAdresu(Paket paket, boolean natovani) {
//
//        deleteOldDynamicRecords();
//        IpAdresa ip = paket.zdroj;
//
//        // nejdriv prochazim staticka pravidla
//        for (Record zaznam : table) {
//            if (zaznam.staticRule && zaznam.in.jeStejnaAdresa(ip)) {
//                IpAdresa vrat = zaznam.out.vratKopii();
//                vrat.port = ip.port;
//                return vrat;
//            }
//        }
//
//        // nejdriv kontroluju, jestli uz to nahodou nema dynamicky zaznam v NATtabulce
//        for (Record zaznam : table) { // porovnavam i podle portu (mohou byt NATy za sebou..)
//            if (!zaznam.staticRule && zaznam.in.jeStejnaAdresaSPortem(ip)) {
//                zaznam.touch();
//                return zaznam.out;
//            }
//        }
//
//        // staticky ani dynamicky zaznam neni, tak vygenerujeme novy
//        AccessList access = lAccess.vratAccessListIP(ip);
//        Pool pool = lPool.vratPoolZAccessListu(access);
//        IpAdresa vrat = lPool.dejIpZPoolu(pool);
//
//        vrat.port = vygenerujPort(vrat);
//        if (natovani == true) { // jen kdyz opravdu pridavam
//            // kopiruju si novou IP, pri pridavani do tabulku se prepisovaly zaznamy
//            pridejZaznamDynamcikyDoNATtabulky(ip.vratKopii(), vrat.vratKopii(), paket.cil.vratKopii());
//        }
//        return vrat;
//    }
//
//    /**
//     * Mrkne se do tabulky a vrati prislusny zaznam pokud existuje.
//     * Kdyz je v tabulce staticky zaznam, tak to prelozi na adresu o stejnem portu.
//     * @param ip
//     * @return null - pokud neexistuje zaznam pro danou ip
//     */
//    private IpAdresa odnatujZdrojovouIpAdresu(IpAdresa ip) {
//        for (Record zaznam : table) {
//            if (zaznam.staticRule) {
//                if (zaznam.out.jeStejnaAdresa(ip)) {
//                    IpAdresa vrat = zaznam.in;
//                    vrat.port = ip.port;
//                    return vrat;
//                }
//            }
//        }
//
//        for (Record zaznam : table) {
//            if (zaznam.out.jeStejnaAdresaSPortem(ip)) {
//                return zaznam.in;
//            }
//        }
//        return null;
//    }

    /**
     * Prida zaznam do natovaci tabulky. Pouziva se to pri dynamickym natovani.
     * @param in zdrojova IP
     * @param out nova zdrojova (prelozena)
     */
    private void pridejZaznamDynamcikyDoNATtabulky(IpAddress in, IpAddress out, IpAddress cil) {
        table.add(new Record(in, out, cil, false));
    }

    /**
     * Smaze stare (starsi nez 10s) dynamicke zaznamy v tabulce.
     * @return pocet dynamickych zaznamu, ktere se smazaly
     */
    public int deleteOldDynamicRecords() {
        long now = System.currentTimeMillis();
        List<Record> delete = new ArrayList<>();
        for (Record record : table) {
            if (record.staticRule == false) { // jen dynamicke zaznamy
                if (now - record.getTimestamp() > 10_000) {
                    delete.add(record);
                }
            }
        }
        int pocet = delete.size();
		table.removeAll(delete);

        return pocet;
    }

//    /**
//     * Vygeneruje unikatni port pro prelozeny zaznam.
//     * Generuje v rozsahu 1025-65536 <br />
//     * @param vrat
//     * @return
//     */
//    public int vygenerujPort(IpAdresa vrat) {
//        int port = 333;
//        port = 1025 + (int)(Math.random() * 65536);
//
//        for (Record z: table) {
//            if (vrat.jeStejnaAdresa(z.out) && z.out.port == port) return vygenerujPort(vrat);
//        }
//
//        return port;
//    }

    /****************************************** staticRule natovani ******************************************************/

//    /**
//     * Nasype IpAdresy ze statickych pravidel na dane rozhrani.
//     * Kdyz je rozhrani null, tak se nic neudela.
//     * Mimo tridu NATtabulka by se to melo pouzivat jen pri cteni z konfiguraku.
//     * @param iface outside rozhrani (outside)
//     */
//    public void pridejIpAdresyZeStatickychPravidel(SitoveRozhrani iface) {
//        if (iface == null) {
//            return;
//        }
//        for (Record zaznam : table) {
//            if (zaznam.staticRule) {
//                iface.seznamAdres.add(zaznam.out.vratKopii());
//            }
//        }
//    }

//    /**
//     * Smaze vsechny staticRule zaznamy, ktere maji odpovidajici in a out.
//     * Dale aktualizuje outside rozhrani co se IP tyce. Nejdrive smaze vsechny krom prvni,
//     * a pak postupne prida ze statickych a pak i z poolu.
//     * @return 0 - alespon 1 zaznam se smazal <br />
//     *         1 - nic se nesmazalo, pac nebyl nalezen odpovidajici zaznam (% Translation not found)
//     */
//    public int smazStatickyZaznam(IpAddress in, IpAddress out) {
//
//        List<Record> smaznout = new ArrayList<>();
//        for (Record zaznam : table) {
//            if (zaznam.staticRule && in.jeStejnaAdresa(zaznam.in) && out.jeStejnaAdresa(zaznam.out)) {
//                smaznout.add(zaznam);
//            }
//        }
//
//        if (smaznout.isEmpty()) {
//            return 1;
//        }
//
//        for (Record z : smaznout) {
//            table.remove(z);
//        }
//
//        return 0;
//    }

    /****************************************** nastavovani rozhrani ***************************************************/
    /**
     * Prida inside rozhrani. <br />
     * Neprida se pokud uz tam je rozhrani se stejnym jmenem. <br />
     * Pro pouziti prikazu 'ip nat inside'.
     * @param iface
     */
    public void pridejRozhraniInside(NetworkInterface iface) {
		if (!inside.containsKey(iface.name)) { // nepridavam uz pridane
			inside.put(iface.name, iface);
		}
    }

    /**
     * Nastavi outside rozhrani.
     * @param iface
     */
    public void nastavRozhraniOutside(NetworkInterface iface) {
        outside = iface;
//        lPool.updateIpNaRozhrani();
    }

    /**
     * Smaze toto rozhrani z inside listu.
     * Kdyz to rozhrani neni v inside, tak se nestane nic.
     * @param iface
     */
    public void smazRozhraniInside(NetworkInterface iface) {
		inside.remove(iface.name);
    }

    /**
     * Smaze vsechny inside rozhrani.
     */
    public void smazRozhraniInsideVsechny() {
        inside.clear();
    }

    /**
     * Smaze outside rozhrani.
     */
    public void smazRozhraniOutside() {
        outside = null;
    }

    /****************************************** Cisco *********************************************************/
//    /**
//     * Prida staticRule pravidlo do tabulky.
//     * Razeno vzestupne dle out adresy.
//     * @param in zdrojova IP urcena pro preklad
//     * @param out nova (prelozena) adresa
//     * @return 0 - ok, zaznam uspesne pridan <br />
//     *         1 - chyba, in adresa tam uz je (% in already mapped (in -> out)) <br />
//     *         2 - chyba, out adresa tam uz je (% similar static entry (in -> out) already exists)
//     */
//    public int pridejStatickePravidloCisco(IpAddress in, IpAddress out) {
//
//        if (jeTamZdrojova(in, true)) {
//            return 1;
//        }
//        if (jeTamPrelozena(out, true)) {
//            return 2;
//        }
//
//        int index = dejIndexVTabulce(out);
//        table.add(index, new Record(in, out, true));
//
//        return 0;
//    }

//    /**
//     * Vrati vypis vsech zaznamu v tabulce.
//     * Nejdrive to vypise dynamicka pravidla, pak staticka.
//     * Nejdriv se smazou stare dynamcike zaznamy.
//     * @return
//     */
//    public String vypisZaznamyCisco() {
//        deleteOldDynamicRecords();
//
//        String s = "";
//        if (table.size() == 0) {
//            s += "\n\n";
//            return s;
//        }
//
//        s += zarovnej("Pro Inside global", 24) + zarovnej("Inside local", 20);
//        s += zarovnej("Outside local", 20) + zarovnej("Outside global", 20);
//        s += "\n";
//
//        for (Record zaznam : table) {
//            if (zaznam.staticRule == false) {
//                s += zarovnej("icmp " + zaznam.out.vypisAdresuSPortem(), 24)
//                        + zarovnej(zaznam.in.vypisAdresuSPortem(), 20)
//                        + zarovnej(zaznam.target.vypisAdresuSPortem(), 20)
//                        + zarovnej(zaznam.target.vypisAdresuSPortem(), 20)+"\n";
//            }
//        }
//
//        for (Record zaznam : table) {
//            if (zaznam.staticRule) {
//                s += zarovnej("--- " + zaznam.out.vypisAdresu(), 24)
//                        + zarovnej(zaznam.in.vypisAdresu(), 20)
//                        + zarovnej("---", 20)
//                        + zarovnej("---", 20)+"\n";
//            }
//        }
//
//        return s;
//    }

//    /**
//     * Pomocny servisni vypis.
//     * Nejdriv se smazou stare dynamcike zaznamy.
//     * @return
//     */
//    public String vypisZaznamyDynamicky() {
//        deleteOldDynamicRecords();
//        String s = "";
//
//        for (Record zaznam : table) {
//            if (zaznam.staticRule == false) {
//                s += zaznam.in.vypisAdresuSPortem() + "\t" + zaznam.out.vypisAdresuSPortem() + "\n";
//            }
//        }
//        return s;
//    }

    /****************************************** Linux *********************************************************/
    /**
     * Nastavi Linux pocitac pro natovani. Kdyz uz je nastavena, nic nedela.
     * Pocitam s tim, ze ani pc ani rozhrani neni null.
     * Jestli jsem to dobre pochopil, tak tohle je ten zpusob natovani, kdy se vsechny pakety jdouci
     * ven po nejakym rozhrani prekladaj na nejakou verejnou adresu, a z toho rozhrani zase zpatky.
     * Prikaz napr: "iptables -t nat -I POSTROUTING -o eth2 -j MASQUERADE" - vsechny pakety jdouci ven
     * po rozhrani eth2 se prekladaj.
     * @param pc
     * @param outside, urci ze je tohle rozhrani outside a ostatni jsou automaticky soukroma.
     */
    public void nastavLinuxMaskaradu(NetworkInterface verejne) {

        if (linux_nastavena_maskarada) {
            return;
        }

        // nastaveni rozhrani
        inside.clear();
        for (NetworkInterface iface : ipLayer.getNetworkIfaces()) {
            if (iface.name.equals(verejne.name)) {
                continue; // preskakuju verejny
            }
            // vsechny ostatni nastrkam do inside
            pridejRozhraniInside(iface);
        }
        nastavRozhraniOutside(verejne);

        // osefovani access-listu
        int cislo = 1;
        lAccess.smazAccessListyVsechny();
        lAccess.pridejAccessList(new IPwithNetmask("0.0.0.0", 0), cislo);

        // osefovani IP poolu
        String pool = "ovrld";
        lPool.smazPoolVsechny();
        lPool.pridejPool(verejne.getIpAddress(), verejne.getIpAddress(), 24, pool);

        lPoolAccess.smazPoolAccessVsechny();
        lPoolAccess.pridejPoolAccess(cislo, pool, true);

        linux_nastavena_maskarada = true;
    }

    /**
     * Zrusi linux DNAT. Kdyz neni nastavena, nic nedela.
     */
    public void zrusLinuxMaskaradu() {

        lAccess.smazAccessListyVsechny();
        lPool.smazPoolVsechny();
        lPoolAccess.smazPoolAccessVsechny();
        smazRozhraniOutside();
        smazRozhraniInsideVsechny();
        linux_nastavena_maskarada = false;
    }

    public boolean jeNastavenaLinuxovaMaskarada() {
        return linux_nastavena_maskarada;
    }

    /**
     * Nastavi promennou na true.
     */
    public void nastavZKonfigurakuLinuxBooleanTrue() {
        linux_nastavena_maskarada = true;
    }

    /**
     * Prida staticRule pravidlo do NAT tabulky. Nic se nekontroluje.
     * @param in zdrojova IP
     * @param out nova zdrojova (prelozena)
     */
    public void pridejStatickePravidloLinux(IpAddress in, IpAddress out) {
        table.add(new Record(in, out, true));
    }
}
