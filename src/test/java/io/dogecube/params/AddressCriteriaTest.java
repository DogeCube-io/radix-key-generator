package io.dogecube.params;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AddressCriteriaTest {

    @Test
    public void testParsingSuffix() {
        AddressCriteria c = new AddressCriteria("grandma");
        assertEquals("grandma", new String(c.getSuffix()));
        assertNull(c.getPrefix());
        assertEquals(4_294_967_296L, c.getDifficulty());
    }

    @Test
    public void testDifficulty() {
        AddressCriteria c = new AddressCriteria("?randma");
        assertEquals("?randma", new String(c.getSuffix()));
        assertNull(c.getPrefix());
        assertEquals(1_073_741_824L, c.getDifficulty());
    }

    @Test
    public void testParsingPrefix() {
        AddressCriteria c = new AddressCriteria("doge3x*");
        assertEquals("doge3x", new String(c.getPrefix()));
        assertNull(c.getSuffix());
        assertEquals(1_073_741_824L, c.getDifficulty());
    }

    @Test
    public void testParsingPrefix_2() {
        AddressCriteria c = new AddressCriteria("rdx1qspd0ge3x");
        assertEquals("d0ge3x", new String(c.getPrefix()));
        assertNull(c.getSuffix());
        assertEquals(1_073_741_824L, c.getDifficulty());
    }
}
