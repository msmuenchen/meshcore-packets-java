package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.AdvertNodeType;
import de.afa_amateurfunk.meshcore_packets.types.ControlPacketType;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ControlResponsePacket
 *
 * @see ControlResponsePacket
 */
public class ControlResponsePacketTest extends AbstractLoggingTest {
    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ControlResponsePacketTest.class);

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
        assertThrows(ParseErrorException.class, () -> packet.subclassFromBytes(new byte[]{(byte) 0x90}));
    }

    /**
     * Test that a 1, 13, 15, 37  and 39 byte packet gets rejected
     *
     * @see ControlResponsePacket#parsePayload(byte[]) for the gate under test
     */
    @Test
    public void testRejectInvalidLengthPacketInnerGate() {
        String[] invalidBuffers = new String[]{
                "90", // flag
                "9020aabbccdd00112233445566", // flag + snr + tag + pk (1 byte too short of 7 bytes)
                "9020aabbccdd001122334455667788", // flag + filter + pk + 1 byte extra
                "9020aabbccdd00112233445566778899aabbccddeeff00112233445566778899aabbccddee", // flag + filter + tag + pk (1 byte short of 32 bytes)
                "9020aabbccdd00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00", // flag + filter + tag + pk + 1 byte extra
        };
        for (String buffer : invalidBuffers) {
            assertThrows(ParseErrorException.class, () -> new ControlResponsePacket(buffer));
        }
    }

    /**
     * Test that a payload with subtype DISCOVER_REQUEST gets rejected
     */
    @Test
    public void testRejectRequestPayload() {
        String buffer = "8020aabbccdd0011223344556677"; // remainder is garbage, the gate should close way before parsing
        assertThrows(ParseErrorException.class, () -> new ControlResponsePacket(buffer));
    }

    /**
     * Test parsing a valid DISCOVER_RESPONSE payload with short pubkey
     */
    @Test
    void testParseShortKeyPayload() {
        // Response to AABBCCDD, Repeater, 9 dB SNR, PK 0001020304050607
        String buffer = "9224AABBCCDD0001020304050607";
        ControlResponsePacket packet = new ControlResponsePacket(buffer);
        //Verify superstructure
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacketType.DISCOVER_RESPONSE, packet.getSubtype());
        //Verify the payload
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((byte) 0x24, packet.getSnr());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, packet.getTag());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
                },
                packet.getPublicKey());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
    }

    /**
     * Test parsing a full valid DISCOVER_RESPONSE payload
     */
    @Test
    void testParseFullKeyPayload() {
        // Response to AABBCCDD, Repeater, 9 dB SNR, PK 000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f
        String buffer = "9224AABBCCDD000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f";
        ControlResponsePacket packet = new ControlResponsePacket(hexFormat.parseHex(buffer));
        //Verify superstructure
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacketType.DISCOVER_RESPONSE, packet.getSubtype());
        //Verify the payload
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((byte) 0x24, packet.getSnr());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, packet.getTag());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), packet.getPayloadBuffer());
    }

    /**
     * Test creation of packet with full length key
     */
    @Test
    void createFromScratchFullKey() {
        // Response to AABBCCDD, Repeater, 9 dB SNR, PK 000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f
        String expectedBuffer = "9224AABBCCDD000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f";
        ControlResponsePacket packet = new ControlResponsePacket();
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setSnr((byte) 0x24);
        packet.setTag(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD});
        packet.setPublicKey(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
        });
        //Verify superstructure
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacketType.DISCOVER_RESPONSE, packet.getSubtype());
        //Verify the payload
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((byte) 0x24, packet.getSnr());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, packet.getTag());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test creation of packet with short key
     */
    @Test
    void createFromScratchShortKey() {
// Response to AABBCCDD, Repeater, 9 dB SNR, PK 0001020304050607
        String expectedBuffer = "9224AABBCCDD0001020304050607";
        ControlResponsePacket packet = new ControlResponsePacket();
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setSnr((byte) 0x24);
        packet.setTag(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD});
        packet.setPublicKey(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07
        });
        //Verify superstructure
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacketType.DISCOVER_RESPONSE, packet.getSubtype());
        //Verify the payload
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((byte) 0x24, packet.getSnr());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, packet.getTag());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
                },
                packet.getPublicKey());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that invalid public key lengths get rejected
     */
    @Test
    public void testRejectInvalidPublicKey() {
        //Too short 1 byte for short key
        assertThrows(InvalidParameterException.class, () -> new ControlResponsePacket().setPublicKey(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
        }));
        // Too long 1 byte for short key
        assertThrows(InvalidParameterException.class, () -> new ControlResponsePacket().setPublicKey(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
        }));
        //Too short 1 byte for long key
        assertThrows(InvalidParameterException.class, () -> new ControlResponsePacket().setPublicKey(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e
        }));
        // Too long 1 byte for long key
        assertThrows(InvalidParameterException.class, () -> new ControlResponsePacket().setPublicKey(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                (byte) 0x00,
        }));
    }
}
