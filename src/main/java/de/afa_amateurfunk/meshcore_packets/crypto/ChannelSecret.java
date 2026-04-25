package de.afa_amateurfunk.meshcore_packets.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Utility class to deal with channel hashes
 * <p>for hashtag channels, these are defined as sha256('#channelname') | trunc (16)</p>
 *
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/companion_protocol.md?plain=1#L356">upstream doc</a>
 */
public class ChannelSecret {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ChannelSecret.class);
    /**
     * class-wide instance of hex formatter
     */
    protected static HexFormat hexFormat = HexFormat.of();
    /**
     * well-known id
     */
    public static byte[] publicChannelSecret = hexFormat.parseHex("8b3387e9c5cdea6ac9e5edbaa115cd72");
    /**
     * Map to store heard known channel secrets, to be used to ease decryption of incoming packets
     */
    protected static LinkedHashMap<String, byte[]> knownSecrets = new LinkedHashMap<>(Map.of("Public", publicChannelSecret));

    /**
     * Calculate the secret for a hashtag channel
     *
     * @param name hashtag channel name, must start with #
     * @return secret for symmetric encryption
     */
    public static byte[] getHashtagChannelSecret(String name) {
        try {
            LOG.trace(String.format("Attempting to get channel secret for %s", name));
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] channelName = name.getBytes(StandardCharsets.UTF_8);
            if (channelName[0] != '#')
                throw new InvalidParameterException("Hashtag channel name must start with # sign");
            sha256.update(channelName);
            byte[] channelSecret = Arrays.copyOfRange(sha256.digest(), 0, 16);
            LOG.trace(String.format("Returning channel secret %s for channel name %s", hexFormat.formatHex(channelSecret), name));
            registerChannelSecret(channelSecret, name);
            return channelSecret;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerChannelSecret(byte[] channelSecret, String name) {
        LOG.trace(String.format("Registering channel secret %s for %s in lookup list", hexFormat.formatHex(channelSecret), name));
        if (!knownSecrets.containsValue(channelSecret))
            knownSecrets.put(name, channelSecret);
    }

    public static void registerChannelSecret(byte[] channelSecret) {
        if (!knownSecrets.containsValue(channelSecret))
            knownSecrets.put(hexFormat.formatHex(channelSecret), channelSecret);
    }

    public static void registerChannelSecret(String name) {
        if (!knownSecrets.containsKey(name))
            getHashtagChannelSecret(name);
    }

    /**
     * get a list of all secrets (and, where applicable, human-readable names) that match a prefix
     *
     * @param prefix byte prefix to test secrets against
     * @return list of found secrets
     */
    public static LinkedList<Map.Entry<String, byte[]>> lookupChannelSecret(byte[] prefix) {
        LinkedList<Map.Entry<String, byte[]>> ret = new LinkedList<>();
        String prefixAsString = hexFormat.formatHex(prefix);
        for (Map.Entry<String, byte[]> entrySet : knownSecrets.entrySet()) {
            String entrySecretAsString = hexFormat.formatHex(entrySet.getValue());
            if (entrySecretAsString.startsWith(prefixAsString))
                ret.add(entrySet);
        }
        return ret;
    }
}
