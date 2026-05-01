package de.afa_amateurfunk.meshcore_packets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utilities that are (more or less) needed everywhere to avoid having dozens of object instances floating around
 */
public class Util {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    /**
     * library-wide instance of hex formatter
     */
    public static HexFormat hexFormat = HexFormat.of();

    /**
     * SHA256 hasher
     */
    public static MessageDigest sha256;
    /**
     * hmacSHA256 hasher/verifier
     */
    public static Mac hmacSHA256;
    /**
     * AES-ECB no-padding cipher (channel messages)
     */
    public static Cipher aesEcb;

    static {
        try {
            aesEcb = Cipher.getInstance("AES/ECB/NOPADDING");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOG.error("unable to instantiate global AES-ECB instance");
            throw new RuntimeException(e);
        }
        try {
            hmacSHA256 = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("unable to instantiate global hmacSha256 instance");
            throw new RuntimeException(e);
        }
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("unable to instantiate global sha256 instance");
            throw new RuntimeException(e);
        }
    }
}
