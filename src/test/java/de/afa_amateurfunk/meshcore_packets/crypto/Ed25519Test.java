package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Test class for our Ed25519 extension to BouncyCastle
 */
public class Ed25519Test extends AbstractLoggingTest {
    /**
     * Test signing a payload with both public and private key
     */
    @Test
    public void testSignWithPublicKey() {
        // Generated on https://gessaman.com/mc-keygen/
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";
        String message = "000102030405060708090A0B0C0D0E0F";
        String expectedSignature = "11c18df52a56168934e7ff7f8573facc5975b4ea6f9da7f82f58429f9aa34532b65e4a20131ae3b1a9ea477669ce8ab8ad43efe2f8bf15332af15005803c890e";

        byte[] signature = new byte[64];
        Ed25519.sign_orlp(signature, hexFormat.parseHex(message), hexFormat.parseHex(publicKey), hexFormat.parseHex(privateKey));
        System.out.println(hexFormat.formatHex(signature));
        assertArrayEquals(hexFormat.parseHex(expectedSignature), signature);
    }

    /**
     * Test signing a payload with only private key
     */
    @Test
    public void testSignWithoutPublicKey() {
        // Generated on https://gessaman.com/mc-keygen/
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String message = "000102030405060708090A0B0C0D0E0F";
        String expectedSignature = "11c18df52a56168934e7ff7f8573facc5975b4ea6f9da7f82f58429f9aa34532b65e4a20131ae3b1a9ea477669ce8ab8ad43efe2f8bf15332af15005803c890e";

        byte[] signature = new byte[64];
        Ed25519.sign_orlp(signature, hexFormat.parseHex(message), hexFormat.parseHex(privateKey));
        System.out.println(hexFormat.formatHex(signature));
        assertArrayEquals(hexFormat.parseHex(expectedSignature), signature);
    }

    /**
     * Test public key derivation
     */
    @Test
    public void testPubkeyDerivation() {
        // Generated on https://gessaman.com/mc-keygen/
        String privateKey = "481541E6CA9CF485D80546440ADD76C18A6C971AE3EC7A1C5F4CA0A37FA0575E0D7780174B8F9AB6FC2384BA85E1A39FEB71EF9728CD41C6D2656FB575D49BA3";
        String publicKey = "1A2BEE0B31567CD8CB799B3B7036C57741F00CA182238F6068331204785910B2";

        assertArrayEquals(hexFormat.parseHex(publicKey), Ed25519.derive_pubkey_orlp(hexFormat.parseHex(privateKey)));
    }

    /**
     * Test derivation from seed
     */
    @Test
    public void testSeedDerivation() {
        // Generated on https://cyphr.me/ed25519_tool/ed.html
        String seed = "B9ED499FDEA1A690508165C29C5BB068EC31706D0CE952B472238E61D8CBF6E8";
        String expectedPrivateKey = "60deedc5dc9cfe23dc0149f51157dae3b3c5f7fe2c492f586e4505cb94e0a460568b4ce618a35af1cb29552ce3cfbce7295e2bc9cb8e814112caef8c25d50529";
        String publicKey = "C7D623FD7E673C52F4A22ADAEE2F0ED0A412FB466CBF5EEA3534C341F9EE7ED2";
        byte[] orlpPrivateKey = Ed25519.seed_to_orlp(hexFormat.parseHex(seed));
        assertArrayEquals(hexFormat.parseHex(expectedPrivateKey), orlpPrivateKey);
        assertArrayEquals(hexFormat.parseHex(publicKey), Ed25519.derive_pubkey_orlp(orlpPrivateKey));
    }
}
