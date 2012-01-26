/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author neiss
 */
public class MacAddressTest {
    
    public MacAddressTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    
     @Test
     public void testKonstruktor() {
         MacAddress mac;
         String sm;
         
         sm="1a:4c:05:49:ad:f9";
         mac = new MacAddress(sm);
         assertEquals(sm, mac.toString());
         
         sm="1A:4C:05:49:AD:F9";
         mac = new MacAddress(sm);
         assertEquals(sm, mac.toString().toUpperCase());
         
         sm="1a:4c:05:49:ad:fr";
         try {
            mac = new MacAddress(sm);
            fail();
         } catch ( MacAddress.BadMacException ex){}

     }
     
     @Test
     public void testBroadcast() {
         MacAddress mac;
         String sm;
         
         sm="ff:ff:ff:ff:ff:ff";
         mac = new MacAddress(sm);
         assertTrue(MacAddress.isBroadcast(mac));
         
         sm="1a:4c:05:49:ad:f9";
         mac = new MacAddress(sm);
         assertFalse(MacAddress.isBroadcast(mac));
     }
}
