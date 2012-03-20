/*
 * created 13.3.2012
 */

package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import java.util.ArrayList;
import java.util.List;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.RoutingTable.Record;
import utils.Util;

/**
 * Trida reprezentujici wrapper nad routovaci tabulkou pro system cisco.
 * Tez bude sefovat zmenu v RT dle vlastnich rozhrani.
 * Cisco samo o sobe ma tez 2 tabulky: <br />
 *      1. zadane uzivatelem (tato trida) <br />
 *      2. vypocitane routy z tabulky c. 1 (trida RoutovaciTabulka)
 * @author Stanislav Řehák <rehaksta@fit.cvut.cz>
 */
public class CiscoWrapperRT implements Loggable {

	/**
     * Jednotlive radky wrapperu.
     */
    private List<CiscoRecord> radky;
	private final IPLayer ipLayer;
	private final Device device;
    /**
     * Odkaz na routovaci tabulku, ktera je wrapperem ovladana.
     */
    private RoutingTable routingTable;
    /**
     * ochrana proti smyckam v routovaci tabulce.
     * Kdyz to projede 50 rout, tak se hledani zastavi s tim, ze smula..
     */
    int citac = 0;
    private boolean debug = false;

    public CiscoWrapperRT(Device device, IPLayer ipLayer) {
        radky = new ArrayList<>();
		this.ipLayer = ipLayer;
        this.routingTable = ipLayer.routingTable;
		this.device = device;
    }

    /**
     * Vnitrni trida pro reprezentaci CiscoZaznamu ve wrapperu.
     * Adresat neni null, ale bud rozhrani nebo brana je vzdy null.
     */
    public class CiscoRecord {

        private IPwithNetmask adresat; // s maskou
        private IpAddress brana;
        private NetworkInterface rozhrani;
        private boolean connected = false;

        private CiscoRecord(IPwithNetmask adresat, IpAddress brana) {
            this.adresat = adresat;
            this.brana = brana;
        }

        private CiscoRecord(IPwithNetmask adresat, NetworkInterface rozhrani) {
            this.adresat = adresat;
            this.rozhrani = rozhrani;
        }

        /**
         * Pouze pro ucely vypisu RT!!! Jinak nepouzivat!
         * @param adresat
         * @param brana
         * @param rozhrani
         */
        private CiscoRecord(IPwithNetmask adresat, IpAddress brana, NetworkInterface rozhrani) {
            this.adresat = adresat;
            this.brana = brana;
            this.rozhrani = rozhrani;
        }

        public IPwithNetmask getAdresat() {
            return adresat;
        }

        public IpAddress getBrana() {
            return brana;
        }

        public NetworkInterface getRozhrani() {
            return rozhrani;
        }

        private void setConnected() {
            this.connected = true;
        }

        public boolean isConnected() {
            return connected;
        }

        @Override
        public String toString() {
            String s = adresat.getIp() + " " + adresat.getMask() + " ";
            if (brana == null) {
                s += rozhrani.name;
            } else {
                s += brana;
            }
            return s;
        }

        /**
         * CiscoZaznamy se rovnaji pokud adresat ma stejnou adresu i masku &&
         * ( se takto rovnaji i brany ) || ( rozhrani se jmenuji stejne nehlede na velikost pismen )
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != CiscoRecord.class) {
                return false;
            }

            if (adresat.equals(((CiscoRecord) obj).adresat)) {
                if (brana != null && ((CiscoRecord) obj).brana != null) {
                    if (brana.equals(((CiscoRecord) obj).brana)) {
                        return true;
                    }
                } else if (rozhrani != null && ((CiscoRecord) obj).rozhrani != null) {
                    if (rozhrani.name.equalsIgnoreCase(((CiscoRecord) obj).rozhrani.name)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.adresat != null ? this.adresat.hashCode() : 0);
            hash = 37 * hash + (this.brana != null ? this.brana.hashCode() : 0);
            hash = 37 * hash + (this.rozhrani != null ? this.rozhrani.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Tato metoda bude aktualizovat RoutovaciTabulku dle tohoto wrapperu.
     */
    public void update() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "update RT, pocet static zaznamu: "+radky.size(), null);

        // smazu RT
        routingTable.flushAllRecords();

        // nastavuju citac
        this.citac = 0;

