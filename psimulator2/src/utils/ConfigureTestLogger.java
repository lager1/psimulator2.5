/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.EnumMap;
import utils.TestLogger.SOURCE;
import utils.TestLogger.TYPE;

/**
 *
 * @author zaltair
 */
public class ConfigureTestLogger {

    /**
     * Tato metoda nastaví v mapách, které hlášky se mají vypisovat. Můžete ji libovolně upravovat, ale necommitovat.
     */
    public static void configure(EnumMap<SOURCE, Boolean> activeSource, EnumMap<TYPE, Boolean> activeType) {

        activeSource.put(SOURCE.ARP, Boolean.TRUE);     // pro testování "modulu ARP"
        activeSource.put(SOURCE.TELNET, Boolean.TRUE);   // pro testování modulu telnet


        activeType.put(TYPE.DEBUG, Boolean.TRUE);  // zapne ladící hlášky
        activeType.put(TYPE.WARNING, Boolean.TRUE);  // zapne vyrovné hlášky
        activeType.put(TYPE.ERROR, Boolean.TRUE);  // zapne chybové hlášky


    }
}
