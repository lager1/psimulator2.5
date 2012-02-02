/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.EnumMap;

/**
 *
 * @author Martin Lukáš
 */
public class TestLogger {

    /**
     * výčet možných zdrojů zpráv. Zdroje můžete libovolně přidávat.
     */
    public static enum SOURCE {

        TELNET,
        ARP,
    }

    /**
     * výčet možných typů zpráv. Typy můžete libovolně přidávat, pravděpodobně to ale nebude třeba.
     */
    public static enum TYPE {

        DEBUG,
        WARNING,
        ERROR
    }

    
    private static EnumMap<SOURCE, Boolean> activeSource = new EnumMap<SOURCE, Boolean>(SOURCE.class);
    private static EnumMap<TYPE, Boolean> activeType = new EnumMap<TYPE, Boolean>(TYPE.class);
    private static boolean configured = false;

   
    public static void configureTestLogger() {

        ConfigureTestLogger.configure(activeSource, activeType);
        configured = true;
    }

    /**
     * metoda pro záznam testovacích, ladicích apod. zpráv.  Není určeno k logování
     * informace pro visualizer
     * @param message
     * @param type
     * @param source
     */
    public static void logMessage(String message, TYPE type, SOURCE source) {

        if (!configured) // pokud není zkonfigurováno
        {
            configureTestLogger();
            configured = true;
        }

        Boolean isTypeActive = activeType.get(type);

        if (isTypeActive == null || !isTypeActive) // pokud není typ aktivní
        {
            return;
        }


        Boolean isSourceActive = activeSource.get(source);

        if (isSourceActive == null || !isSourceActive) // pokud není source aktivní
        {
            return;
        }


        System.out.println("!!" + type.toString() + ":" + source.toString() + message);
    }
}
