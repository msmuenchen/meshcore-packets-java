package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Ed25519 private key in Meshcore format
 * <p>MeshCore uses orlp-ed25519, which uses NaCl's private key format of a || RH. This saves one round of SHA512 at load.</p>
 *
 * @see <a href="https://blog.mozilla.org/warner/2011/11/29/ed25519-keys/">Brian Warner on ed25519 keys</a>
 */
public class PrivateKey {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PrivateKey.class);
    /**
     * randomness generator, exposed to allow overriding it in unittests
     */
    protected static Random random = new SecureRandom();
    /**
     * the private key (64 bytes, a || RH)
     */
    protected byte[] privateKey;
    /**
     * seed (may be null if this represents a private key only loaeed from a || RH - cannot be calculated back!)
     */
    protected byte[] seed;
    /**
     * derived public key
     */
    protected PublicKey publicKey;

    /**
     * Construct a random private key
     */
    public PrivateKey() {
        byte[] seed = new byte[32];
        random.nextBytes(seed);
        LOG.trace("Attempting to create private key by random seed {}", Util.hexFormat.formatHex(seed));
        loadSeed(seed);
        this.publicKey = new PublicKey(Ed25519.derive_pubkey_orlp(this.privateKey));
        LOG.trace("Created new private key {}", Util.hexFormat.formatHex(this.privateKey));
    }

    /**
     * construct a PrivateKey instance
     *
     * @param privateKey raw key material
     */
    public PrivateKey(byte[] privateKey) {
        LOG.trace(String.format("Attempting to construct private key from buffer %s", Util.hexFormat.formatHex(privateKey)));
        if (privateKey.length == 64)
            loadOrlp(privateKey);
        else if (privateKey.length == 32)
            loadSeed(privateKey);
        else
            throw new InvalidParameterException("privateKey must be exactly 32 (seed) or 64 (orlp) bytes long");

        this.publicKey = new PublicKey(Ed25519.derive_pubkey_orlp(this.privateKey));
        LOG.trace("Created private key {}", Util.hexFormat.formatHex(this.privateKey));
    }

    /**
     * initialize with a 32-byte long seed
     *
     * @param seed 32 random bytes
     */
    private void loadSeed(byte[] seed) {
        if (seed.length != 32)
            throw new InvalidParameterException("seed for privateKey must be exactly 32 bytes long");
        this.seed = seed;
        this.privateKey = Ed25519.seed_to_orlp(this.seed);
    }

    private void loadOrlp(byte[] privateKey) {
        if (privateKey.length != 64)
            throw new InvalidParameterException("orlp-style privateKey must be exactly 64 bytes long");
        this.seed = null; //cannot recover seed, as orlp = sha512(seed)
        this.privateKey = privateKey;
    }

    /**
     * get the associated public key
     *
     * @return public key instance
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * get private key
     *
     * @return private key
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * sign a message with this key
     *
     * @param message message to be signed
     * @return signature
     */
    public byte[] sign(byte[] message) {
        byte[] ret = new byte[64];
        LOG.trace("Attempting to sign message {}", Util.hexFormat.formatHex(message));
        Ed25519.sign_orlp(ret, message, this.privateKey);
        LOG.trace("Signed message, signature {}", Util.hexFormat.formatHex(ret));
        return ret;
    }

    @Override
    public String toString() {
        return "PrivateKey{" +
                "seed=" + (seed == null ? "null" : Util.hexFormat.formatHex(seed)) +
                ", privateKey=" + Util.hexFormat.formatHex(privateKey) +
                ", publicKey=" + publicKey +
                '}';
    }

    public byte[] getSeed() {
        return seed;
    }
}
