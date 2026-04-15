package de.afa_amateurfunk.meshcore_packets.types;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PayloadType
 *
 * @see PayloadType
 */
class PayloadTypeTest extends AbstractLoggingTest {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PayloadTypeTest.class);

    /**
     * Test that the data repository is correctly represented
     */
    @Test
    void testDataMapping() {
        // Test that we have exactly three members
        assertEquals(13, PayloadType.values().length);
        // REQUEST: bitmask 0x00
        assertEquals(0x00, PayloadType.REQUEST.getBitmask());
        assertEquals("PAYLOAD_TYPE_REQ", PayloadType.REQUEST.getSpecName());
        assertEquals("PAYLOAD_TYPE_REQ", PayloadType.REQUEST.toString());
        // RESPONSE: bitmask 0x01
        assertEquals(0x01, PayloadType.RESPONSE.getBitmask());
        assertEquals("PAYLOAD_TYPE_RESPONSE", PayloadType.RESPONSE.getSpecName());
        assertEquals("PAYLOAD_TYPE_RESPONSE", PayloadType.RESPONSE.toString());
        // TEXT_MESSAGE: bitmask 0x02
        assertEquals(0x02, PayloadType.TEXT_MESSAGE.getBitmask());
        assertEquals("PAYLOAD_TYPE_TXT_MSG", PayloadType.TEXT_MESSAGE.getSpecName());
        assertEquals("PAYLOAD_TYPE_TXT_MSG", PayloadType.TEXT_MESSAGE.toString());
        // ACK: bitmask 0x03
        assertEquals(0x03, PayloadType.ACK.getBitmask());
        assertEquals("PAYLOAD_TYPE_ACK", PayloadType.ACK.getSpecName());
        assertEquals("PAYLOAD_TYPE_ACK", PayloadType.ACK.toString());
        // ADVERT: bitmask 0x04
        assertEquals(0x04, PayloadType.ADVERT.getBitmask());
        assertEquals("PAYLOAD_TYPE_ADVERT", PayloadType.ADVERT.getSpecName());
        assertEquals("PAYLOAD_TYPE_ADVERT", PayloadType.ADVERT.toString());
        // GROUP_TEXT: bitmask 0x05
        assertEquals(0x05, PayloadType.GROUP_TEXT.getBitmask());
        assertEquals("PAYLOAD_TYPE_GRP_TXT", PayloadType.GROUP_TEXT.getSpecName());
        assertEquals("PAYLOAD_TYPE_GRP_TXT", PayloadType.GROUP_TEXT.toString());
        // GROUP_DATAGRAM: bitmask 0x06
        assertEquals(0x06, PayloadType.GROUP_DATAGRAM.getBitmask());
        assertEquals("PAYLOAD_TYPE_GRP_DATA", PayloadType.GROUP_DATAGRAM.getSpecName());
        assertEquals("PAYLOAD_TYPE_GRP_DATA", PayloadType.GROUP_DATAGRAM.toString());
        // ANON_REQUEST: bitmask 0x07
        assertEquals(0x07, PayloadType.ANON_REQUEST.getBitmask());
        assertEquals("PAYLOAD_TYPE_ANON_REQ", PayloadType.ANON_REQUEST.getSpecName());
        assertEquals("PAYLOAD_TYPE_ANON_REQ", PayloadType.ANON_REQUEST.toString());
        // PATH: bitmask 0x08
        assertEquals(0x08, PayloadType.PATH.getBitmask());
        assertEquals("PAYLOAD_TYPE_PATH", PayloadType.PATH.getSpecName());
        assertEquals("PAYLOAD_TYPE_PATH", PayloadType.PATH.toString());
        // TRACE: bitmask 0x09
        assertEquals(0x09, PayloadType.TRACE.getBitmask());
        assertEquals("PAYLOAD_TYPE_TRACE", PayloadType.TRACE.getSpecName());
        assertEquals("PAYLOAD_TYPE_TRACE", PayloadType.TRACE.toString());
        // MULTIPART: bitmask 0x0A
        assertEquals(0x0A, PayloadType.MULTIPART.getBitmask());
        assertEquals("PAYLOAD_TYPE_MULTIPART", PayloadType.MULTIPART.getSpecName());
        assertEquals("PAYLOAD_TYPE_MULTIPART", PayloadType.MULTIPART.toString());
        // CONTROL: bitmask 0x0B
        assertEquals(0x0B, PayloadType.CONTROL.getBitmask());
        assertEquals("PAYLOAD_TYPE_CONTROL", PayloadType.CONTROL.getSpecName());
        assertEquals("PAYLOAD_TYPE_CONTROL", PayloadType.CONTROL.toString());
        // RAW_CUSTOM: bitmask 0x0F
        assertEquals(0x0F, PayloadType.RAW_CUSTOM.getBitmask());
        assertEquals("PAYLOAD_TYPE_RAW_CUSTOM", PayloadType.RAW_CUSTOM.getSpecName());
        assertEquals("PAYLOAD_TYPE_RAW_CUSTOM", PayloadType.RAW_CUSTOM.toString());
    }

    /**
     * Test the actual parser with all possible valid cases
     * <p>
     * Unfortunately, we have to brute-force all possible 256 combinations of version, routing and payload to make sure
     * the bitfield parser always operates correctly.
     * </p>
     * <p>
     * To make our lives even worse than they are, we need to exclude a couple of reserved payload types as well, these
     * are tested later-on.
     * </p>
     *
     * @see PayloadTypeTest#testHeaderParseRejectReserved
     */
    @Test
    void testHeaderParseValidCases() {
        int[] payloadTypes = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0F};
        for (int version = 0; version < 4; version++) {
            byte versionByte = (byte) (0x00 | ((byte) version << 6));
            LOG.trace(String.format("New version row. Initial header byte %02x / %s", versionByte, StringUtils.leftPad(Integer.toBinaryString(versionByte & 0xFF), 8, '0')));
            for (int routing = 0; routing < 4; routing++) {
                byte routingBitmask = (byte) (((byte) routing << 0) & 0xFF);
                LOG.trace(String.format("New routing row. Row bitmask %02x / %s", routingBitmask, StringUtils.leftPad(Integer.toBinaryString(routingBitmask & 0xFF), 8, '0')));
                byte routingByte = (byte) (versionByte | routingBitmask);
                //LOG.trace(String.format("       Resulting header byte %02x / %s", routingByte, StringUtils.leftPad(Integer.toBinaryString(routingByte & 0xFF), 8, '0')));
                for (int payloadType : payloadTypes) {
                    byte payloadBitmask = (byte) (((byte) payloadType << 2) & 0xFF);
                    LOG.trace(String.format("New payload row. Row bitmask %02x / %s %02x", payloadBitmask, StringUtils.leftPad(Integer.toBinaryString(payloadBitmask & 0xFF), 8, '0'), payloadType));
                    byte payloadByte = (byte) (routingByte | payloadBitmask);
                    LOG.trace(String.format("       Resulting header byte %02x / %s", payloadByte, StringUtils.leftPad(Integer.toBinaryString(payloadByte & 0xFF), 8, '0')));
                    PayloadType result = assertDoesNotThrow(() -> PayloadType.fromHeader(payloadByte));
                    assertEquals(payloadType, result.getBitmask());
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
        int[] payloadTypes = {0x0C, 0x0D, 0x0E};
        for (int version = 0; version < 4; version++) {
            byte versionByte = (byte) (0x00 | ((byte) version << 6));
            LOG.trace(String.format("New version row. Initial header byte %02x / %s", versionByte, StringUtils.leftPad(Integer.toBinaryString(versionByte & 0xFF), 8, '0')));
            for (int routing = 0; routing < 4; routing++) {
                byte routingBitmask = (byte) (((byte) routing << 0) & 0xFF);
                LOG.trace(String.format("New routing row. Row bitmask %02x / %s", routingBitmask, StringUtils.leftPad(Integer.toBinaryString(routingBitmask & 0xFF), 8, '0')));
                byte routingByte = (byte) (versionByte | routingBitmask);
                LOG.trace(String.format("       Resulting header byte %02x / %s", routingByte, StringUtils.leftPad(Integer.toBinaryString(routingByte & 0xFF), 8, '0')));
                for (int payloadType : payloadTypes) {
                    byte payloadBitmask = (byte) (((byte) payloadType << 2) & 0xFF);
                    LOG.trace(String.format("New payload row. Row bitmask %02x / %s %02x", payloadBitmask, StringUtils.leftPad(Integer.toBinaryString(payloadBitmask & 0xFF), 8, '0'), payloadType));
                    byte payloadByte = (byte) (routingByte | payloadBitmask);
                    LOG.trace(String.format("       Resulting header byte %02x / %s", payloadByte, StringUtils.leftPad(Integer.toBinaryString(payloadByte & 0xFF), 8, '0')));
                    assertThrows(NoSuchElementException.class, () -> PayloadType.fromHeader(payloadByte));
                }
            }
        }
    }
}