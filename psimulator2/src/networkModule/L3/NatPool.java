/*
 * created 5.3.2012
 */

package networkModule.L3;

import dataStructures.ipAddresses.IPwithNetmask;
import dataStructures.ipAddresses.IpAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import networkModule.L3.NatAccessList.AccessList;
import networkModule.L3.NatPoolAccess.PoolAccess;
import networkModule.L3.NatTable.Record;

/**
 * Datova struktura pro pools poolu IP adres. <br />
 * Kazdy poolName obsahuje name a pools IpAdres. <br />
 * (cisco prikaz: "ip nat poolName 'jmenoPoolu' 'ip_start' 'ip_konec' prefix 'cislo'" )
 *
 * OK
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public class NatPool {

	/**
	 * Key - name of pool <br />
	 * Value - pool itself
	 */
    private final Map<String, Pool> pools = new HashMap<>();

    private final NatTable natTable;


    public NatPool(NatTable tab) {
        this.natTable = tab;
    }

    /**
     * Prida poolName.
     * @param start staci predavat jen IpAddress a maska muze byt null
     * @param konec staci predavat jen IpAddress a maska muze byt null
     * @param prefix maska pocet bitu
     * @param name neni null ani ""
     * @return 0 - ok prida poolName <br />
     *         1 - kdyz je prvni IP vetsi nez druha IP (%End address less than start address) <br />
     *         2 - poolName s timto jmenem je prave pouzivan, tak nic. (%Pool ovrld in use, cannot redefine) <br />
     *         3 - kdyz je spatna maska (% Invalid input detected) <br />
     *         4 - kdyz je start a konec v jine siti (%Start and end addresses on different subnets)
     */
    public int pridejPool(IPwithNetmask start, IPwithNetmask konec, int prefix, String jmeno) {
        if (start.getIp().getLongRepresentation() > konec.getIp().getLongRepresentation()) {
            return 1;
        }

        if (prefix > 32 || prefix < 1) {
            return 3;
        }

		start = new IPwithNetmask(start.getIp(), prefix);

		if (!start.isInMyNetwork(konec.getIp())) {
			return 4;
		}

        // smaznout stejne se jmenujici poolName + kontrola jestli vubec ho muzem prespat
        if (smazPool(jmeno) == 2) {
            return 2;
        }

        // tady pridej poolName
        Pool pool = new Pool(jmeno, prefix);

        // pridavam IP adresy do poolu.
        IPwithNetmask ukazatel = start;
        do {
            pool.pool.add(ukazatel.getIp());
			ukazatel = new IPwithNetmask(IpAddress.nextAddress(ukazatel.getIp()), prefix);

        } while (ukazatel.getIp().getLongRepresentation() <= konec.getIp().getLongRepresentation() && start.isInMyNetwork(ukazatel.getIp()));

        pool.pointer = pool.prvni();

        pools.put(pool.name, pool);
        return 0;
    }

    /**
     * Smaze poolName podle jmena.
     * Dale maze stare dynamicke zaznamy.
     * @param name
     * @return 0 - ok smazal se takovy poolName <br />
     *         1 - poolName s takovym jmenem neni. (%Pool name not found) <br />
     *         2 - %Pool ovrld in use, cannot redefine
     */
    public int smazPool(String name) {

        natTable.deleteOldDynamicRecords();
        if(isPoolInUse(name)) {
            return 2;
        }

		Object value = pools.remove(name);
		if (value == null) {
			return 1;
		}

        return 0;
    }

    /**
     * Smaze vsechny pooly.
     */
    public void smazPoolVsechny() {
        pools.clear();
//        updateIpNaRozhrani();
    }

    /**
     * Vrati pro overload prvni IP z poolu, jinak dalsi volnou IP. Pri testovani vrati null,
     * kdyz uz neni volna IP v poolu. To je ale osetreno metodou mamNAtovat(), tak uz pri
     * samotnem natovani by to null nikdy vracet nemelo.
     * @return
     */
    public IpAddress dejIpZPoolu(Pool pool) { // TODO: dejIpZPoolu bacha, predelat!!
        if (vratPoolAccess(pool).overload) {
            return pool.prvni();
        } else {
            return pool.dejIp(false);
        }
    }

    /**
     * Vrati vsechny pooly, ktere obsahuji danou adresu.
     * @param address
     * @return pools poolu - kdyz to najde alespon 1 poolName <br />
     *         null - kdyz to zadny poolName nenajde
     */
    public List<Pool> vratPoolProIp(IpAddress address) {
        List<Pool> poolsWithIP = new ArrayList<>();
        for (Pool pool : pools.values()) {
            if (pool.prvni() == null) continue;
			IPwithNetmask poolRange = new IPwithNetmask(pool.prvni(), pool.prefix);
			if (poolRange.isInMyNetwork(address)) {
				poolsWithIP.add(pool);
			}
        }
        if (poolsWithIP.isEmpty()) return null;
        return poolsWithIP;
    }

    /**
     * Vrati prirazeny poolAccess nebo null, kdyz nic nenajde.
     * @param poolName
     * @return
     */
    public PoolAccess vratPoolAccess(Pool pool) {
        for (PoolAccess pa : natTable.lPoolAccess.getPoolAccess().values()) {
            if (pa.poolName.equals(pool.name)) {
                return pa;
            }
        }
        return null;
    }

	/**
	 * Vrati prirazeny poolAccess nebo null, kdyz nic nenajde.
	 * @param acc
	 * @return
	 */
    public PoolAccess vratPoolAccessZAccessListu(AccessList acc) {
		return natTable.lPoolAccess.getPoolAccess().get(acc.cislo);
//
//        for (PoolAccess pa : natTable.lPoolAccess.getPoolAccess().values()) {
//            if (acc.cislo == pa.access) {
//                return pa;
//            }
//        }
//        return null;
    }

    /**
     * Vrati poolName, ktery je navazan na access-pools.
     * @param access
     * @return poolName - ktery je navazan na access-pools <br />
     *         null - kdyz neni PoolAccess s timto cislem a nebo neni Pool s nazvem u nalezeneho PoolAccessu.
     */
    public Pool vratPoolZAccessListu(AccessList access) {
		PoolAccess pac = natTable.lPoolAccess.getPoolAccess().get(access.cislo);
		if (pac == null) return null;

		return pools.get(pac.poolName);
//        for (PoolAccess pa : natTable.lPoolAccess.getPoolAccess().values()) {
//            if (pa.access == access.cislo) {
//                for (Pool pool : pools) {
//                    if (pool.name.equals(pa.poolName)) {
//                        return pool;
//                    }
//                }
//            }
//        }
//        return null;
    }


    /**
     * Vrati true, pokud se najde nejaky zaznam od toho poolu.
     * @param name
     * @return
     */
    private boolean isPoolInUse(String jmeno) {
        for (Record zaznam : natTable.table) {
            if (zaznam.staticRule == false) {
                List<Pool> pseznam = vratPoolProIp(zaznam.out);
                if (pseznam == null) continue;
                for (Pool pool : pseznam) {
                    if (pool.name.equals(jmeno)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public class Pool {

        /**
         * Jmeno poolu
         */
        public final String name;
        /**
         * Prirazeny poolName adres.
         */
        List<IpAddress> pool;
        /**
         * Ukazuje na dalsi volnou IpAdresu z poolu nebo null, kdyz uz neni volna.
         */
        IpAddress pointer = null;
		public final int prefix;

        public Pool(String name, int prefix) {
			this.name = name;
			this.prefix = prefix;
            pool = new ArrayList<IpAddress>(){

                @Override
                public boolean add(IpAddress ip) {
                    if (pool.isEmpty()) {
                        pointer = ip;
                    }
                    return super.add(ip);
                }

            };
        }

        /**
         * Vrati prvni IpAdresu z poolu nebo null, kdyz je poolName prazdny.
         * @return
         */
        public IpAddress prvni() {
            if (pool.isEmpty()) {
                return null;
            }
            return pool.get(0);
        }

        /**
         * Vrati dalsi IpAdresu z poolu. Kdyz uz jsem na posledni, tak vracim null (DHU).
         * @return
         */
        private IpAddress dalsi() {
            int n = -1;
            for (IpAddress ip : pool) {
                n++;
                if (ip.equals(pointer)) {
                    break; // n = index ukazatele
                }
            }
            if (n + 1 == pool.size()) {
                return null;
            }
            return pool.get(n + 1);
        }

        /**
         * Vrati dalsi IP z poolu nebo null, pokud uz neni dalsi IP.
         * @param testovani true, kdyz se zjistuje, zda je jeste IP, nemenim pak pointer na volnou IP
         * @return
         */
        public IpAddress dejIp(boolean testovani) {
            IpAddress vrat = pointer;
            if (testovani == false) {
                pointer = dalsi();
            }
            return vrat;
        }

        /**
         * Vrati posledni Ip
         * @return
         */
        public IpAddress posledni() {
            if (pool.size() <= 1) {
                return prvni();
            }
            return pool.get(pool.size()-1);
        }
    }
}

