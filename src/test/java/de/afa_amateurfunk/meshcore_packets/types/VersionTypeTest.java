package de.afa_amateurfunk.meshcore_packets.types;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for VersionType
 *
 * @see VersionType
 */
class VersionTypeTest extends AbstractLoggingTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(VersionTypeTest.class);

    /**
     * Test that the data repository is correctly represented
     */
    @Test
    void testDataMapping() {
        // Test that we have exactly one member
        assertEquals(1, VersionType.values().length);
        // REQUEST: bitmask 0x00
        assertEquals(0x00, VersionType.VER_1.getBitmask());
        assertEquals("PAYLOAD_VER_1", VersionType.VER_1.getSpecName());
        assertEquals("PAYLOAD_VER_1", VersionType.VER_1.toString());
    }

    /**
     * Test the actual parser with all possible valid cases
     * <p>
     * Unfortunately, we have to brute-force all possible 256 combinations of version, routing and payload to make sure
     * the bitfield parser always operates correctly.
     * </p>
     *
     * @see VersionTypeTest#testHeaderParseRejectReserved
     */
    @Test
    void testHeaderParseValidCases() {
        int[] versionTypes = {0x00};
        for (int version : versionTypes) {
            byte versionByte = (byte) (0x00 | ((byte) version << 6));
            LOG.trace(String.format("New version row. Initial header byte %02x / %s", versionByte, StringUtils.leftPad(Integer.toBinaryString(versionByte & 0xFF), 8, '0')));
            for (int routing = 0; routing < 4; routing++) {
                byte routingBitmask = (byte) (((byte) routing << 0) & 0xFF);
                LOG.trace(String.format("New routing row. Row bitmask %02x / %s", routingBitmask, StringUtils.leftPad(Integer.toBinaryString(routingBitmask & 0xFF), 8, '0')));
                byte routingByte = (byte) (versionByte | routingBitmask);
                //LOG.trace(String.format("       Resulting header byte %02x / %s", routingByte, StringUtils.leftPad(Integer.toBinaryString(routingByte & 0xFF), 8, '0')));
                for (int payloadType = 0; payloadType < 16; payloadType++) {
                    byte payloadBitmask = (byte) (((byte) payloadType << 2) & 0xFF);
                    LOG.trace(String.format("New payload row. Row bitmask %02x / %s %02x", payloadBitmask, StringUtils.leftPad(Integer.toBinaryString(payloadBitmask & 0xFF), 8, '0'), payloadType));
                    byte payloadByte = (byte) (routingByte | payloadBitmask);
                    LOG.trace(String.format("       Resulting header byte %02x / %s", payloadByte, StringUtils.leftPad(Integer.toBinaryString(payloadByte & 0xFF), 8, '0')));
                    VersionType result = assertDoesNotThrow(() -> VersionType.fromHeader(payloadByte));
                    assertEquals(version, result.getBitmask());
                }
            }
        }
    }

    /**
     * Test that the parser rejects invalid cases
     *
     * <p>0x0C-0x0E are currently marked as reserved</p>
     */
    @Test
    void testHeaderParseRejectReserved() {
        int[] versionTypes = {0x01, 0x02, 0x03};
        for (int version : versionTypes) {
            byte versionByte = (byte) (0x00 | ((byte) version << 6));
            LOG.trace(String.format("New version row. Initial header byte %02x / %s", versionByte, StringUtils.leftPad(Integer.toBinaryString(versionByte & 0xFF), 8, '0')));
            for (int routing = 0; routing < 4; routing++) {
                byte routingBitmask = (byte) (((byte) routing << 0) & 0xFF);
                LOG.trace(String.format("New routing row. Row bitmask %02x / %s", routingBitmask, StringUtils.leftPad(Integer.toBinaryString(routingBitmask & 0xFF), 8, '0')));
                byte routingByte = (byte) (versionByte | routingBitmask);
                LOG.trace(String.format("       Resulting header byte %02x / %s", routingByte, StringUtils.leftPad(Integer.toBinaryString(routingByte & 0xFF), 8, '0')));
                for (int payloadType = 0; payloadType < 16; payloadType++) {
                    byte payloadBitmask = (byte) (((byte) payloadType << 2) & 0xFF);
                    LOG.trace(String.format("New payload row. Row bitmask %02x / %s %02x", payloadBitmask, StringUtils.leftPad(Integer.toBinaryString(payloadBitmask & 0xFF), 8, '0'), payloadType));
                    byte payloadByte = (byte) (routingByte | payloadBitmask);
                    LOG.trace(String.format("       Resulting header byte %02x / %s", payloadByte, StringUtils.leftPad(Integer.toBinaryString(payloadByte & 0xFF), 8, '0')));
                    assertThrows(NoSuchElementException.class, () -> VersionType.fromHeader(payloadByte));
                }
            }
        }
    }
}
