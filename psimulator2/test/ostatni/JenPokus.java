package ostatni;

/*
 * Erstellt am 5.3.2012.
 */

import device.Device;
import networkModule.NetMod;
import networkModule.SimpleSwitchNetMod;
import networkModule.TcpIpNetMod;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author neiss
 */
public class JenPokus {

	public JenPokus() {
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
	public void testIsAssignableFrom(){
		SimpleSwitchNetMod ss = new SimpleSwitchNetMod(new Device(1, "name1", Device.DeviceType.cisco_router));
		TcpIpNetMod ip = new TcpIpNetMod(new Device(1, "name2", Device.DeviceType.cisco_router));

//		System.out.println(ss.getClass().isAssignableFrom(TcpIpNetMod.class));	//true
//		System.out.println(ip.getClass().isAssignableFrom(ss.getClass()));	// false
//		System.out.println(ss.getClass().isAssignableFrom(NetMod.class));	// false
//		System.out.println(NetMod.class.isAssignableFrom(ip.getClass()));	// true
//		System.out.println(NetMod.class.isAssignableFrom(ss.getClass()));	// true

		System.out.println("Tedka skutecny pouziti:");
		assertFalse(ss.isStandardTcpIpNetMod());
		assertTrue(ip.isStandardTcpIpNetMod());


	}
}
