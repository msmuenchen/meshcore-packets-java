package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test PrivateKey
 *
 * @see PrivateKey
 */
public class PrivateKeyTest extends AbstractLoggingTest {
    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PrivateKeyTest.class);

    /**
     * Test creating a private key from scratch
     */
    @Test
    public void testCreateRandom() {
        // Override secureRandom by (re)setting the seed to allow us deterministic behavior tests
        PrivateKey.random = new Random(1);

        PrivateKey pk = new PrivateKey();
        assertArrayEquals(hexFormat.parseHex("28c1a395e653682154f3cb3d408717aa9cacf45e14b8bf1cebc31c67f76e5660f163da55845e7b420720b5722ace47879019e7b6a36584aa6091ffc53593343c"), pk.getPrivateKey());
        assertArrayEquals(hexFormat.parseHex("73d51abbd89cb8196f0efb6892f94d68fccc2c35f0b84609e5f12c55dd85aba8"), pk.getSeed());
        assertArrayEquals(hexFormat.parseHex("528170770de5e44acff38be3c6d3ca41b8aafffed1aa81290f7bf5c3d6ccea4c"), pk.getPublicKey().getPublicKey());
    }

    /**
     * Test creating a private key from seed
     */
    @Test
    public void testCreateFromSeed() {
        // Generated on https://cyphr.me/ed25519_tool/ed.html
        String seed = "B9ED499FDEA1A690508165C29C5BB068EC31706D0CE952B472238E61D8CBF6E8";
        String expectedPrivateKey = "60deedc5dc9cfe23dc0149f51157dae3b3c5f7fe2c492f586e4505cb94e0a460568b4ce618a35af1cb29552ce3cfbce7295e2bc9cb8e814112caef8c25d50529";
        String publicKey = "C7D623FD7E673C52F4A22ADAEE2F0ED0A412FB466CBF5EEA3534C341F9EE7ED2";

        PrivateKey pk = new PrivateKey(hexFormat.parseHex(seed));
        assertArrayEquals(hexFormat.parseHex(expectedPrivateKey), pk.getPrivateKey());
        assertArrayEquals(hexFormat.parseHex(seed), pk.getSeed());
        assertArrayEquals(hexFormat.parseHex(publicKey), pk.getPublicKey().getPublicKey());
    }

    /**
     * Test creating a private key from wrong lengths
     */
    @Test
    public void testCreateRejectWrongLengths() {
        // 1 too short for seed
        assertThrows(InvalidParameterException.class, () -> new PrivateKey(hexFormat.parseHex("000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e")));
        // 1 too long for seed
        assertThrows(InvalidParameterException.class, () -> new PrivateKey(hexFormat.parseHex("000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f00")));
        // 1 too short for orlp
        assertThrows(InvalidParameterException.class, () -> new PrivateKey(hexFormat.parseHex("000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e")));
        // 1 too long for orlp
        assertThrows(InvalidParameterException.class, () -> new PrivateKey(hexFormat.parseHex("000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f00")));
    }

    /**
     * Test creating a private key from orlp-style
     */
    @Test
    public void testCreateFromOrlp() {
        // Generated on https://gessaman.com/mc-keygen/
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey));
        assertArrayEquals(hexFormat.parseHex(privateKey), pk.getPrivateKey());
        assertArrayEquals(hexFormat.parseHex(publicKey), pk.getPublicKey().getPublicKey());
        assertNull(pk.getSeed());
    }

    /**
     * Test signing a message
     */
    @Test
    public void testSign() {
        // Generated on https://gessaman.com/mc-keygen/
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String message = "000102030405060708090A0B0C0D0E0F";
        String expectedSignature = "11c18df52a56168934e7ff7f8573facc5975b4ea6f9da7f82f58429f9aa34532b65e4a20131ae3b1a9ea477669ce8ab8ad43efe2f8bf15332af15005803c890e";
        PrivateKey pk = new PrivateKey(hexFormat.parseHex(privateKey));
        byte[] signature = pk.sign(hexFormat.parseHex(message));
        assertArrayEquals(hexFormat.parseHex(expectedSignature), signature);
    }
}
