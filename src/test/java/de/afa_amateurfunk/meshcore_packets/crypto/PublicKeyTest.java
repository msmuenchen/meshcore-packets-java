package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
     * Test verification of a signed payload
     * Disabled until signing works to generate a payload
     */
    @Test
    @Disabled
    public void testVerify() {

    }
}
