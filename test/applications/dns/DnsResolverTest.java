/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applications.dns;

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michal Horacek
 */
public class DnsResolverTest {

    public DnsResolverTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void nullNameTest() {
        boolean res = DnsResolver.isValidDomainName(null);
        assertFalse(res);
    }

    @Test
    public void emptyNameTest() {
        boolean res = DnsResolver.isValidDomainName("");
        assertFalse(res);
    }

    @Test
    public void invalidNameTest() {
        boolean res = DnsResolver.isValidDomainName("com");
        assertFalse(res);

        res = DnsResolver.isValidDomainName("1");
        assertFalse(res);

        res = DnsResolver.isValidDomainName("asd,asd");
        assertFalse(res);

        res = DnsResolver.isValidDomainName("www.example.com..");
        assertFalse(res);

        res = DnsResolver.isValidDomainName("a.b.c.d");
        assertFalse(res);

        res = DnsResolver.isValidDomainName("123.example.com");
        assertFalse(res);
    }

    @Test
    public void validNameTest() {
        boolean res = DnsResolver.isValidDomainName("example.com.");
        assertTrue(res);

        res = DnsResolver.isValidDomainName("www.example.com.");
        assertTrue(res);

        res = DnsResolver.isValidDomainName("a.b.c.d.cz.");
        assertTrue(res);
        
        res = DnsResolver.isValidDomainName("abc.def.test.info.example.com.");
        assertTrue(res);
    }
}
