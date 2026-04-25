package de.afa_amateurfunk.meshcore_packets.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.HexFormat;

/**
 * Ed25519 public key in Meshcore format
 * <p>We essentially wrap {@link java.security.PublicKey} to avoid upper levels (user applications) having to wrangle with ASN.1 on their own.</p>
 */
public class PublicKey {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PublicKey.class);
    /**
     * class-wide instance of hex formatter
     */
    protected static HexFormat hexFormat = HexFormat.of();
    /**
     * the public key, 32 bytes
     */
    protected final byte[] publicKey;
    /**
     * Java crypto PublicKey
     */
    protected final java.security.PublicKey publicKeyObj;

    /**
     * construct a PublicKey instance
     *
     * @param publicKey the raw public key
     */
    public PublicKey(byte[] publicKey) {
        LOG.trace(String.format("Attempting to construct public key from buffer %s", hexFormat.formatHex(publicKey)));
        // See https://news.ycombinator.com/item?id=26916544
        if (publicKey.length != 32)
            throw new InvalidParameterException("publicKey must be exactly 32 bytes long");
        // Gates set by MeshCore, see https://github.com/meshcore-dev/MeshCore/blob/main/src/Identity.cpp#L56
        if (publicKey[0] == 0x00)
            throw new InvalidParameterException("first byte of publicKey must not be 0x00");
        if (publicKey[0] == (byte) 0xFF)
            throw new InvalidParameterException("first byte of publicKey must not be 0xFF");

        this.publicKey = publicKey;
        /*
         * Java is a load of garbage
         * See https://github.com/timbray/blueskidjava/blob/main/src/com/textuality/blueskid/Ed25519.java for the inspiration how to properly deal with this...
         */
        try {
            final KeyFactory kf = KeyFactory.getInstance("Ed25519");
            // BouncyCastle AND Java both want an actual X509 encoded key. No way around that, so construct an ASN.1 wrapper
            // Warning: hot garbage follows
            byte[] asn1Buffer = new byte[44];
            asn1Buffer[0] = 0x30; // Sequence SubjectPublicKeyInfo
            asn1Buffer[1] = 0x2A; // 42 bytes follow
            asn1Buffer[2] = 0x30; // Sequence AlgorithmIdentifier
            asn1Buffer[3] = 0x05; // 5 bytes follow
            asn1Buffer[4] = 0x06; // Object Identifier
            asn1Buffer[5] = 0x03; // 3 bytes follow
            asn1Buffer[6] = 0x2B; // 43 - that's OID 1.3, see https://www.ranecommercial.com/legacy/note161.html - WTF
            asn1Buffer[7] = 0x65; // 101 - that's OID 101 id-edwards-curve-algs, see https://datatracker.ietf.org/doc/html/rfc8410#section-9
            asn1Buffer[8] = 0x70; // 112 - that's OID 112 id-Ed25519
            asn1Buffer[9] = 0x03; // Bit String
            asn1Buffer[10] = 0x21; // 33 bytes follow
            asn1Buffer[11] = 0x00; // 0 bits of padding (apparently there's a possibility of differentiating between "constructed" and "primitive" encoding?) https://datatracker.ietf.org/doc/html/draft-kaliski-asn1-layman-guide-00#name-bit-string
            // Now that we got the header constructed... copy in our public key byte by byte
            System.arraycopy(this.publicKey, 0, asn1Buffer, 12, 32);

            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(asn1Buffer);
            publicKeyObj = kf.generatePublic(keySpec);

            if (!publicKeyObj.getAlgorithm().equals("EdDSA")) {
                throw new Exception("Key type is " + publicKeyObj.getAlgorithm() + ", should be EdDSA.");
            }
        } catch (Exception e) {
            LOG.error("Failed to construct Java public key object out of public key bytes", e);
            throw new RuntimeException(e);
        }
        LOG.trace("Created public key {}", hexFormat.formatHex(this.publicKey));
    }

    /**
     * construct a PublicKey instance
     *
     * @param publicKey the raw public key
     */
    public PublicKey(String publicKey) {
        this(hexFormat.parseHex(publicKey));
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
        LOG.trace(String.format("Verifying if message %s was signed by PK %s / signature %s", hexFormat.formatHex(message), hexFormat.formatHex(this.publicKey), hexFormat.formatHex(signature)));
        if (signature.length != 64)
            throw new InvalidParameterException("Signature must be 64 bytes in length");
        try {
            Signature sig = Signature.getInstance("ed25519");
            sig.initVerify(publicKeyObj);
            sig.update(message);
            boolean ret = sig.verify(signature);
            LOG.trace(String.format("Signature verification: %b", ret));
            return ret;
        } catch (Exception e) {
            LOG.error("Failed to verify signature thanks to exception thrown", e);
            throw new RuntimeException(e);
        }
    }
}
