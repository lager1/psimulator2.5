/*
 * Erstellt am 29.11.2011.
 */

package dataStructures.ipAdresses;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author neiss
 */
public class IpAdressesTest {

    public IpAdressesTest() {
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

    @Test
    public void testIP() {

        IpAdress ip;
        String adr;

        adr = "147.32.125.138";
        ip = new IpAdress(adr);
        assertEquals(ip.toString(), adr);

        adr = "0.0.0.0";
        ip = new IpAdress(adr);
        assertEquals(ip.toString(), adr);

        adr = "1.1.1.1";
        ip = new IpAdress(adr);
        assertEquals(ip.toString(), adr);

        adr = "192.168.1.0";
        ip = new IpAdress(adr);
        assertEquals(ip.toString(), adr);
    }

	@Test
    public void testNetmask(){
        IpNetmask maska;
		
        maska=new IpNetmask(24);
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"255.255.255.0");

        maska=new IpNetmask(25);
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"255.255.255.128");

        maska=new IpNetmask(23);
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"255.255.254.0");

        maska=new IpNetmask(0);
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"0.0.0.0");

        maska=new IpNetmask(32);
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"255.255.255.255");

        maska=new IpNetmask(7);
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"254.0.0.0");

        System.out.println("------------------------------------------");

        maska=new IpNetmask("255.255.255.0");
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"255.255.255.0");

        maska=new IpNetmask("255.255.255.128");
        System.out.println(maska.toString());
        assertEquals(maska.toString(),"255.255.255.128");

        try{
            maska=new IpNetmask("43.23.234.43");
            fail();
        } catch (BadNetmaskException ex){}
    }

	@Test
    public void testBroadcast(){
        System.out.println("------------------------------------------");
        IpAdresa adr=new IpAdresa("192.168.1.0",24);
        assertEquals(adr.vypisBroadcast(),"192.168.1.255");

        adr.nastavMasku(0); //vsechno je cislo pocitace -> cislo site je 0.0.0.0/32
        assertEquals(adr.vypisBroadcast(),"255.255.255.255");

        adr.nastavMasku(32); //vsechno je cislo site -> cislo site je 192.168.1.0/32
        assertEquals(adr.vypisBroadcast(),"192.168.1.0");

        adr.nastavMasku(30); //  cislo site je 192.168.1.0/30
        assertEquals(adr.vypisBroadcast(),"192.168.1.3");
    }

}