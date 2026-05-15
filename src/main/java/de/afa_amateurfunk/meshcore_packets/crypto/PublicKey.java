package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.Util;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Ed25519 public key in Meshcore format
 * <p>
 * MeshCore public keys are ed25519 with the additional assurance the first byte is not 0x00 or 0xFF.
 * Implementation-wise we use BouncyCastle as it allows us to deal with raw byte arrays. That is not needed for public keys,
 * but we go full Bouncycastle here so that our code can be ported to .NET Bouncycastle or, using orlp-ed25519, to C/C++
 * </p>
 *
 */
public class PublicKey {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PublicKey.class);
    /**
     * publicKey the public key, 32 bytes
     */
    protected byte[] publicKey;

    /**
     * construct a PublicKey instance
     *
     * @param publicKey the raw public key
     */
    public PublicKey(byte[] publicKey) {
        LOG.trace(String.format("Attempting to construct public key from buffer %s", Util.hexFormat.formatHex(publicKey)));
        // See https://news.ycombinator.com/item?id=26916544
        if (publicKey.length != 32)
            throw new InvalidParameterException("publicKey must be exactly 32 bytes long");
        // Gates set by MeshCore, see https://github.com/meshcore-dev/MeshCore/blob/main/src/Identity.cpp#L56
        if (publicKey[0] == 0x00)
            throw new InvalidParameterException("first byte of publicKey must not be 0x00");
        if (publicKey[0] == (byte) 0xFF)
            throw new InvalidParameterException("first byte of publicKey must not be 0xFF");

        this.publicKey = publicKey;
        LOG.trace("Created public key {}", Util.hexFormat.formatHex(this.publicKey));
    }

    /**
     * getter for public key field
     *
     * @return public key
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * verify if the signature of a message is valid
     *
     * @param message   message to be verified
     * @param signature signature of the message
     * @return true if signature matches, false if not
     */
    public boolean verifySignature(byte[] message, byte[] signature) {
        LOG.trace(String.format("Verifying if message %s was signed by PK %s / signature %s", Util.hexFormat.formatHex(message), Util.hexFormat.formatHex(this.publicKey), Util.hexFormat.formatHex(signature)));
        if (signature.length != 64)
            throw new InvalidParameterException("Signature must be 64 bytes in length");

        boolean ret = Ed25519.verify(signature, 0, publicKey, 0, message, 0, message.length);
        LOG.trace(String.format("Signature verification: %b", ret));
        return ret;
    }

    @Override
    public String toString() {
        return "PublicKey{" +
                "publicKey=" + Util.hexFormat.formatHex(publicKey) +
                '}';
    }

    /**
     * Equality check
     *
     * @param obj the reference object with which to compare.
     * @return true if key is equal, false if not
     */
    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) {
            LOG.trace("Equality check failed, expected PublicKey, but got null.");
            return false;
        }
        LOG.trace(String.format("Attempting equality comparison of %s with %s", this, obj));
        if (!(obj instanceof PublicKey other)) {
            LOG.trace(String.format("Equality check failed, expected PublicKey, but got %s.", obj.getClass().getName()));
            return false;
        }
        if (!Arrays.equals(this.publicKey, other.publicKey)) {
            LOG.trace(String.format("Equality check failed, expected key %s, but got %s.", Util.hexFormat.formatHex(this.publicKey), Util.hexFormat.formatHex(other.publicKey)));
            return false;
        }
        return true;
    }

}
