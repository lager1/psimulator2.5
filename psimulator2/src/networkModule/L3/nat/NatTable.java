/*
 * created 5.3.2012
 */

package networkModule.L3.nat;

import networkModule.L3.nat.AccessList;
import dataStructures.IpPacket;
import dataStructures.L4Packet;
import dataStructures.L4Packet.L4PacketType;
import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.*;
import logging.Loggable;
import logging.Logger;
import logging.LoggingCategory;
import networkModule.L3.IPLayer;
import networkModule.L3.NetworkInterface;
import utils.Util;

/**
 * TODO: poresit generovani portu kvuli kolizim s poslouchajicima aplikacema.
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatTable implements Loggable {

	private final IPLayer ipLayer;

	List<Record> table = new ArrayList<>();
    /**
     * seznam poolu IP.
     */
    public HolderPoolList lPool;
    /**
     * seznam seznamAccess-listu
     * (= kdyz zdrojova IP patri do nejakeho seznamAccess-listu, tak se bude zrovna natovat)
     */
    public HolderAccessList lAccess;
    /**
     * seznam prirazenych poolu k access-listum
     */
    public HolderPoolAccess lPoolAccess;
    /**
     * Seznam soukromych (inside) rozhrani.
     */
    Map<String, NetworkInterface> inside = new HashMap<>();
    /**
     * Verejne (outside) rozhrani.
     */
    NetworkInterface outside; // = new HashMap<>(); // nevim, jestli potreva jich mit, pak to asi predelam
    private boolean linux_nastavena_maskarada = false;

	/**
	 * Dynamicke zaznamy starsi nez tato hodnota se smazou.
	 */
	private long natRecordLife = 10_000;
	private static int numberOfPorts = 36535;

	/**
	 * V teto prvotni implementaci nebudou vubec reseny kolize s portama aplikaci.
	 * TODO: generovat porty v zavislosti k IP adresam, takto se mohou brzy vycerpat..
	 */
	private Set<Integer> freePorts = new HashSet<>(numberOfPorts);

    public NatTable(IPLayer ipLayer) {
        this.ipLayer = ipLayer;
		lAccess = new HolderAccessList();
        lPoolAccess = new HolderPoolAccess();
		lPool = new HolderPoolList(this);

		for (int i = 1; i <= numberOfPorts; i++) { // naplnim si tabulku volnych portu
			freePorts.add(i);
		}
    }

	//--------------------------------------------- getters and setters ---------------------------------------------

	/**
	 * Returns NetworkAddressTranslation table.
	 * @return
	 */
	public List<Record> getRules() {
		return table;
	}

	/**
     * Returns outside interface.
     * @return
     */
    public NetworkInterface getOutside() {
        return outside;
    }

    /**
     * Returns list of inside interfaces.
     * @return
     */
    public Collection<NetworkInterface> getInside() {
        return inside.values();
    }

	/**
     * Returns interface or null.
     * @return
     */
	public NetworkInterface getInside(String name) {
		return inside.get(name);
	}

