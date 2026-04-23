package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.AdvertNodeType;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ControlRequestPacket
 *
 * @see ControlRequestPacket
 */
public class ControlRequestPacketTest extends AbstractLoggingTest {
    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ControlRequestPacketTest.class);

    /**
     * Test that an empty packet gets rejected
     */
    @Test
    public void testRejectEmptyPacket() {
        ControlPacket packet;
        packet = new ControlPacket();
        assertThrows(ParseErrorException.class, () -> packet.subclassFromBytes(new byte[]{}));
    }

    /**
     * Test that a 1 byte packet gets rejected
     *
     * @see ControlPacket#subclassFromBytes(byte[]) for the gate under test
     */
    @Test
    public void testRejectTooShortPacket() {
        ControlPacket packet;
        packet = new ControlPacket();
        assertThrows(ParseErrorException.class, () -> packet.subclassFromBytes(new byte[]{(byte) 0x80}));
    }

    /**
     * Test that a 1, 2, 5, 7, 9  and 11 byte packet gets rejected
     *
     * @see ControlRequestPacket#parsePayload(byte[]) for the gate under test
     */
    @Test
    public void testRejectTooShortPacketInnerGate() {
        String[] invalidBuffers = new String[]{
                "80", // flag
                "8000", // flag + filter
                "8000aabbcc", // flag + filter + too short tag
                "8000aabbccddaa", // flag + filter + tag + too short since
                "8000aabbccddaabbcc", // flag + filter + tag + too short since
                "8000aabbccddaabbccddee", // flag + filter + tag + since + extra
        };
        for (String buffer : invalidBuffers) {
            assertThrows(ParseErrorException.class, () -> new ControlRequestPacket(hexFormat.parseHex(buffer)));
        }
    }

    /**
     * Test that a payload with subtype DISCOVER_RESPONSE gets rejected
     */
    @Test
    public void testRejectResponsePayload() {
        String buffer = "90aabbccddee"; // remainder is garbage, the gate should close way before parsing
        assertThrows(ParseErrorException.class, () -> new ControlRequestPacket(hexFormat.parseHex(buffer)));
    }

    /**
     * Test that the prefix_only flag gets successfully parsed
     */
    @Test
    public void testParsePrefixOnlySet() {
        ControlRequestPacket packet;
        String buffer;
        // Test set case
        buffer = "8100aabbccdd";
        packet = new ControlRequestPacket(buffer);
        assertTrue(packet.getPrefixOnly());
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, packet.getTag());
        assertNull(packet.getSince());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
        // Test not-set case
        buffer = "8000aabbccdd";
        packet = new ControlRequestPacket(buffer);
        assertFalse(packet.getPrefixOnly());
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, packet.getTag());
        assertNull(packet.getSince());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
    }

    /**
     * Test that unknown flags lead to an error
     */
    @Test
    public void testRejectUnknownFlags() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd};
        for (int i = 2; i < 16; i++) {
            buffer[0] = (byte) (0x80 | i);
            assertThrows(ParseErrorException.class, () -> new ControlRequestPacket(buffer));
        }
    }

    /**
     * Test empty list of discovery filter
     */
    @Test
    public void testParseEmptyFilter() {
        String buffer = "8000aabbccdd";
        ControlRequestPacket packet = new ControlRequestPacket(buffer);
        assertArrayEquals(new LinkedList<AdvertNodeType>().toArray(), packet.getTypeFilter().toArray());
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, packet.getTag());
        assertNull(packet.getSince());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
    }

    /**
     * Test each single individual packet filter
     */
    @Test
    public void testParsePacketFilters() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd};
        for (AdvertNodeType type : AdvertNodeType.values()) {
            buffer[1] = (byte) (0x01 << type.getIndex());
            ControlRequestPacket packet = new ControlRequestPacket(buffer);
            assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, packet.getTag());
            assertFalse(packet.getPrefixOnly());
            assertNull(packet.getSince());
            // Test reconstitution
            assertArrayEquals(buffer, packet.getPayloadBuffer());
        }
    }

    /**
     * Test rejecting unknown packet filters
     */
    @Test
    public void testRejectUnknownFilters() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd};
        for (byte filter : new byte[]{0x20, 0x40, (byte) 0x80}) {
            buffer[1] = filter;
            assertThrows(ParseErrorException.class, () -> new ControlRequestPacket(buffer));
        }
    }

    /**
     * Test that all filters get recognized
     */
    @Test
    public void testAllFilters() {
        String buffer = "801FAABBCCDD";
        ControlRequestPacket packet = new ControlRequestPacket(buffer);
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, packet.getTag());
        assertNull(packet.getSince());
        assertArrayEquals(AdvertNodeType.values(), packet.getTypeFilter().toArray());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
    }

    /**
     * Test the since field
     */
    @Test
    public void testSinceField() {
        String buffer = "801FAABBCCDD0994E569"; // ts / 1776653321 (2026-04-20T02:48:41Z)
        ControlRequestPacket packet = new ControlRequestPacket(buffer);
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, packet.getTag());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getSince());
        assertArrayEquals(AdvertNodeType.values(), packet.getTypeFilter().toArray());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
    }

    /**
     * Test construct from scratch
     */
    @Test
    public void testCreateFromScratchNoSince() {
        ControlRequestPacket packet = new ControlRequestPacket();
        packet.setPrefixOnly(true);
        packet.getTypeFilter().add(AdvertNodeType.REPEATER);
        packet.setTag(new byte[]{(byte) 0xdd, (byte) 0xcc, (byte) 0xbb, (byte) 0xaa});

        assertArrayEquals(new byte[]{(byte) 0xdd, (byte) 0xcc, (byte) 0xbb, (byte) 0xaa}, packet.getTag());
        assertTrue(packet.getPrefixOnly());
        assertNull(packet.getSince());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex("8104DDCCBBAA"), packet.getPayloadBuffer());
    }

    /**
     * Test construct from scratch with since field
     */
    @Test
    public void testCreateFromScratchWithSince() {
        ControlRequestPacket packet = new ControlRequestPacket();
        packet.setPrefixOnly(true);
        packet.getTypeFilter().add(AdvertNodeType.REPEATER);
        packet.setTag(new byte[]{(byte) 0xdd, (byte) 0xcc, (byte) 0xbb, (byte) 0xaa});
        packet.setSince(Instant.ofEpochSecond(1776653321));

        assertArrayEquals(new byte[]{(byte) 0xdd, (byte) 0xcc, (byte) 0xbb, (byte) 0xaa}, packet.getTag());
        assertTrue(packet.getPrefixOnly());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getSince());
        // Test reconstitution
        assertArrayEquals(hexFormat.parseHex("8104DDCCBBAA0994E569"), packet.getPayloadBuffer());
    }
}
