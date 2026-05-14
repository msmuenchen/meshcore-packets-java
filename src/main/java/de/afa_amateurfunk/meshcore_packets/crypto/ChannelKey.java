package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Objects;


/**
 * Utility class to deal with channel encryption secrets
 * <p>for hashtag channels, these are defined as sha256('#channelname') | trunc (16)</p>
 *
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/companion_protocol.md?plain=1#L356">upstream doc</a>
 */
public class ChannelKey {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ChannelKey.class);

    /**
     * Human readable name of the channel. Directly maps to the key.
     * <p>A channel name can be up to 32 bytes long, see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/examples/companion_radio/DataStore.cpp#L341">upstream code</a>. It seems to be a 0-terminated string per <a href="https://github.com/meshcore-dev/MeshCore/blob/main/examples/companion_radio/MyMesh.cpp#L1680">upstream code</a>.</p>
     * <p>We assume for now that it can be up to 31 bytes in length including the prefix #</p>
     */
    String name;
    /**
     * Channel key to be used as a secret key for HMAC and AES-ECB encryption
     * <p>16 bytes in length</p>
     * <p>Either hardcoded (public channel 8b3387e9c5cdea6ac9e5edbaa115cd72), derived from sha256(name).substring(0,16) or random (private channels)</p>
     *
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/companion_protocol.md?plain=1#L354">upstream docs</a>
     */
    byte[] key;
    /**
     * Channel hash (i.e. hash the secret key to avoid exposing the first byte in cleartext on-air) to be used in packet processing. Normally we only use the first byte, but we keep the full hash around for future proofing
     *
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/payloads.md?plain=1#L233">upstream docs</a>
     */
    byte[] channelHash;

    /**
     * Instantiate the key for a hashtag channel based on channel name only (i.e. hashtag public channels)
     *
     * @param name hashtag channel name, must start with #, 2-31 bytes
     */
    public ChannelKey(String name) {
        LOG.trace(String.format("Attempting to instantiate and derive hashtag channel key for %s", name));
        byte[] channelName = name.getBytes(StandardCharsets.UTF_8);
        // Sanity checks
        if (channelName.length < 2)
            throw new InvalidParameterException("Hashtag channel name must be at least two bytes long (# plus one byte)");
        if (channelName.length > 31)
            throw new InvalidParameterException("Hashtag channel name must be at most 31 bytes long");
        if (channelName[0] != '#')
            throw new InvalidParameterException("Hashtag channel name must start with # character");

        //Store name now that it is validated
        this.name = name;

        // Derive channel's secret key
        Util.sha256.reset();
        this.key = Arrays.copyOfRange(Util.sha256.digest(channelName), 0, 16);

        // Derive channel hash from secret key
        Util.sha256.reset();
        channelHash = Util.sha256.digest(key);

        LOG.trace(String.format("Derived channel key %s and channel identifier hash %02x (%s) for channel name %s", Util.hexFormat.formatHex(key), channelHash[0], Util.hexFormat.formatHex(channelHash), name));
    }

    /**
     * Instantiate the key for a private channel based on a human-readable name and a manually specified secret
     *
     * @param name channel name, 1-31 bytes
     * @param key  secret key, 16 bytes
     */
    public ChannelKey(String name, byte[] key) {
        LOG.trace(String.format("Attempting to instantiate channel key for %s/%s", name, Util.hexFormat.formatHex(key)));
        byte[] channelName = name.getBytes(StandardCharsets.UTF_8);
        // Sanity checks
        if (channelName.length < 1)
            throw new InvalidParameterException("Private channel name must be at least one byte long");
        if (channelName.length > 31)
            throw new InvalidParameterException("Private channel name must be at most 31 bytes long");
        if (key.length != 16)
            throw new InvalidParameterException("Private channel key must be exactly 16 bytes long");
        if (channelName[0] == '#')
            throw new InvalidParameterException("Hashtag channels cannot be defined with their secret");


        //Store name and key now that both are validated
        this.name = name;
        this.key = key;

        // Derive channel hash from secret key
        Util.sha256.reset();
        channelHash = Util.sha256.digest(key);

        LOG.trace(String.format("Derived channel identifier hash %02x (%s) for channel name %s and secret %s", channelHash[0], Util.hexFormat.formatHex(channelHash), name, Util.hexFormat.formatHex(key)));
    }

    /**
     * get human readable name
     *
     * @return name associated with key
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name. Only valid for non-hashtag channels
     *
     * @param name new name, 1-31 bytes
     */
    public void setName(String name) {
        byte[] channelName = name.getBytes(StandardCharsets.UTF_8);
        // Sanity checks
        if (channelName.length < 1)
            throw new InvalidParameterException("New channel name must be at least one byte long");
        if (channelName.length > 31)
            throw new InvalidParameterException("New channel name must be at most 31 bytes long");
        if (channelName[0] == '#')
            throw new InvalidParameterException("Hashtag channel names are immutable");
        this.name = name;
    }

    /**
     * get key
     *
     * @return key in byte form
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * get the full channel hash (hash of the key)
     *
     * @return channel hash
     */
    public byte[] getChannelHash() {
        return channelHash;
    }

    /**
     * get the first N bytes of the channel hash (hash of the key)
     *
     * @param prefixLength length to return
     * @return N bytes of channel hash
     */
    public byte[] getChannelHash(int prefixLength) {
        return Arrays.copyOfRange(channelHash, 0, prefixLength);
    }

    /**
     * Check if this channel is a hashtag channel or a private channel
     *
     * @return true if channel name begins with '#', false otherwise
     */
    public boolean isHashtagChannel() {
        return this.name.startsWith("#");
    }

    @Override
    public String toString() {
        return "ChannelKey{" +
                "name='" + name + '\'' +
                ", key=" + Util.hexFormat.formatHex(key) +
                ", channelHash=" + Util.hexFormat.formatHex(channelHash) +
                '}';
    }

    /**
     * Equality check - base on name and key being equal
     *
     * @param obj the reference object with which to compare.
     * @return true if name and key are equal, false if not
     */
    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) {
            LOG.trace("Equality check failed, expected ChannelKey, but got null.");
            return false;
        }
        LOG.trace(String.format("Attempting equality comparison of %s with %s", this, obj));
        if (!(obj instanceof ChannelKey other)) {
            LOG.trace(String.format("Equality check failed, expected ChannelKey, but got %s.", obj.getClass().getName()));
            return false;
        }
        if (!name.equals(other.name)) {
            LOG.trace(String.format("Equality check failed, expected name %s, but got %s.", name, other.name));
            return false;
        }
        if (!Arrays.equals(key, other.key)) {
            LOG.trace(String.format("Equality check failed, expected key %s, but got %s.", Util.hexFormat.formatHex(key), Util.hexFormat.formatHex(other.key)));
            return false;
        }

        return true;
    }
}