//	/**
//	 * Returns sorted list of inside interfaces.
//	 * @return
//	 */
//	public List<NetworkInterface> getInsideSorted() {
//		List<NetworkInterface> ifaces = new ArrayList<>(inside.values());
//		Collections.sort(ifaces);
//		return ifaces;
//	}

	@Override
	public String getDescription() {
		return Util.zarovnej(ipLayer.getNetMod().getDevice().getName(), Util.deviceNameAlign)+ " " + "natTable";
	}

	//--------------------------------------------- forward translation ---------------------------------------------

	/**
     * Dle teto metody se bude pocitac rozhodovat, co delat s paketem.
     * Nevola se hned zanatuj, pac musime rozlisovat, kdy natovat, kdy nenatovat a kdy vratit Destination Host Unreachable.
     * @param zdroj
     * @return 0 - ano natovat se bude <br />
     *         1 - ne, nemam pool - vrat zpatky Destination Host Unreachable <br />
     *         2 - ne, dosli IP adresy z poolu - vrat zpatky Destination Host Unreachable
     *         3 - ne, vstupni neni soukrome nebo vystupni neni verejne <br />
     *         4 - ne, zdrojova Ip neni v seznamu access-listu, tak nechat normalne projit bez natovani <br />
     *         5 - ne, neni nastaveno outside rozhrani
     */
	/**
	 * Executes forward translation of packet.
	 *
	 * @param packet to translate
	 * @param in incomming interface - can be null iff I am sending this packet
	 * @param out outgoing interface - never null
	 * @return
	 */
	public IpPacket translate(IpPacket packet, NetworkInterface in, NetworkInterface out) {

		/*
		 * Nenatuje se kdyz:
		 * 1 - nemam pool
		 *		+ Destination Host Unreachable
		 * 2 - dosly IP adresy z poolu
		 *		+ Destination Host Unreachable
		 * 3 - vstupni neni soukrome nebo vystupni neni verejne
		 * 4 - zdrojova IP neni v seznamu access-listu, tak nechat normalne projit bez natovani
		 * 5 - neni nastaveno outside rozhrani
		 *
		 * Jinak se natuje.
		 */

		if (packet.data == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.NetworkAddressTranslation, "Nenatuji: prisel mi paket, ktery nema L4 data, takze nemuzu zjistit jeho port!?", packet);
			return packet;
		}

		boolean vstupniJeInside = false;
		NetworkInterface insideTemp = inside.get(in == null ? null : in.name); // TODO: osetrit null !!! - spravne chovani!!!
		if (insideTemp != null) {
			vstupniJeInside = true;
		}

		if (outside == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Nenatuji: neni nastaveno outside rozhrani.", null);
			return packet; // 5
		}

		if (!out.name.equals(outside.name) || !vstupniJeInside) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Nenatuji: vstupni neni inside nebo vystupni neni outside.", null);
			return packet; // 3
		}

		IpAddress srcTranslated = najdiStatickePravidloIn(packet.src);
        if (srcTranslated != null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, "Natuji: nalezeno staticke pravidlo!", null);
			return staticTranslation(packet, srcTranslated); // 0
        }

        // neni v access-listech, tak se nanatuje
        AccessList acc = lAccess.vratAccessListIP(packet.src);
        if (acc == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Nenatuji: zdrojova adresa neni v access-listech.", null);
            return packet; // 4
        }

        // je v access-listech, ale neni prirazen pool, vrat DHU
        Pool pool = lPool.vratPoolZAccessListu(acc);
        if (pool == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, "Nenatuji a posilam DHU: zdrojova adresa je v access-listech, ale neni vstupni neni prirazen pool..", null);
			// poslat DHU
			ipLayer.getIcmpHandler().sendDestinationHostUnreachable(packet.src, null);
            return null; // 1
        }

        IpAddress adr = pool.dejIp(true);
        if (adr == null) {
			Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, "Nenatuji a posilam DHU: neni dostupna zadna volna IP adresa pro zanatovani.", null);
			ipLayer.getIcmpHandler().sendDestinationHostUnreachable(packet.src, null);
            return null; // 2
        }

        return dynamicTranslation(packet);
	}

    /**
     * Hleda mezi statickymi pravidly, jestli tam je zaznam pro danou IP.
     * @param zdroj
     * @return zanatovana IP <br />
     *         null pokud nic nenaslo
     */
    public IpAddress najdiStatickePravidloIn(IpAddress zdroj) {
        for (Record record : table) {
            if (record.isStatic && record.in.address.equals(zdroj)) {
                return record.out.address;
            }
        }
        return null;
    }

	/**
	 * Translates packet with static NetworkAddressTranslation rule.
	 * @param packet to translate
	 * @param srcTranslated new source IP
	 * @return
	 */
	private IpPacket staticTranslation(IpPacket packet, IpAddress srcTranslated) {
		logNatOperation(packet, true, true);
		IpPacket p = new IpPacket(srcTranslated, packet.dst, packet.ttl, packet.data); // port se tu nemeni (je v packet.data)
		logNatOperation(p, true, false);
		return p;
	}

	private void logNatOperation(IpPacket packet, boolean natting, boolean before) {
		String op;
		if (natting) {
			op = "Zanatuji";
		} else {
			op = "Odnatuji";
		}

		String when;
		if (before) {
			when = "before";
		} else {
			when = " after";
		}

		Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format(op+": "+when+": "+"src: %s:%d dst: %s:%d",
						packet.src.toString(), packet.data.getPortSrc(), packet.dst.toString(), packet.data.getPortDst()), packet);
	}

	/**
	* Translates packet with dynamic NetworkAddressTranslation. <br />
	* Returns null iff someone tries to translate L3Packet without L4 data = without port number. <br />
	* Returns untranslated packet iff there are now free ports number.
	* @param packet
	* @return
	*/
	private IpPacket dynamicTranslation(IpPacket packet) {
		deleteOldDynamicRecords();

		InnerRecord tempRecord = generateInnerRecordForSrc(packet);
		if (tempRecord == null) { // jen ochrana, kdyby nejakej smoula sem poslal paket, ktery nema L4 data!
			return packet;
		}

		// projdu aktualni dynamicke zaznamy a jestli uz tam je takovy preklad, tak mu prodlouzim zivot a necham se prelozit
		for (Record record : table) {
			if (!record.isStatic && record.in.equals(tempRecord)) {
				logNatOperation(packet, true, true);
//				Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation,
//						String.format("Natuji: %s:%d na %s:%d", record.in.address.toString(), record.in.port, record.out.address.toString(), record.out.port), packet);
				IpPacket p = new IpPacket(record.out.address, packet.dst, packet.ttl, packet.data);
				logNatOperation(packet, true, false);
				record.touch();
				return p;
			}
		}

		// nenasel se stary, tak vygenerujeme novy
		AccessList access = lAccess.vratAccessListIP(packet.src);
		Pool pool = lPool.vratPoolZAccessListu(access);
        IpAddress srcIpNew = lPool.dejIpZPoolu(pool);

		Integer srcPortNew;
		try {
			srcPortNew = freePorts.iterator().next();
		} catch (NoSuchElementException e) {
			Logger.log(this, Logger.WARNING, LoggingCategory.NetworkAddressTranslation, "Dosla cisla volnych portu pro zanatovani! Vracim zpatky neprelozeny paket.", packet);
			return packet;
		}
		freePorts.remove(srcPortNew);

		InnerRecord newDynamic = new InnerRecord(srcIpNew, srcPortNew, tempRecord.protocol);
		Record r = new Record(tempRecord, newDynamic, false);

		Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, "Novy zaznam vytvoren: Pridavam do tabulky.", r);
		table.add(r);

		return getTranslatedPacket(packet, srcIpNew, srcPortNew);
	}

	/**
	 * Vrati kopii IpPacketu s novou zdrojovou adresou a novym portem
	 *
	 * @param packet old packet
	 * @param srcIpNew source IP of new packet
	 * @param srcPortNew source port of new packet
	 * @return
	 */
	private IpPacket getTranslatedPacket(IpPacket packet, IpAddress srcIpNew, int srcPortNew) {
		L4Packet data = packet.data.getCopyWithDifferentSrcPort(srcPortNew);

//		Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format("Zanatuji: before: src: %s:%d dst: %s:%d",
//						packet.src.toString(), packet.data.getPortSrc(), packet.dst.toString(), packet.data.getPortDst()), packet);

		logNatOperation(packet, true, true);

		IpPacket translated = new IpPacket(srcIpNew, packet.dst, packet.ttl, data);

		logNatOperation(packet, true, false);

//		Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format("Zanatuji:  after: src: %s:%d dst: %s:%d",
//						translated.src.toString(), translated.data.getPortSrc(), translated.dst.toString(), translated.data.getPortDst()), packet);

		return translated;
	}

	private InnerRecord generateInnerRecordForSrc(IpPacket packet) {
		L4PacketType type;
		int port;

		if (packet.data != null) {
			type = packet.data.getType();
			port = packet.data.getPortSrc();
		} else {
			Logger.log(this, Logger.WARNING, LoggingCategory.NetworkAddressTranslation, "generateInnerRecordForSrc: Neprijimam paket s L4 data == null, vracim null!", packet);
			return null;
		}

		return new InnerRecord(packet.src, port, type);
	}

	//--------------------------------------------- backward translation ---------------------------------------------

	/**
	 * Executes backward translation of packet.
	 * @param packet to translate
	 * @param in incomming interface
	 * @return
	 */
	public IpPacket backwardTranlate(IpPacket packet, NetworkInterface in) {
		if (outside == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Nenatuji: outside je null.", packet);
			return packet;
		}
		if (packet.data == null) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Nenatuji: Packet nema L4 data.", packet);
			return packet;
		}
		if (outside.name.equals(in.name)) {
			return doBackwardTranslation(packet);
		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Nenatuji: prichozi rozhrani je: "+in.name+", outside ale je: "+outside.name, packet);
		return packet;
	}

	private IpPacket doBackwardTranslation(IpPacket packet) {
		// TODO: zde asi smazat stare zaznamy, abych podle nich neodnatovaval, kdyz nemam..
		deleteOldDynamicRecords();

		// 1) projit staticka pravidla, pokud tam bude sedet packet.dst s record.out.address, tak se vytvori novy a vrati se
		for (Record record : table) {
			if (record.isStatic && record.out.address.equals(packet.dst)) {
				logNatOperation(packet, false, true);

//				Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format("Odnatuji: before: src: %s:%d dst: %s:%d",
//						packet.src.toString(), packet.data.getPortSrc(), packet.dst.toString(), packet.data.getPortDst()), packet);

				IpPacket translated = new IpPacket(packet.src, record.in.address, packet.ttl, packet.data); // port se tu nemeni (je v packet.data)

				logNatOperation(translated, false, false);

//				Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format("Odnatuji:  after: src: %s:%d dst: %s:%d",
//						translated.src.toString(), translated.data.getPortSrc(), translated.dst.toString(), translated.data.getPortDst()), packet);
				return translated;
			}
		}

		// 2) projit dynamicka pravidla, tam musi sedet IP+port // TODO: NatTable: pridat protokol
		for (Record record : table) {
			if (!record.isStatic && record.out.address.equals(packet.dst) && record.out.port == packet.data.getPortDst()) {

				logNatOperation(packet, false, true);

//				Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format("Odnatuji: before: src: %s:%d dst: %s:%d",
//						packet.src.toString(), packet.data.getPortSrc(), packet.dst.toString(), packet.data.getPortDst()), packet);

				L4Packet dataNew = packet.data.getCopyWithDifferentDstPort(record.in.port); // zmena portu zde
				IpPacket translated = new IpPacket(packet.src, record.in.address, packet.ttl, dataNew);

//				Logger.log(this, Logger.INFO, LoggingCategory.NetworkAddressTranslation, String.format("Odnatuji:  after: src: %s:%d dst: %s:%d",
//						translated.src.toString(), translated.data.getPortSrc(), translated.dst.toString(), translated.data.getPortDst()), packet);
				logNatOperation(packet, false, false);

				return translated;
			}
		}

		Logger.log(this, Logger.DEBUG, LoggingCategory.NetworkAddressTranslation, "Neodnatuji: nepodarilo se najit zaznam pro odnatovani.", packet);
		return packet;
	}

	//--------------------------------------------- cisco stuff ---------------------------------------------

	/**
     * Vrati pozici pro pridani do tabulky.
     * Radi se to dle out adresy vzestupne.
     * @param out
     * @return index noveho zaznamu
     */
    private int dejIndexVTabulce(IpAddress out) {
        int index = 0;
        for (Record zaznam : table) {
            if (out.getLongRepresentation() < zaznam.out.address.getLongRepresentation()) {
                break;
            }
            index++;
        }
        return index;
    }

