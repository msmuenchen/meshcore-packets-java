package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.AdvertNodeType;
import de.afa_amateurfunk.meshcore_packets.types.ControlPacketType;
import de.afa_amateurfunk.meshcore_packets.types.ControlPacketTypeTest;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the ControlPacket superclass
 */
public class ControlPacketTest extends AbstractLoggingTest {
    /**
     * Test creating a packet from scratch
     */
    @Test
    void testFromScratch() {
        ControlPacket packet = new ControlPacket();
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacket.class, packet.getClass());
    }

    /**
     * Test parsing a DISCOVER_REQUEST payload
     */
    @Test
    void testParseRequestPayload() {
        // Request repeaters, tag AABBCCDD
        String buffer = "8004AABBCCDD";
        //Create a blank packet from scratch
        ControlPacket packet = new ControlPacket();
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacket.class, packet.getClass());
        assertNull(packet.getSubtype());
        //Add the payload
        ControlPacket actualPacket = packet.subclassFromBytes(hexFormat.parseHex(buffer));
        //Verify "blank" packet still is the same
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacket.class, packet.getClass());
        //Subtype may have changed, subclassFromBytes modifies this
        assertEquals(ControlPacketType.DISCOVER_REQUEST, actualPacket.getSubtype());
        //Verify the payload
        assertEquals(PayloadType.CONTROL, actualPacket.getPacketPayloadType());
        assertEquals(ControlRequestPacket.class, actualPacket.getClass());
        assertEquals(ControlPacketType.DISCOVER_REQUEST, actualPacket.getSubtype());
        assertFalse(((ControlRequestPacket) actualPacket).getPrefixOnly());
        assertEquals((byte) 0x04, ((ControlRequestPacket) actualPacket).getTypeFilter());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, ((ControlRequestPacket) actualPacket).getTag());
        assertNull(((ControlRequestPacket) actualPacket).getSince());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), actualPacket.getPayloadBuffer());
    }

    /**
     * Test parsing a DISCOVER_RESPONSE payload
     */
    @Test
    void testParseResponsePayload() {
        // Response to AABBCCDD, Repeater, 9 dB SNR, PK 000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f
        String buffer = "9224AABBCCDD000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f";
        //Create a blank packet from scratch
        ControlPacket packet = new ControlPacket();
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacket.class, packet.getClass());
        assertNull(packet.getSubtype());
        //Add the payload
        ControlPacket actualPacket = packet.subclassFromBytes(hexFormat.parseHex(buffer));
        //Verify "blank" packet still is the same
        assertEquals(PayloadType.CONTROL, packet.getPacketPayloadType());
        assertEquals(ControlPacket.class, packet.getClass());
        //Subtype may have changed, subclassFromBytes modifies this
        assertEquals(ControlPacketType.DISCOVER_RESPONSE, actualPacket.getSubtype());
        //Verify the payload
        assertEquals(PayloadType.CONTROL, actualPacket.getPacketPayloadType());
        assertEquals(ControlResponsePacket.class, actualPacket.getClass());
        assertEquals(ControlPacketType.DISCOVER_RESPONSE, actualPacket.getSubtype());
        assertEquals(AdvertNodeType.REPEATER, ((ControlResponsePacket) actualPacket).getNodeType());
        assertEquals((byte) 0x24, ((ControlResponsePacket) actualPacket).getSnr());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD}, ((ControlResponsePacket) actualPacket).getTag());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                ((ControlResponsePacket) actualPacket).getPublicKey());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(buffer), actualPacket.getPayloadBuffer());
    }

    /**
     * test if packets with no payload get rejected
     */
    @Test
    void testRejectEmptyPayload() {
        ControlPacket packet = new ControlPacket();
        //Add the payload
        assertThrows(ParseErrorException.class, () -> packet.subclassFromBytes(new byte[]{}));
    }

    /**
     * test if packets with invalid subtype get rejected
     * <p>Only rough test - more detailed test including the flag bits are in {@link ControlPacketTypeTest#testRejectInvalidCases()}</p>
     */
    @Test
    void testRejectInvalidSubtypes() {
        byte[] invalidBitmasks = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0x40, (byte) 0x50, (byte) 0x60, (byte) 0x70, (byte) 0xA0, (byte) 0xB0, (byte) 0xC0, (byte) 0xD0, (byte) 0xE0, (byte) 0xF0};
        for (byte bitmask : invalidBitmasks) {
            ControlPacket packet = new ControlPacket();
            //Add the payload
            assertThrows(NoSuchElementException.class, () -> packet.subclassFromBytes(new byte[]{bitmask}));
        }
    }

    /**
     * Test if the parsePayload method always throws an exception
     */
    @Test
    void testParsePayloadDisabled() {
        ControlPacket packet = new ControlPacket();
        assertThrows(NotImplementedException.class, () -> packet.parsePayload(new byte[]{}));
    }

    /**
     * Test if the getPayloadBuffer method always throws an exception
     */
    @Test
    void testGetPayloadBufferDisabled() {
        ControlPacket packet = new ControlPacket();
        assertThrows(NotImplementedException.class, packet::getPayloadBuffer);
    }
}
