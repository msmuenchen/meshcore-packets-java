package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.types.PathSizeType;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import de.afa_amateurfunk.meshcore_packets.types.RouteType;
import de.afa_amateurfunk.meshcore_packets.types.VersionType;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, ((RawCustomPacket) packet).getPayloadBuffer());
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

    /*
    No rejection test - anything is legal, max packet length is tested in {@link HeaderIntegrationTests}
     */

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
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF}, ((RawCustomPacket) packet).getPayloadBuffer());
        String expectedPacketBuffer = "3E00AABBCCDDEEFF";
        //TODO implement full byte comparison once we have toByteArray() on MeshcorePacket
    }
}
