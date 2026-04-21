package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.PathSizeType;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import de.afa_amateurfunk.meshcore_packets.types.RouteType;
import de.afa_amateurfunk.meshcore_packets.types.VersionType;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Raw/Custom packets
 *
 * @see RawCustomPacket
 */
public class RawCustomPacketTest extends AbstractLoggingTest {
    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RawCustomPacketTest.class);

    /**
     * Test decoding of a completely empty packet. Pointless but legal
     * Indirectly, also branch-test MeshcorePacket#fromBytes
     */
    @Test
    public void testParseFullEmptyPacket() {
        String packetBuffer = "3E00";
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        assertEquals(RawCustomPacket.class, packet.getClass());
    }

    /**
     * Test decoding of a full-featured packet
     * Indirectly, also branch-test MeshcorePacket#fromBytes
     */
    @Test
    public void testParseFullPacketWithPayload() {
        String packetBuffer = "3E00AABBCCDDEEFF";
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        assertEquals(RawCustomPacket.class, packet.getClass());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, packet.getPayloadBuffer());
    }

    /**
     * Test constructing a completely empty packet (pointless but legal)
     */
    @Test
    public void testParseEmptyPacket() {
        RawCustomPacket packet = new RawCustomPacket("");
        assertArrayEquals(new byte[]{}, packet.getPayloadBuffer());
    }

    /**
     * Test constructing a packet with payload
     */
    @Test
    public void testParsePacketWithPayload() {
        RawCustomPacket packet = new RawCustomPacket("AABBCCDDEEFF");
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, packet.getPayloadBuffer());
    }

    /**
     * Test constructing a packet with maximum payload
     */
    @Test
    public void testParsePacketWithMaxPayload() {
        String buffer = "000102030405060708090001020304050607080900010203040506070809000102030405060708090001020304050607080900010203040506070809" + //0-59
                "000102030405060708090001020304050607080900010203040506070809000102030405060708090001020304050607080900010203040506070809" + //60-119
                "000102030405060708090001020304050607080900010203040506070809000102030405060708090001020304050607080900010203040506070809" + //120-180
                "00010203"; //180-183
        RawCustomPacket packet = new RawCustomPacket(buffer);
        assertArrayEquals(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //000-009
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //010-019
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //020-029
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //030-039
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //040-049
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //050-059
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //060-069
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //070-079
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //080-089
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //090-099
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //100-109
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //110-119
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //120-129
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //130-139
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //140-149
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //150-159
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //160-169
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, //170-179
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 //180-183

        }, packet.getPayloadBuffer());
    }

    /**
     * Test constructing a packet with payload
     */
    @Test
    public void testRejectTooLongPayload() {
        String buffer = "000102030405060708090001020304050607080900010203040506070809000102030405060708090001020304050607080900010203040506070809" + //0-59
                "000102030405060708090001020304050607080900010203040506070809000102030405060708090001020304050607080900010203040506070809" + //60-119
                "000102030405060708090001020304050607080900010203040506070809000102030405060708090001020304050607080900010203040506070809" + //120-180
                "0001020304"; //180-184
        assertThrows(ParseErrorException.class, () -> new RawCustomPacket(buffer));
        RawCustomPacket packet = new RawCustomPacket();
        assertThrows(ParseErrorException.class, () -> packet.setPayloadBuffer(hexFormat.parseHex(buffer)));
    }

    /**
     * Construct a blank RawCustomPacket from scratch
     */
    @Test
    public void testCreateFromScratch() {
        RawCustomPacket packet = new RawCustomPacket();
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.DIRECT, packet.getPacketRouting());
        assertEquals(PayloadType.RAW_CUSTOM, packet.getPacketPayloadType());
        assertEquals(PathSizeType.SIZE_1, packet.getPacketPathInformation().getPacketPathSize());
        assertEquals(0, packet.getPacketPathInformation().getHopCount());
        assertArrayEquals(new byte[]{}, packet.getPayloadBuffer());
    }

    /**
     * Construct a RawCustomPacket from scratch with existing payload
     */
    @Test
    public void testCreateFromScratchWithPayloadString() {
        RawCustomPacket packet = new RawCustomPacket("AABBCCDDEEFF");
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.DIRECT, packet.getPacketRouting());
        assertEquals(PayloadType.RAW_CUSTOM, packet.getPacketPayloadType());
        assertEquals(PathSizeType.SIZE_1, packet.getPacketPathInformation().getPacketPathSize());
        assertEquals(0, packet.getPacketPathInformation().getHopCount());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, packet.getPayloadBuffer());
    }

    /**
     * Construct a RawCustomPacket from scratch with existing payload
     */
    @Test
    public void testCreateFromScratchWithPayloadByteArray() {
        RawCustomPacket packet = new RawCustomPacket(hexFormat.parseHex("AABBCCDDEEFF"));
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.DIRECT, packet.getPacketRouting());
        assertEquals(PayloadType.RAW_CUSTOM, packet.getPacketPayloadType());
        assertEquals(PathSizeType.SIZE_1, packet.getPacketPathInformation().getPacketPathSize());
        assertEquals(0, packet.getPacketPathInformation().getHopCount());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, packet.getPayloadBuffer());
    }

    /**
     * Test setting payload data on a blank packet
     */
    @Test
    public void testSetPayloadBlankPacket() {
        RawCustomPacket packet = new RawCustomPacket();
        packet.parsePayload(hexFormat.parseHex("AABBCCDDEEFF"));
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, packet.getPayloadBuffer());
        String expectedPacketBuffer = "3E00AABBCCDDEEFF";
        //TODO implement full byte comparison once we have toByteArray() on MeshcorePacket
    }

    /**
     * Test setting payload data on an existing packet
     */
    @Test
    public void testSetPayloadExistingPacket() {
        String packetBuffer = "3E00FFEEDDCCBBAA";
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        packet.parsePayload(hexFormat.parseHex("AABBCCDDEEFF"));
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, packet.getPayloadBuffer());
        String expectedPacketBuffer = "3E00AABBCCDDEEFF";
        //TODO implement full byte comparison once we have toByteArray() on MeshcorePacket
    }
}