        // pridam routy na nahozena rozhrani
        for (NetworkInterface iface : ipLayer.getNetworkIfaces()) {
            if (iface.isUp && iface.getIpAddress() != null && iface.ethernetInterface.isConnected()) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "Pridavam routy z nahozenych rozhrani do RT: "+iface.getIpAddress(), null);
                routingTable.addRecord(iface.getIpAddress().getNetworkNumber(), iface, true);
            }
        }

        // propocitam a pridam routy s prirazenyma rozhranima
        for (CiscoRecord zaznam : radky) {
            if (zaznam.rozhrani != null) { // kdyz to je na rozhrani
                if (zaznam.rozhrani.isUp) {
					Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "Pridavam zaznam na rozhrani.", zaznam);
                    routingTable.addRecord(zaznam.adresat, zaznam.rozhrani);
                }
            } else { // kdyz to je na branu
                NetworkInterface odeslat = najdiRozhraniProBranu(zaznam.brana);
                if (odeslat != null) {
                    if (odeslat.isUp) {
						Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "nasel jsem pro "+zaznam.adresat.toString() + " rozhrani "+odeslat.name, null);
                        routingTable.addRecordWithoutControl(zaznam.adresat, zaznam.brana, odeslat);
                    } else {
						Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "nasel jsem pro "+zaznam.adresat.toString() + " rozhrani "+odeslat.name+", ale je zhozene!!!", null);
					}
                } else {
					Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "Nenasel jsem pro tento zaznam zadne rozhrani, po kterem by to mohlo odejit..", zaznam);
