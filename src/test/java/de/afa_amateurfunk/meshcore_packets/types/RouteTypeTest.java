package de.afa_amateurfunk.meshcore_packets.types;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RouteType
 *
 * @see RouteType
 */
class RouteTypeTest extends AbstractLoggingTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RouteTypeTest.class);

    /**
     * Test that the data repository is correctly represented
     */
    @Test
    void testDataMapping() {
        // Test that we have exactly four members
        assertEquals(4, RouteType.values().length);
        // TRANSPORT_FLOOD: bitmask 0x00
        assertEquals(0x00, RouteType.TRANSPORT_FLOOD.getBitmask());
        assertEquals("ROUTE_TYPE_TRANSPORT_FLOOD", RouteType.TRANSPORT_FLOOD.getSpecName());
        assertEquals("ROUTE_TYPE_TRANSPORT_FLOOD", RouteType.TRANSPORT_FLOOD.toString());
        assertTrue(RouteType.TRANSPORT_FLOOD.isUsingTransport());
        assertTrue(RouteType.TRANSPORT_FLOOD.isFlood());
        assertFalse(RouteType.TRANSPORT_FLOOD.isDirect());
        // FLOOD: bitmask 0x01
        assertEquals(0x01, RouteType.FLOOD.getBitmask());
        assertEquals("ROUTE_TYPE_FLOOD", RouteType.FLOOD.getSpecName());
        assertEquals("ROUTE_TYPE_FLOOD", RouteType.FLOOD.toString());
        assertFalse(RouteType.FLOOD.isUsingTransport());
        assertTrue(RouteType.FLOOD.isFlood());
        assertFalse(RouteType.FLOOD.isDirect());
        // DIRECT: bitmask 0x02
        assertEquals(0x02, RouteType.DIRECT.getBitmask());
        assertEquals("ROUTE_TYPE_DIRECT", RouteType.DIRECT.getSpecName());
        assertEquals("ROUTE_TYPE_DIRECT", RouteType.DIRECT.toString());
        assertFalse(RouteType.DIRECT.isUsingTransport());
        assertFalse(RouteType.DIRECT.isFlood());
        assertTrue(RouteType.DIRECT.isDirect());
        // TRANSPORT_DIRECT: bitmask 0x03
        assertEquals(0x03, RouteType.TRANSPORT_DIRECT.getBitmask());
        assertEquals("ROUTE_TYPE_TRANSPORT_DIRECT", RouteType.TRANSPORT_DIRECT.getSpecName());
        assertEquals("ROUTE_TYPE_TRANSPORT_DIRECT", RouteType.TRANSPORT_DIRECT.toString());
        assertTrue(RouteType.TRANSPORT_DIRECT.isUsingTransport());
        assertFalse(RouteType.TRANSPORT_DIRECT.isFlood());
        assertTrue(RouteType.TRANSPORT_DIRECT.isDirect());
    }

    /**
     * Test the actual parser with all possible valid cases
     * <p>
     * Unfortunately, we have to brute-force all possible 256 combinations of version, routing and payload to make sure
     * the bitfield parser always operates correctly.
     * </p>
     */
    @Test
    void testHeaderParseValidCases() {
        for (int version = 0; version < 4; version++) {
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
                    RouteType result = assertDoesNotThrow(() -> RouteType.fromHeader(payloadByte));
                    assertEquals(routing, result.getBitmask());
                }
            }
        }
    }
    /*
    We do not have invalid route specifications, all variants of the bitmask are occupied
     */
}
