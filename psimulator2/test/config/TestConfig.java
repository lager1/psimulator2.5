/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package config;


import java.io.File;
import java.io.IOException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class TestConfig {
    
    
    /**
     * metoda pro vytvoření modelu ukázkové sítě
     *
     * @return
     */
//    public static Network createSampleNework() {
//
//        return nt;
//    }
    
    

    public static String configFileName = "network.xml";
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    public File configFile;

    @Test
    public void testConfig() throws IOException {

        configFile = testFolder.newFile(configFileName);

        storeConfig();
        loadConfig();
    }

    /**
     * test uložení konfiguračního souboru
     */
    public void storeConfig() {
        System.out.println("1");

    }

    /**
     * test načtení nově vytvořeného konfiguračního souboru
     */
    public void loadConfig() {

        System.out.println("2");
    }
}
