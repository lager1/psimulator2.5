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
 * @author Martin Lukáš
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


    /**
     * tady si můžete specifikovat jak chcete aby vypadal výstup hlášky
     * @param message
     * @param type
     * @param source
     */
     public static void individualLog(String message, TYPE type, SOURCE source) {

         // obyčejná podoba hlášky -- typ, zdroj, zpráva
       //  System.out.println("!!" + type.toString() + ":" + source.toString() + message);


         // rozšířená hláška včetně čísla řádku a jména třídy odkud zpráva pochází
         Exception ex = new Exception();
         int lineNumber = ex.getStackTrace()[2].getLineNumber();
         String className = ex.getStackTrace()[2].getClassName();

         System.out.println(type.toString() + ":" + source.toString() + " " + className+":" +lineNumber+ "|| "+ message);

     }


}
