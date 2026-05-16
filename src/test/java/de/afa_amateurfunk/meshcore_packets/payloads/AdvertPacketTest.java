package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.crypto.PrivateKey;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.AdvertNodeType;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Advert packets
 *
 * @see AdvertPacket
 */
public class AdvertPacketTest extends AbstractLoggingTest {
    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AdvertPacketTest.class);

    /**
     * Test that an empty packet gets rejected
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectEmptyPacket() {
        assertThrows(ParseErrorException.class, () -> new AdvertPacket(new byte[]{}));
        // Only test fromString here, we rely on MeshcorePacket#fromString and our own constructor to call fromBytes and do nothing else
    }

    /**
     * Test that a too short packet gets rejected
     * <p>A minimal advert packet consists of 32 bytes (pk)+4 bytes(ts)+64 bytes (sig)+1 byte(appdata flag)</p>
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectTooShortPacket() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "00112233" + //ts
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f"; // sig 32-63

        assertThrows(ParseErrorException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /*
    todo add tests for timestamps that might cause issues (less than zero/before unix epoch, after year 2038)
     */

    /*
    todo add tests for signature verification and signing
     */

    /**
     * Test that a minimal packet gets accepted
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testParseMinimalPacket() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "02"; // appdata (is repeater)
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchMinimalPacket() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));

        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "3b697e539f54252c8ae69cc7f7df09a7513e065efaedf946641ce8cce51ca9e3" + // sig 0-31
                "0efee0b8367eab46273d9f831e9d323d2e4414ff0e16895f5ed19e7b46aeeb01" + // sig 32-63
                "02"; // appdata (is repeater)
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("3b697e539f54252c8ae69cc7f7df09a7513e065efaedf946641ce8cce51ca9e30efee0b8367eab46273d9f831e9d323d2e4414ff0e16895f5ed19e7b46aeeb01"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a too short packet gets rejected
     * <p>A minimal advert packet consists of 32 bytes (pk)+4 bytes(ts)+64 bytes (sig)+1 byte(appdata flag)+4 bytes(lat)+4 bytes(long)</p>
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectTooShortPacketWithLatLong() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "12" + // appdata (is repeater, has lat/long)
                "bd84de0295a0b0"; // 48.13740596750293 11.575445878381272, Munich Marienplatz
        assertThrows(BufferUnderflowException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /**
     * Test that a minimal packet with lat/long gets accepted
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testParsePacketWithLatLong() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "12" + // appdata (is repeater, has lat/long)
                "bd84de0295a0b000"; // 48.13740596750293 11.575445878381272, Munich Marienplatz
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals(48137405, packet.getLatitude());
        assertEquals(11575445, packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet with lat/long gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithLatLong() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));
        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "15d094d0567e0a3cca002552e11b6bcefe4acfa8c845675f4571963f53dbf2f7" + // sig 0-31
                "041192c03cbbe835c412c2ba1bee77d9045c4c617a87b72711fb76b195680e0a" + // sig 32-63
                "12" + // appdata (is repeater, has lat/long)
                "bd84de0295a0b000"; // 48.13740596750293 11.575445878381272, Munich Marienplatz

        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setLatitude(48137405);
        packet.setLongitude(11575445);
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("15d094d0567e0a3cca002552e11b6bcefe4acfa8c845675f4571963f53dbf2f7041192c03cbbe835c412c2ba1bee77d9045c4c617a87b72711fb76b195680e0a"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals(48137405, packet.getLatitude());
        assertEquals(11575445, packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a minimal packet with lat/long gets rejected when either lat or long are missing
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectMissingLatLong() {
        AdvertPacket missingLatPacket = new AdvertPacket();
        missingLatPacket.setTimestamp(Instant.ofEpochSecond(1776653321));
        missingLatPacket.setNodeType(AdvertNodeType.REPEATER);
        missingLatPacket.setLongitude(11575445);
        assertThrows(InvalidParameterException.class, missingLatPacket::getPayloadBuffer);

        AdvertPacket missingLongPacket = new AdvertPacket();
        missingLongPacket.setTimestamp(Instant.ofEpochSecond(1776653321));
        missingLongPacket.setNodeType(AdvertNodeType.REPEATER);
        missingLongPacket.setLatitude(48137405);

        assertThrows(InvalidParameterException.class, missingLongPacket::getPayloadBuffer);
    }

    /**
     * Test that a too short packet gets rejected
     * <p>A minimal advert packet consists of 32 bytes (pk)+4 bytes(ts)+64 bytes (sig)+1 byte(appdata flag)+2 bytes(feat1)</p>
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectTooShortPacketWithFeat1() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "22" + // appdata (is repeater, has feat1)
                "aa"; // feat1 (1 byte short)

        assertThrows(BufferUnderflowException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /**
     * Test that a minimal packet with feat1 gets accepted
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testParsePacketWithFeat1() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "22" + // appdata (is repeater, has feat1)
                "aabb"; // feat1
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((short) 0xBBAA, packet.getFeat1());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat2());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet with feat1 gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithFeat1() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));

        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "f0230116349aaeee1e484bed58fd79161f9383d57fbfcf2f38af67879c0360de" + // sig 0-31
                "5114b2b1b00b90ed179a4b3b0dd01a3d8e128abce5742f30402755a466ab9009" + // sig 32-63
                "22" + // appdata (is repeater, has feat1)
                "aabb"; // feat1
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setFeat1((short) 0xbbaa);
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("f0230116349aaeee1e484bed58fd79161f9383d57fbfcf2f38af67879c0360de5114b2b1b00b90ed179a4b3b0dd01a3d8e128abce5742f30402755a466ab9009"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((short) 0xBBAA, packet.getFeat1());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat2());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a too short packet gets rejected
     * <p>A minimal advert packet consists of 32 bytes (pk)+4 bytes(ts)+64 bytes (sig)+1 byte(appdata flag)+2 bytes(feat2)</p>
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectTooShortPacketWithFeat2() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "22" + // appdata (is repeater, has feat2)
                "cc"; // feat2 (1 byte short)

        assertThrows(BufferUnderflowException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /**
     * Test that a minimal packet with feat2 gets accepted
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testParsePacketWithFeat2() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "42" + // appdata (is repeater, has feat2)
                "ccdd"; // feat2
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((short) 0xDDCC, packet.getFeat2());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet with feat2 gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithFeat2() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));

        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "d6ed770847bd92a23130b47957597a688faae0a6fcda59ccc802c65f0e8fb3b9" + // sig 0-31
                "4a8b543390ee723b485d8f24c87c2d3fce76d2dabbf73ec77c669de56b992005" + // sig 32-63
                "42" + // appdata (is repeater, has feat2)
                "ccdd"; // feat2
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setFeat2((short) 0xddcc);
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("d6ed770847bd92a23130b47957597a688faae0a6fcda59ccc802c65f0e8fb3b94a8b543390ee723b485d8f24c87c2d3fce76d2dabbf73ec77c669de56b992005"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals((short) 0xDDCC, packet.getFeat2());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a too short packet gets rejected
     * <p>A minimal advert packet consists of 32 bytes (pk)+4 bytes(ts)+64 bytes (sig)+1 byte(appdata flag)+1-n bytes (name)</p>
     * todo also add full packet harnessing (packet, path, payload)
     * todo upstream accepts empty names https://github.com/meshcore-dev/MeshCore/blob/dev/src/helpers/AdvertDataHelpers.cpp#L48
     */
    @Test
    public void testRejectTooShortPacketWithName() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "82";  // appdata (is repeater, has name), but no name follows

        assertThrows(ParseErrorException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /**
     * Test that a minimal packet with shortest allowed name (1 byte) gets accepted
     * todo also add full packet harnessing (packet, path, payload)
     * todo add full name length
     */
    @Test
    public void testParsePacketWithName1Byte() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "82" + // appdata (is repeater, has name)
                "41"; // "A"
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertArrayEquals(new byte[]{0x41}, packet.getNodeName());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet with shortest allowed name (1 byte) gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithName1Byte() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));

        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "fc036b9e0f776f1a91461ec6d4de008c9e6eac76955d808338a5177906c5eb55" + // sig 0-31
                "e8eda8590681de1711a4e3f8673e5b83b261f5a5ea575ce9cf4664955af1df09" + // sig 32-63
                "82" + // appdata (is repeater, has name)
                "41"; // "A"
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setNodeName(new byte[]{0x41});
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("fc036b9e0f776f1a91461ec6d4de008c9e6eac76955d808338a5177906c5eb55e8eda8590681de1711a4e3f8673e5b83b261f5a5ea575ce9cf4664955af1df09"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertArrayEquals(new byte[]{0x41}, packet.getNodeName());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a minimal packet with longest allowed name (32 bytes) gets accepted
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testParsePacketWithName32Bytes() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "82" + // appdata (is repeater, has name)
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "3031"; // "01" name 30-31
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertArrayEquals(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        }, packet.getNodeName());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet with shortest allowed name (1 byte) gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithName32Bytes() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));

        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "706be22cd54423504c07b4f802ae83410d74260232f90866bb443c58692ed280" + // sig 0-31
                "ef8e6cd358d60a4cc2c761a104cf86bc03f59247f979d4c54b30333f25895a06" + // sig 32-63
                "82" + // appdata (is repeater, has name)
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "3031"; // "01" name 30-31
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setNodeName(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        });
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("706be22cd54423504c07b4f802ae83410d74260232f90866bb443c58692ed280ef8e6cd358d60a4cc2c761a104cf86bc03f59247f979d4c54b30333f25895a06"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertArrayEquals(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        }, packet.getNodeName());
        assertNull(packet.getLatitude());
        assertNull(packet.getLongitude());
        assertNull(packet.getFeat1());
        assertNull(packet.getFeat2());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a minimal packet with too long name (33 bytes) gets rejected
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectPacketWithName33Bytes() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "82" + // appdata (is repeater, has name)
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "303132"; // "012" name 30-33
        assertThrows(ParseErrorException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /**
     * Test a full packet with everything enabled
     */
    @Test
    public void testParsePacketWithAllFeatures() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "F2" + // appdata (is repeater, has lat/long, has feat1, has feat2, has name)
                "bd84de0295a0b000" + // 48.13740596750293 11.575445878381272, Munich Marienplatz
                "aabb" + // feat1
                "ccdd" + // feat2
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "3031"; // "01" name 30-31
        AdvertPacket packet = new AdvertPacket(hexFormat.parseHex(payloadBuffer));
        assertArrayEquals(
                new byte[]{
                        (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
                },
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                        (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
                },
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals(48137405, packet.getLatitude());
        assertEquals(11575445, packet.getLongitude());
        assertEquals((short) 0xBBAA, packet.getFeat1());
        assertEquals((short) 0xDDCC, packet.getFeat2());
        assertArrayEquals(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        }, packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(payloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a full packet with everything enabled gets constructed from scratch
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithAllFeatures() {
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey), hexFormat.parseHex(publicKey));

        String expectedPayloadBuffer = publicKey + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "f0e4bc8129259e77a2a248e5b0d85b441b3d7a50f6f24601312ac798a338d63a" + // sig 0-31
                "070effabc179723c74ad371690bba1ee0588d23a6deb7ed42d7b821aa196d605" + // sig 32-63
                "F2" + // appdata (is repeater, has lat/long, has feat1, has feat2, has name)
                "bd84de0295a0b000" + // 48.13740596750293 11.575445878381272, Munich Marienplatz
                "aabb" + // feat1
                "ccdd" + // feat2
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "3031"; // "01" name 30-31
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setLatitude(48137405);
        packet.setLongitude(11575445);
        packet.setFeat1((short) 0xbbaa);
        packet.setFeat2((short) 0xddcc);
        packet.setNodeName(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        });
        packet.updateSignature(pk);

        assertArrayEquals(
                hexFormat.parseHex(publicKey),
                packet.getPublicKey().getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertArrayEquals(
                hexFormat.parseHex("f0e4bc8129259e77a2a248e5b0d85b441b3d7a50f6f24601312ac798a338d63a070effabc179723c74ad371690bba1ee0588d23a6deb7ed42d7b821aa196d605"),
                packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals(48137405, packet.getLatitude());
        assertEquals(11575445, packet.getLongitude());
        assertEquals((short) 0xBBAA, packet.getFeat1());
        assertEquals((short) 0xDDCC, packet.getFeat2());
        assertArrayEquals(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        }, packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
        //Verify signature
        assertTrue(packet.verify());
    }

    /**
     * Test that a full packet with everything enabled gets constructed from scratch with 0x00 replacing publicKey/sig if not signed
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testCreateFromScratchWithAllFeaturesUnsigned() {

        String expectedPayloadBuffer = "0000000000000000000000000000000000000000000000000000000000000000" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "0000000000000000000000000000000000000000000000000000000000000000" + // sig 0-31
                "0000000000000000000000000000000000000000000000000000000000000000" + // sig 32-63
                "F2" + // appdata (is repeater, has lat/long, has feat1, has feat2, has name)
                "bd84de0295a0b000" + // 48.13740596750293 11.575445878381272, Munich Marienplatz
                "aabb" + // feat1
                "ccdd" + // feat2
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "3031"; // "01" name 30-31
        AdvertPacket packet = new AdvertPacket();
        packet.setTimestamp(Instant.ofEpochSecond(1776653321));
        packet.setNodeType(AdvertNodeType.REPEATER);
        packet.setLatitude(48137405);
        packet.setLongitude(11575445);
        packet.setFeat1((short) 0xbbaa);
        packet.setFeat2((short) 0xddcc);
        packet.setNodeName(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        });

        assertNull(packet.getPublicKey());
        assertEquals(Instant.ofEpochSecond(1776653321), packet.getTimestamp());
        assertNull(packet.getSignature());
        assertEquals(AdvertNodeType.REPEATER, packet.getNodeType());
        assertEquals(48137405, packet.getLatitude());
        assertEquals(11575445, packet.getLongitude());
        assertEquals((short) 0xBBAA, packet.getFeat1());
        assertEquals((short) 0xDDCC, packet.getFeat2());
        assertArrayEquals(new byte[]{
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
                (byte) 0x30, (byte) 0x31
        }, packet.getNodeName());
        //Verify reconstitution
        assertArrayEquals(hexFormat.parseHex(expectedPayloadBuffer), packet.getPayloadBuffer());
    }

    /**
     * Test that a minimal packet with too long payload (easiest to test: name 33 bytes) gets rejected
     * todo also add full packet harnessing (packet, path, payload)
     */
    @Test
    public void testRejectTooLongPacket() {
        String payloadBuffer = "010102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f" + // pk
                "0994E569" + // ts / 1776653321 (2026-04-20T02:48:41Z)
                "101112131415161718191a1b1c1d1e1f101112131415161718191a1b1c1d1e1f" + // sig 0-31
                "202122232425262728292a2b2c2d2e2f202122232425262728292a2b2c2d2e2f" + // sig 32-63
                "F2" + // appdata (is repeater, has lat/long, has feat1, has feat2, has name)
                "bd84de0295a0b000" + // 48.13740596750293 11.575445878381272, Munich Marienplatz
                "aabb" + // feat1
                "ccdd" + // feat2
                "30313233343536373839" + // "0123456789" name 00-09
                "30313233343536373839" + // "0123456789" name 10-19
                "30313233343536373839" + // "0123456789" name 20-29
                "303132"; // "01" name 30-32
        assertThrows(ParseErrorException.class, () -> new AdvertPacket(hexFormat.parseHex(payloadBuffer)));
    }

    /**
     * Test packet cryptography against real-world example
     */
    @Test
    public void testPacketCryptography() {
        LinkedHashMap<String, String> packets = new LinkedHashMap<>();
        //Advert for DE-BY-LA Nordfriedhof, direct
        packets.put("8E83AE7F02A711C9", "120039FF455499896B69AC13A788C09FB2403E8D366942F13552B93EFBE98C0C31962005E8698631F4BE67E5F7DE951C9C562DBF65BFF151DB340D8960F4F729B10B807B1D3C92ABC3497B41EE0A7432BD908D7BCFA89A7F8FD9F95B43FFEC5817A8F510CD08920C07E502E817B90044452D42592D4C41204E6F72646672696564686F66");
        //Advert for MUC Mühldorfstraße, direct
        packets.put("D0F9C2874C9BA577", "1200527465E62A155546D1EC87761C34C07E161A10F0932247B38AEAF4005FBB6DEF338E4666AA9616A67E640A9D4C6938F745978AFED3024F2638E7C7B85C50EFC056B8326068D351A3E370958082304146885DB43A876284EF7579E4957A952AC22425E003929660DE02BE2BB1004D5543204DC3BC686C646F726673747261C39F65");
        //Advert for Eschenberg Repeater, via 1 hop
        packets.put("9D60C54D0339BB6E", "1101D86C85453819BF7BAE5522B39C54760DB7146B56B9A352AC5572A809B639BBD10C151FE869F7D6AC602CAAEFA9F8363A8B36A7003D955EEED8E568ECC8003DF0DAEE5A5CCC5EEAE777D3F7EE8B766BD1A26E596358B9AA1EBE9A87DD63EC80CD7B9D112C0192E6AEEA02CC9FCA0045736368656E62657267205265706561746572");
        for (String expectedHash : packets.keySet()) {
            String packetBuffer = packets.get(expectedHash);
            LOG.trace(String.format("Expecting hash %s for packet %s", expectedHash, packetBuffer));
            MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
            assertInstanceOf(AdvertPacket.class, packet);
            byte[] hash = assertDoesNotThrow(packet::getPacketHash);
            LOG.trace(String.format("Got hash %s", hexFormat.formatHex(hash)));
            assertArrayEquals(hexFormat.parseHex(expectedHash), hash);
            //Verify if the cryptographic signature is intact
            assertTrue(((AdvertPacket) packet).verify());
        }
    }
}
