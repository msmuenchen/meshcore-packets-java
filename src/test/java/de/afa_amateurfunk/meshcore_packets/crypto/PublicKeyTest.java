package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for PublicKey
 *
 * @see PublicKey
 */
public class PublicKeyTest extends AbstractLoggingTest {
    /**
     * Test creation of a public key
     */
    @Test
    public void testCreateFromScratch() {
        // Meshcore's official unit-test key
        // https://github.com/meshcore-dev/MeshCore/blob/main/src/Identity.cpp#L69
        byte[] buf = new byte[]{
                (byte) 0x1e, (byte) 0xc7, (byte) 0x71, (byte) 0x75, (byte) 0xb0, (byte) 0x91, (byte) 0x8e, (byte) 0xd2,
                (byte) 0x06, (byte) 0xf9, (byte) 0xae, (byte) 0x04, (byte) 0xec, (byte) 0x13, (byte) 0x6d, (byte) 0x6d,
                (byte) 0x5d, (byte) 0x43, (byte) 0x15, (byte) 0xbb, (byte) 0x26, (byte) 0x30, (byte) 0x54, (byte) 0x27,
                (byte) 0xf6, (byte) 0x45, (byte) 0xb4, (byte) 0x92, (byte) 0xe9, (byte) 0x35, (byte) 0x0c, (byte) 0x10,
        };
        PublicKey pk = new PublicKey(buf);
        assertArrayEquals(buf, pk.getPublicKey());
    }

    /**
     * Test denial of wrong length
     */
    @Test
    public void testCreateDenyLength() {
        // Meshcore's official unit-test key, shortened by 1 byte
        // https://github.com/meshcore-dev/MeshCore/blob/main/src/Identity.cpp#L69
        byte[] buf = new byte[]{
                (byte) 0x1e, (byte) 0xc7, (byte) 0x71, (byte) 0x75, (byte) 0xb0, (byte) 0x91, (byte) 0x8e, (byte) 0xd2,
                (byte) 0x06, (byte) 0xf9, (byte) 0xae, (byte) 0x04, (byte) 0xec, (byte) 0x13, (byte) 0x6d, (byte) 0x6d,
                (byte) 0x5d, (byte) 0x43, (byte) 0x15, (byte) 0xbb, (byte) 0x26, (byte) 0x30, (byte) 0x54, (byte) 0x27,
                (byte) 0xf6, (byte) 0x45, (byte) 0xb4, (byte) 0x92, (byte) 0xe9, (byte) 0x35, (byte) 0x0c
        };
        assertThrows(InvalidParameterException.class, () -> new PublicKey(buf));
    }

    /**
     * Test denial of wrong prefixes
     */
    @Test
    public void testCreateDenyPrefixes() {
        // Meshcore's official unit-test key, shortened by 1 byte
        // https://github.com/meshcore-dev/MeshCore/blob/main/src/Identity.cpp#L69
        byte[] buf = new byte[]{
                (byte) 0x1e, (byte) 0xc7, (byte) 0x71, (byte) 0x75, (byte) 0xb0, (byte) 0x91, (byte) 0x8e, (byte) 0xd2,
                (byte) 0x06, (byte) 0xf9, (byte) 0xae, (byte) 0x04, (byte) 0xec, (byte) 0x13, (byte) 0x6d, (byte) 0x6d,
                (byte) 0x5d, (byte) 0x43, (byte) 0x15, (byte) 0xbb, (byte) 0x26, (byte) 0x30, (byte) 0x54, (byte) 0x27,
                (byte) 0xf6, (byte) 0x45, (byte) 0xb4, (byte) 0x92, (byte) 0xe9, (byte) 0x35, (byte) 0x0c, (byte) 0x10,
        };
        buf[0] = (byte) 0xFF;
        assertThrows(InvalidParameterException.class, () -> new PublicKey(buf));
        buf[0] = (byte) 0x00;
        assertThrows(InvalidParameterException.class, () -> new PublicKey(buf));
    }

    /**
     * Test if creating from all-zeroes is denied
     */
    @Test
    public void testDenyAllZeroes() {
        String publicKey = "0000000000000000000000000000000000000000000000000000000000000000";
        assertThrows(InvalidParameterException.class, () -> new PublicKey(hexFormat.parseHex(publicKey)));
    }

    /**
     * Test verification of a signed payload
     */
    @Test
    public void testVerifySuccess() {
        String message = "000102030405060708090A0B0C0D0E0F";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        String signature = "11c18df52a56168934e7ff7f8573facc5975b4ea6f9da7f82f58429f9aa34532b65e4a20131ae3b1a9ea477669ce8ab8ad43efe2f8bf15332af15005803c890e";
        PublicKey pk = new PublicKey(hexFormat.parseHex(publicKey));
        assertTrue(pk.verifySignature(hexFormat.parseHex(message), hexFormat.parseHex(signature)));
    }

    /**
     * Test verification of a signed payload
     */
    @Test
    public void testVerifyFail() {
        String message = "0F0E0D0C0B0A09080706050403020100";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        String signature = "11c18df52a56168934e7ff7f8573facc5975b4ea6f9da7f82f58429f9aa34532b65e4a20131ae3b1a9ea477669ce8ab8ad43efe2f8bf15332af15005803c890e";
        PublicKey pk = new PublicKey(hexFormat.parseHex(publicKey));
        assertFalse(pk.verifySignature(hexFormat.parseHex(message), hexFormat.parseHex(signature)));
    }
}
