package de.afa_amateurfunk.meshcore_packets.types;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for ControlPacketType
 *
 * @see ControlPacketType
 */
public class ControlPacketTypeTest extends AbstractLoggingTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ControlPacketTypeTest.class);

    /**
     * Test that the data repository is correctly represented
     */
    @Test
    void testDataMapping() {
        // Test that we have exactly two members
        assertEquals(2, ControlPacketType.values().length);
        // DISCOVER_REQUEST: bitmask 0x80
        assertEquals(0x80, ControlPacketType.DISCOVER_REQUEST.getBitmask());
        assertEquals("CTL_TYPE_NODE_DISCOVER_REQ", ControlPacketType.DISCOVER_REQUEST.getSpecName());
        assertEquals("CTL_TYPE_NODE_DISCOVER_REQ", ControlPacketType.DISCOVER_REQUEST.toString());
        // DISCOVER_REQUEST: bitmask 0x90
        assertEquals(0x90, ControlPacketType.DISCOVER_RESPONSE.getBitmask());
        assertEquals("CTL_TYPE_NODE_DISCOVER_RESP", ControlPacketType.DISCOVER_RESPONSE.getSpecName());
        assertEquals("CTL_TYPE_NODE_DISCOVER_RESP", ControlPacketType.DISCOVER_RESPONSE.toString());
    }

    /**
     * Test the actual parser with all possible valid cases
     * <p>
     * Unfortunately, we have to brute-force all possible 16 combinations of flags to each valid type to ensure
     * the bitfield parser always operates correctly.
     * </p>
     */
    @Test
    void testHeaderParseValidCases() {
        byte[] validBitmasks = new byte[]{(byte) 0x80, (byte) 0x90};
        for (byte bitmask : validBitmasks) {
            for (int flags = 0; flags < 16; flags++) {
                byte headerByte = (byte) (0x00 | bitmask);
                byte finalByte = (byte) (headerByte | flags);
                ControlPacketType result = ControlPacketType.fromHeader(finalByte);
                assertEquals(bitmask, (byte) result.getBitmask());
            }
        }
    }

    /**
     * Test the actual parser with all possible invalid cases
     * <p>
     * Unfortunately, we have to brute-force all possible 16 combinations of flags to each valid type to ensure
     * the bitfield parser always operates correctly.
     * </p>
     */
    @Test
    void testRejectInvalidCases() {
        byte[] invalidBitmasks = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0x40, (byte) 0x50, (byte) 0x60, (byte) 0x70, (byte) 0xA0, (byte) 0xB0, (byte) 0xC0, (byte) 0xD0, (byte) 0xE0, (byte) 0xF0};
        for (byte bitmask : invalidBitmasks) {
            for (int flags = 0; flags < 16; flags++) {
                byte headerByte = (byte) (0x00 | bitmask);
                byte finalByte = (byte) (headerByte | flags);
                assertThrows(NoSuchElementException.class, () -> ControlPacketType.fromHeader(finalByte));
            }
        }
    }


}
