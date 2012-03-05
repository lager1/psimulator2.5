package networkModule.L3;

import java.util.HashMap;
import java.util.Map;
import networkModule.L3.NatPool.Pool;

/**
 * Datova struktura pro seznam PoolAccess. Jednoznacny identifikator je cislo.
 * (cisco prikaz: "ip nat inside source list 'cisloAccessListu' poolName 'jmenoPoolu' overload?" )
 *
 * OK
 *
 * @author Stanislav Řehák
 */
public class NatPoolAccess {

	/**
	 * Key - number of poolAccess <br />
	 * Value - poolAccess itself
	 */
    private final Map<Integer, PoolAccess> poolAccess = new HashMap<>();

	public Map<Integer, PoolAccess> getPoolAccess() {
		return poolAccess;
	}

    /**
     * Prida novy poolAccess na spravnou pozici. Kdyz najde PoolAccess se stejnym jmenem,
     * tak ho bez milosti premazne.
     * @param access
     * @param poolName
     */
    public void pridejPoolAccess(int access, String pool, boolean overload) {
        smazPoolAccess(access);
        PoolAccess novy = new PoolAccess(access, pool, overload);
//        int index = 0;
//        for (PoolAccess pa : seznam) {
//            if (novy.access < pa.access) {
//                break;
//            }
//            index++;
//        }
        poolAccess.put(access, novy);
    }

    /**
     * Zkusi smazat PoolAccess.
     * @param access, identifikator PoolAccessu.
     * @return 0 - ok, smazalo to. <br />
     *         1 - takovy zaznam neni (%Dynamic mapping not found)
     */
    public int smazPoolAccess(int access) {

        Object object = poolAccess.remove(access);
		if (object == null) {
			return 1;
		}

        return 0;
    }

    /**
     * Smaze vsechny PoolAccessy.
     */
    public void smazPoolAccessVsechny() {
        poolAccess.clear();
    }

    /**
     * Vrati prirazeny poolAccess nebo null, kdyz nic nenajde.
     * @param poolName
     * @return
     */
    public PoolAccess vratPoolAccess(Pool pool) {
		for (PoolAccess pa : poolAccess.values()) {
            if (pa.poolName.equals(pool.name)) {
                return pa;
            }
        }
        return null;
    }

    public class PoolAccess {

        /**
         * Cislo 1-2699.
         */
        public final int access;
        /**
         * Unikatni name poolu.
         */
        public final String poolName;
        public final boolean overload;

        public PoolAccess(int access, String pool, boolean overload) {
            this.access = access;
            this.poolName = pool;
            this.overload = overload;
        }
    }
}