//                    System.out.println("nenasel jsem pro "+ zaznam);
                }
            }
        }
    }

    /**
     * Vrati rozhrani, na ktere se ma odesilat, kdyz je zaznam na branu.
     * Tato metoda pocita s tim, ze v RT uz jsou zaznamy pro nahozena rozhrani.
     * @param brana
     * @return kdyz nelze nalezt zadne rozhrani, tak vrati null
     */
    NetworkInterface najdiRozhraniProBranu(IpAddress brana) {

        citac++;
        if (citac >= 101) {
            return null; // ochrana proti smyckam
        }
        for (int i = radky.size() - 1; i >= 0; i--) { // prochazim opacne (tedy vybiram s nejvyssim poctem jednicek)

            // kdyz to je na rozsah vlastniho rozhrani
			Record record = routingTable.findRoute(brana);
            if (record.rozhrani != null) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "najdiRozhraniProBranu: nalezeno rozhrani.. ok", brana);
                return record.rozhrani;
            } else {
				Logger.log(this, Logger.DEBUG, LoggingCategory.WRAPPER_CISCO, "najdiRozhraniProBranu: NEnalezeno rozhrani pro "+brana, null);
			}

            // kdyz to je na branu jako v retezu
            CiscoRecord zaznam = radky.get(i);
            if (zaznam.adresat.isInMyNetwork(brana)) {
                if (zaznam.rozhrani != null) { // 172.18.1.0 255.255.255.0 FastEthernet0/0
                    return zaznam.rozhrani;
                }
                return najdiRozhraniProBranu(zaznam.brana);
            }
        }
        return null;
    }

    /**
     * Pridava do wrapperu novou routu na branu.
     * @param adresa
     * @param brana
     */
    public void pridejZaznam(IPwithNetmask adresa, IpAddress brana) {
        CiscoRecord z = new CiscoRecord(adresa, brana);
        pridejZaznam(z);
    }

    /**
     * Pridava do wrapperu novou routu na rozhrani.
     * @param adresa
     * @param rozhrani
     */
    public void pridejZaznam(IPwithNetmask adresa, NetworkInterface rozhrani) {
        CiscoRecord z = new CiscoRecord(adresa, rozhrani);
        pridejZaznam(z);
    }

    /**
     * Prida do wrapperu novou routu na rozhrani. Pote updatuje RT je-li potreba.
     * V teto metode se kontroluje, zda adresat je cislem site.
     * @param zaznam, ktery chci vlozit
     */
    private void pridejZaznam(CiscoRecord zaznam) {

        if (!zaznam.getAdresat().isNetworkNumber()) { // vyjimka pro nacitani z konfiguraku, jinak to je osetreno v parserech
//            throw new RuntimeException("Adresa " + zaznam.getAdresat().getIp() + " neni cislem site!");
			Logger.log(this, Logger.WARNING, LoggingCategory.WRAPPER_CISCO, "Adresa " + zaznam.getAdresat().getIp() + " neni cislem site! Nepridano!", zaznam);
			return;
        }

        for (CiscoRecord z : radky) { // zaznamy ulozene v tabulce se uz znovu nepridavaji
            if (zaznam.equals(z)) {
                return;
            }
        }

        radky.add(dejIndexPozice(zaznam, true), zaznam);
        update();
    }

    /**
     * Malinko prasacka metoda pro pridani zaznamu do RT pouze pro vypis!
     * @param zaznam
     */
    private void pridejRTZaznamJenProVypis(Record zaznam) {
        CiscoRecord ciscozaznam = new CiscoRecord(zaznam.adresat, zaznam.brana, zaznam.rozhrani);
        if (zaznam.jePrimoPripojene()) {
            ciscozaznam.setConnected();
        }
        radky.add(dejIndexPozice(ciscozaznam, false), ciscozaznam);
    }

    /**
     * Smaze zaznam z wrapperu + aktualizuje RT. Rozhrani maze podle jmena!
     * Muze byt zadana bud adresa nebo adresa+brana nebo adresa+rozhrani.
     *
     * no ip route IP MASKA DALSI? <br />
     * IP a MASKA je povinne, DALSI := { ROZHRANI | BRANA } <br />
     *
     * @param adresa
     * @param brana
     * @param rozhrani
     * @return 0 = ok, 1 = nic se nesmazalo
     */
    public int smazZaznam(IPwithNetmask adresa, IpAddress brana, NetworkInterface rozhrani) {
        int i = -1;

        if (adresa == null) {
            return 1;
        }
        if (brana != null && rozhrani != null) {
            return 1;
        }

        // maze se zde pres specialni seznam, inac to hazi concurrent neco vyjimku..
        List<CiscoRecord> smazat = new ArrayList();

        for (CiscoRecord z : radky) {
            i++;

            if (!z.adresat.equals(adresa)) {
                continue;
            }

            if (brana == null && rozhrani == null) {
                smazat.add(radky.get(i));
            } else if (brana != null && rozhrani == null && z.brana != null) {
                if (z.brana.equals(brana)) {
                    smazat.add(radky.get(i));
                }
            } else if (brana == null && rozhrani != null) {
                if (z.rozhrani.name.equals(rozhrani.name)) {
                    smazat.add(radky.get(i));
                }
            }
        }

        if (smazat.isEmpty()) {
            return 1;
        }

        for (CiscoRecord zaznam : smazat) {
            radky.remove(zaznam);
        }

        update();

        return 0;
    }

    /**
     * Smaze vsechny zaznamy ve wrapperu + zaktualizuje RT
     * Prikaz 'clear ip route *'
     */
    public void smazVsechnyZaznamy() {
        radky.clear();
        update();
    }

    /**
     * Vrati pozici, na kterou se bude pridavat zaznam do wrapperu.
     * Je to razeny dle integeru cile.
     * @param pridavany, zaznam, ktery chceme pridat
     * @param nejminBituVMasce rika, jestli chceme radit nejdrive zaznamy maskou o mensim poctu 1,
     * pouziva se pri normalnim vkladani do wrapperu, false pro vypis RT
     * @return
     */
    private int dejIndexPozice(CiscoRecord pridavany, boolean nejminBituVMasce) {
        int i = 0;
        for (CiscoRecord cz : radky) {
            if (jeMensiIP(pridavany.adresat, cz.adresat, nejminBituVMasce)) {
                break;
            }
            i++;
        }
        return i;
    }

    /**
     * Vrati true, pokud je prvni adresa mensi nez druha, pokud se rovnaji, tak rozhoduje maska.
     * @param prvni
     * @param druha
     * @return
     */
    private boolean jeMensiIP(IPwithNetmask prvni, IPwithNetmask druha, boolean nejminBituVMasce) {

        // kdyz maj stejny IP a ruzny masky
        if (prvni.getIp().toString().equals(druha.getIp().toString())) {
            if (nejminBituVMasce) { // pro pridani do wrapperu
                if (prvni.getMask().getNumberOfBits() < druha.getMask().getNumberOfBits()) {
                    return true;
                }
            } else { // pro vypis RT
                if (prvni.getMask().getNumberOfBits() > druha.getMask().getNumberOfBits()) {
                    return true;
                }
            }
        }
        if (prvni.getIp().getLongRepresentation() < druha.getIp().getLongRepresentation()) {
            return true;
        }
        return false;
    }

    /**
     * Vrati CiscoRecord na indexu.
     * @param index
     * @return
     */
    public CiscoRecord vratZaznam(int index) {
        return radky.get(index);
    }

    /**
     * Vrati pocet zaznamu ve wrapperu.
     * @return
     */
    public int size() {
        return radky.size();
    }

    /**
     * Pro vypis pres 'sh run'
     * @return
     */
    public String vypisRunningConfig() {
        String s = "";
        for (CiscoRecord z : radky) {
            s += "ip route " + z + "\n";
        }
        return s;
    }

    /**
     * Vrati vypis routovaci tabulky.
     * Kasle se na tridni vypisy pro adresaty ze A rozsahu, protoze se v laborce takovy rozsah nepouziva.
     * @return
     */
    public String vypisRT() {
        String s = "";

        if (debug) {
            s += "Codes: C - connected, S - static\n\n";
        } else {
            s += "Codes: C - connected, S - static, R - RIP, M - mobile, B - BGP\n"
                    + "       D - EIGRP, EX - EIGRP external, O - OSPF, IA - OSPF inter area\n"
                    + "       N1 - OSPF NSSA external type 1, N2 - OSPF NSSA external type 2\n"
                    + "       E1 - OSPF external type 1, E2 - OSPF external type 2\n"
                    + "       i - IS-IS, su - IS-IS summary, L1 - IS-IS level-1, L2 - IS-IS level-2\n"
                    + "       ia - IS-IS inter area, * - candidate default, U - per-user static route\n"
                    + "       o - ODR, P - periodic downloaded static route\n\n";
        }

//        CiscoWrapperRT wrapper = ((CiscoPocitac) pc).getWrapper();
        boolean defaultGW = false;
        String brana = null;
		IPwithNetmask zeros = new IPwithNetmask("0.0.0.0", 0);
        for (int i = 0; i < size(); i++) {
            if (vratZaznam(i).adresat.equals(zeros)) {
                if (vratZaznam(i).brana != null) {
                    brana = vratZaznam(i).brana.toString();
                }
                defaultGW = true;
            }
        }

        s += "Gateway of last resort is ";
        if (defaultGW) {
            if (brana != null) {
                s += brana;
            } else {
                s += "0.0.0.0";
            }
            s += " to network 0.0.0.0\n\n";
        } else {
            s += "not set\n\n";
        }

        // vytvarim novy wrapperu kvuli zabudovanemu razeni
        CiscoWrapperRT wrapper_pro_razeni = new CiscoWrapperRT(device, ipLayer);
        for (int i = 0; i < routingTable.size(); i++) {
            wrapper_pro_razeni.pridejRTZaznamJenProVypis(routingTable.getRecord(i));
        }

        for (CiscoRecord czaznam : wrapper_pro_razeni.radky) {
            s += vypisZaznamDoRT(czaznam);
        }

        return s;
    }

    /**
     * Vrati vypis cisco zaznamu ve spravnem formatu pro RT
     * @param zaznam
     * @return
     */
    private String vypisZaznamDoRT(CiscoRecord zaznam) {
        String s = "";

        if (zaznam.isConnected()) { //C       21.21.21.0 is directly connected, FastEthernet0/0
            s += "C       " + zaznam.getAdresat().getNetworkNumber() + " is directly connected, " + zaznam.getRozhrani().name + "\n";
        } else { //S       18.18.18.0 [1/0] via 51.51.51.9
            if (zaznam.getAdresat().equals(new IPwithNetmask("0.0.0.0", 0))) {
                s += "S*      ";
            } else {
                s += "S       ";
            }
            s += zaznam.getAdresat().getIp() + "/" + zaznam.getAdresat().getMask().getNumberOfBits();
            if (zaznam.getBrana() != null) {
                s += " [1/0] via " + zaznam.getBrana();
            } else {
                s += " is directly connected, " + zaznam.getRozhrani().name;
            }
            s += "\n";
        }

        return s;
    }

	@Override
	public String getDescription() {
		return Util.zarovnej(device.getName(), Util.deviceNameAlign) + "wrapper";
	}
}
