package de.afa_amateurfunk.meshcore_packets.types;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AdvertNodeTypeTest extends AbstractLoggingTest {
    /**
     * Test that the data repository is correctly represented
     */
    @Test
    void testDataMapping() {
        // Test that we have exactly three members
        assertEquals(5, AdvertNodeType.values().length);
        // NONE: index 0
        assertEquals(0, AdvertNodeType.NONE.getIndex());
        assertEquals("ADV_TYPE_NONE", AdvertNodeType.NONE.getSpecName());
        assertEquals("ADV_TYPE_NONE", AdvertNodeType.NONE.toString());
        // COMPANION: index 1
        assertEquals(1, AdvertNodeType.COMPANION.getIndex());
        assertEquals("ADV_TYPE_CHAT", AdvertNodeType.COMPANION.getSpecName());
        assertEquals("ADV_TYPE_CHAT", AdvertNodeType.COMPANION.toString());
        // REPEATER: index 2
        assertEquals(2, AdvertNodeType.REPEATER.getIndex());
        assertEquals("ADV_TYPE_REPEATER", AdvertNodeType.REPEATER.getSpecName());
        assertEquals("ADV_TYPE_REPEATER", AdvertNodeType.REPEATER.toString());
        // ROOMSERVER: index 3
        assertEquals(3, AdvertNodeType.ROOMSERVER.getIndex());
        assertEquals("ADV_TYPE_ROOM", AdvertNodeType.ROOMSERVER.getSpecName());
        assertEquals("ADV_TYPE_ROOM", AdvertNodeType.ROOMSERVER.toString());
        // SENSOR: index 4
        assertEquals(4, AdvertNodeType.SENSOR.getIndex());
        assertEquals("ADV_TYPE_SENSOR", AdvertNodeType.SENSOR.getSpecName());
        assertEquals("ADV_TYPE_SENSOR", AdvertNodeType.SENSOR.toString());

    }

    /**
     * Test the actual parser with all possible valid cases
     */
    @Test
    void testHeaderParseValidCases() {
        for (int i = 0; i < 5; i++) {
            byte headerByte = (byte) (0x00 | ((byte) i));
            AdvertNodeType result = AdvertNodeType.fromHeader(headerByte);
            assertEquals(AdvertNodeType.values()[i], result);
        }
    }

    /**
     * Test that the parser rejects invalid cases (bitmask 0x04 is marked RESERVED upstream)
     */
    @Test
    public void testHeaderParseRejectReserved() {
        for (int i = 5; i < 16; i++) {
            byte headerByte = (byte) (0x00 | ((byte) i));
            assertThrows(java.util.NoSuchElementException.class, () -> AdvertNodeType.fromHeader(headerByte));
        }
    }
}