//    /**
//     * True, pokud je uz tam je zdrojova address v tabulce.
//     * @param in adresa, kterou chceme porovnavat
//     * @param isStatic udava, jestli se ma hledat jen mezi statickymi (true) nebo dynamickymi (false)
//     * @return
//     */
//    private boolean jeTamZdrojova(IpAddress in, boolean isStatic) {
//        for (Record record : table) {
//            if (record.isStatic == isStatic && record.in.jeStejnaAdresaSPortem(in)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * True, pokud je uz tam je prelozena address v tabulce.
//     * @param out adresa, kterou chceme porovnavat
//     * @param isStatic udava, jestli se ma hledat jen mezi statickymi (true) nebo dynamickymi (false)
//     * @return
//     */
//    private boolean jeTamPrelozena(IpAddress out, boolean staticke) {
//        for (Record zaznam : table) {
//            if (zaznam.isStatic == staticke && zaznam.out.jeStejnaAdresaSPortem(out)) {
//                return true;
//            }
//        }
//        return false;
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
//            if (zaznam.isStatic && zaznam.out.jeStejnaAdresa(zdroj)) {
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
//        IpAddress address = odnatujZdrojovouIpAdresu(adr);
//        if (address == null) {
//            return false;
//        }
//        return true;
//    }

//

