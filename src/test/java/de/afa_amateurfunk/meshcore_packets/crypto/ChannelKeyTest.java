package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.Util;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

public class ChannelKeyTest extends AbstractLoggingTest {
    /**
     * Test that hashtag channels need to have a # prefix
     */
    @Test
    public void testRejectHashtagNoHash() {
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("foo"));
    }

    /**
     * Test that private channels need to have a # prefix
     */
    @Test
    public void testRejectPrivateWithHash() {
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("#foo", new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f}));
    }

    /**
     * Test that too short and too long channel names are rejected for hashtag channels
     */
    @Test
    public void testRejectHashtagWrongLength() {
        assertThrows(InvalidParameterException.class, () -> new ChannelKey(""));
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("#"));
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("#0123456789012345678901234567890"));
    }

    /**
     * Test that too short and too long channel names are rejected
     */
    @Test
    public void testRejectPrivateWrongLength() {
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("", new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f}));
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("01234567890123456789012345678901", new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f}));
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("tooshortkey", new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e}));
        assertThrows(InvalidParameterException.class, () -> new ChannelKey("toolongkey", new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10}));
    }


    /**
     * Test correct generation of keys with a list of known channel names
     */
    @Test
    public void testHashtagChannels() {
        ChannelKey k;
        k = new ChannelKey("#ping");
        assertEquals("#ping", k.getName());
        assertArrayEquals(Util.hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525"), k.getKey());
        assertArrayEquals(Util.hexFormat.parseHex("2885d962dbd452e6403c88a7d02c93a7e4dadc5d1733bafe84a298197cfb27c9"), k.getChannelHash());
        assertArrayEquals(new byte[]{0x28}, k.getChannelHash(1));
        assertTrue(k.isHashtagChannel());
        k = new ChannelKey("#test");
        assertEquals("#test", k.getName());
        assertArrayEquals(Util.hexFormat.parseHex("9cd8fcf22a47333b591d96a2b848b73f"), k.getKey());
        assertArrayEquals(Util.hexFormat.parseHex("d9f4e8fd5a720b48bb4f9d31a0a9857f8389df7c1fbf36476081bb8d96cb36d6"), k.getChannelHash());
        assertArrayEquals(new byte[]{(byte) 0xd9}, k.getChannelHash(1));
        assertTrue(k.isHashtagChannel());
    }

    /**
     * test that hashtag channel names are immutable
     */
    @Test
    public void testHashtagChannelImmutableName() {
        ChannelKey k;
        k = new ChannelKey("#bot");
        assertEquals("#bot", k.getName());
        assertArrayEquals(Util.hexFormat.parseHex("eb50a1bcb3e4e5d7bf69a57c9dada211"), k.getKey());
        assertArrayEquals(Util.hexFormat.parseHex("ca589aad56d89b05b347d1863883643a9d2bfc8af116a8d05257efbbd37f5f3a"), k.getChannelHash());
        assertArrayEquals(new byte[]{(byte) 0xca}, k.getChannelHash(1));
        assertTrue(k.isHashtagChannel());
        assertThrows(InvalidParameterException.class, () -> k.setName("#banana"));
    }

    /**
     * test behavior of private channels
     */
    @Test
    public void testPrivateChannels() {
        ChannelKey k;
        k = new ChannelKey("private channel 1", new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f});
        assertEquals("private channel 1", k.getName());
        assertArrayEquals(Util.hexFormat.parseHex("000102030405060708090a0b0c0d0e0f"), k.getKey());
        assertArrayEquals(Util.hexFormat.parseHex("be45cb2605bf36bebde684841a28f0fd43c69850a3dce5fedba69928ee3a8991"), k.getChannelHash());
        assertArrayEquals(new byte[]{(byte) 0xbe}, k.getChannelHash(1));
        assertFalse(k.isHashtagChannel());
        k.setName("private channel 2");
        assertEquals("private channel 2", k.getName());
        assertThrows(InvalidParameterException.class, () -> k.setName(""));
        assertThrows(InvalidParameterException.class, () -> k.setName("01234567890123456789012345678901"));
    }

    /**
     * test equality checks
     */
    @Test
    public void testEquals() {
        //deny: null
        assertNotEquals(new ChannelKey("#blackout"), null);
        //deny: different object
        assertNotEquals(new ChannelKey("#blackout"), "foo");
        //accept: same hashtag channel name
        assertEquals(new ChannelKey("#blackout"), new ChannelKey("#blackout"));
        //deny: different hashtag channel names
        assertNotEquals(new ChannelKey("#blackout"), new ChannelKey("#foo"));
        //accept: identical name and key
        assertEquals(
                new ChannelKey("foo", Util.hexFormat.parseHex("000102030405060708090a0b0c0d0e0f")),
                new ChannelKey("foo", Util.hexFormat.parseHex("000102030405060708090a0b0c0d0e0f"))
        );
        //deny: identical name, different key
        assertNotEquals(
                new ChannelKey("foo", Util.hexFormat.parseHex("000102030405060708090a0b0c0d0e0f")),
                new ChannelKey("foo", Util.hexFormat.parseHex("0f0e0d0c0b0a09080706050403020100"))
        );
        //deny: different name, identical key
        assertNotEquals(
                new ChannelKey("foo", Util.hexFormat.parseHex("000102030405060708090a0b0c0d0e0f")),
                new ChannelKey("bar", Util.hexFormat.parseHex("000102030405060708090a0b0c0d0e0f"))
        );
    }
}