//    /**
//     * Vrati true, pokud je mozne danou adresu prelozit. Jinak vrati false.
//     * @param adr
//     * @return true - musi byt adr v access-listu, k nemu priraznem pool s alespon 1 volnou IP adresou. <br />
//     */
//    public boolean lzePrelozit(IpAddress adr) {
//
//        AccessList access = lAccess.vratAccessListIP(adr);
//        if (access == null) {
//            return false;
//        }
//
//        Pool pool = lPool.vratPoolZAccessListu(access);
//        if (pool == null) {
//            return false;
//        }
//
//        IpAddress nova = pool.dejIp(true);
//        if (nova == null) {
//            return false;
//        }
//
//        return true;
//    }

    /**
     * Smaze stare (starsi nez natRecordLife [s]) dynamicke zaznamy v tabulce.
     */
    public void deleteOldDynamicRecords() {
        long now = System.currentTimeMillis();
        List<Record> delete = new ArrayList<>();
        for (Record record : table) {
            if (record.isStatic == false) { // jen dynamicke zaznamy
                if (now - record.getTimestamp() > natRecordLife) {
					freePorts.add(record.out.port);
                    delete.add(record);
                }
            }
        }

		table.removeAll(delete);
    }

    /****************************************** functions for static rules ******************************************************/

	    /**
     * Prida isStatic pravidlo do tabulky.
     * Razeno vzestupne dle out adresy.
     * @param in zdrojova IP urcena pro preklad
     * @param out nova (prelozena) adresa
     * @return 0 - ok, zaznam uspesne pridan <br />
     *         1 - chyba, in adresa tam uz je (% in already mapped (in -> out)) <br />
     *         2 - chyba, out adresa tam uz je (% similar static entry (in -> out) already exists)
     */
    public int addStaticRuleForCisco(IpAddress in, IpAddress out) {

		for (Record zaznam : table) {
			if (zaznam.isStatic) {
				if (zaznam.in.address.equals(in)) {
					return 1;
				}
				if (zaznam.out.address.equals(out)) {
					return 2;
				}
			}
        }

        int index = dejIndexVTabulce(out);
		InnerRecord innerIn = new InnerRecord(in, 0, L4PacketType.ICMP); // port or L4type is irrelevant
		InnerRecord innerOut = new InnerRecord(out, 0, L4PacketType.ICMP); // port or L4type is irrelevant
        table.add(index, new Record(innerIn, innerOut, true));

        return 0;
    }

	 /**
     * Smaze vsechny isStatic zaznamy, ktere maji odpovidajici in a out.
     * Dale aktualizuje outside rozhrani co se IP tyce. Nejdrive smaze vsechny krom prvni,
     * a pak postupne prida ze statickych a pak i z poolu.
     * @return 0 - alespon 1 zaznam se smazal <br />
     *         1 - nic se nesmazalo, pac nebyl nalezen odpovidajici zaznam (% Translation not found)
     */
    public int deleteStaticRule(IpAddress in, IpAddress out) {

        List<Record> smaznout = new ArrayList<>();
        for (Record zaznam : table) {
            if (zaznam.isStatic && in.equals(zaznam.in.address) && out.equals(zaznam.out.address)) {
                smaznout.add(zaznam);
            }
        }

        if (smaznout.isEmpty()) {
            return 1;
        }

		table.removeAll(smaznout);

        return 0;
    }

    /****************************************** nastavovani rozhrani ***************************************************/
    /**
     * Prida inside rozhrani. <br />
     * Neprida se pokud uz tam je rozhrani se stejnym jmenem. <br />
     * Pro pouziti prikazu 'address nat inside'.
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

    /****************************************** debug stuff *********************************************************/

    /**
     * Pomocny servisni vypis.
     * Nejdriv se smazou stare dynamcike zaznamy.
     * @return
     */
    public String vypisZaznamyDynamicky() {
        deleteOldDynamicRecords();
        String s = "";

        for (Record zaznam : table) {
            if (!zaznam.isStatic) {
                s += zaznam.in.getAddressWithPort() + "\t" + zaznam.out.getAddressWithPort() + "\n";
            }
        }
        return s;
    }

    //--------------------------------------------- linux stuff ---------------------------------------------
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
        lPool.pridejPool(verejne.getIpAddress().getIp(), verejne.getIpAddress().getIp(), 24, pool);

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
     * Prida staticke pravidlo do NetworkAddressTranslation tabulky. Nic se nekontroluje.
     * @param in zdrojova IP
     * @param out nova zdrojova (prelozena)
     */
    public void pridejStatickePravidloLinux(IpAddress in, IpAddress out) {
		Record r = new Record(new InnerRecord(in, 0, L4PacketType.ICMP), new InnerRecord(out, 0, L4PacketType.ICMP), true);
		table.add(r);
    }

	//--------------------------------------------- classes ---------------------------------------------

	private enum Operation {
		NAT,
		DENAT,
	}

	/**
     * Reprezentuje jeden radek v NetworkAddressTranslation tabulce.
     */
    public class Record {

        /**
         * Zdrojova address.
         */
        public final InnerRecord in;
        /**
         * Zdrojova prelozena address.
         */
        public final InnerRecord out;
        /**
         * Potreba pro vypis v ciscu. Je null, kdyz se vkladaji staticka pravidla.
         */
        public final IpAddress target;
        /**
         * Vlozeno staticky - true, dynamicky - false.
         */
        public final boolean isStatic;
        /**
         * Cas vlozeni v ms (pocet ms od January 1, 1970)
         */
        private long timestamp;

        public Record(InnerRecord in, InnerRecord out, boolean staticke) {
            this.in = in;
            this.out = out;
            this.target = null;
            this.isStatic = staticke;
            this.timestamp = System.currentTimeMillis();
        }

        public Record(InnerRecord in, InnerRecord out, IpAddress cil, boolean staticke) {
            this.in = in;
            this.out = out;
            this.target = cil;
            this.isStatic = staticke;
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Obnovi zaznam na dalsich 10s.
         */
        public void touch() {
            this.timestamp = System.currentTimeMillis();
        }

		@Override
		public String toString() {
			return in.address.toString()+":"+in.port+" "+in.protocol+" => "+out.address.toString()+":"+out.port+" "+out.protocol + (isStatic ? " (static)" : "");
		}
    }

	public class InnerRecord {
		public final IpAddress address;
		public final int port;
		public final L4PacketType protocol;

		public InnerRecord(IpAddress ip, int port, L4PacketType protocol) {
			this.address = ip;
			this.port = port;
			this.protocol = protocol;
		}

		public String getAddressWithPort() {
			return address + ":" + port;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final InnerRecord other = (InnerRecord) obj;
			if (!Objects.equals(this.address, other.address)) {
				return false;
			}
			if (this.port != other.port) {
				return false;
			}
			if (this.protocol != other.protocol) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 53 * hash + Objects.hashCode(this.address);
			hash = 53 * hash + this.port;
			hash = 53 * hash + (this.protocol != null ? this.protocol.hashCode() : 0);
			return hash;
		}
	}
}
