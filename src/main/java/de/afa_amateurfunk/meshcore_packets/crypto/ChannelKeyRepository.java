package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Repository for ChannelKey instances
 *
 * @see ChannelKey
 */
public class ChannelKeyRepository {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ChannelKeyRepository.class);

    /**
     * well-known id
     */
    public static ChannelKey publicChannelKey = new ChannelKey("Public", Util.hexFormat.parseHex("8b3387e9c5cdea6ac9e5edbaa115cd72"));
    /**
     * Map to store heard known channel keys, to be used to ease decryption of incoming packets. Public channel will register itself from its constructor
     */
    protected static LinkedList<ChannelKey> knownKeys = new LinkedList<>();

    /**
     * Add a ChannelKey to the repository if it isn't present yet
     *
     * @param key ChannelKey to register
     */
    public static void registerChannelKey(ChannelKey key) {
        LOG.trace(String.format("Adding key to repository: %s", key));
        //todo refactor to see what we can do with equals()? do we need to hand-roll a loop or can we just use contains()?
        for (ChannelKey k : knownKeys) {
            if (Arrays.equals(k.getKey(), key.getKey())) {
                LOG.trace("Found key in repository, not adding");
                return;
            }
        }
        knownKeys.add(key);
    }

    /**
     * Ensure that the definition for a given hashtag channel is present in the repository
     *
     * @param name hashtag channel name, must start with #, 2-31 bytes
     */
    public static ChannelKey registerHashtagChannel(String name) {
        LOG.trace(String.format("Adding hashtag channel to repository: %s", name));
        // todo refactor with streams?
        for (ChannelKey k : knownKeys) {
            if (name.equals(k.getName())) {
                LOG.trace("Found key in repository, not adding");
                return k;
            }
        }
        ChannelKey ret = new ChannelKey(name);
        knownKeys.add(ret);
        return ret;
    }

    /**
     * register or update name of a private channel
     *
     * @param name channel name, 1-31 bytes
     * @param key  secret key, 16 bytes
     */
    public static ChannelKey registerPrivateChannel(String name, byte[] key) {
        LOG.trace(String.format("Adding private channel to repository: %s", name));
        // todo refactor with streams?
        for (ChannelKey k : knownKeys) {
            if (Arrays.equals(key, k.getKey())) {
                LOG.trace("Found key in repository, not adding");
                if (!name.equals(k.getName())) {
                    LOG.trace("Name differs, updating name");
                    k.setName(name);
                }
                return k;
            }
        }
        ChannelKey ret = new ChannelKey(name, key);
        knownKeys.add(ret);
        return ret;
    }


    /**
     * get a list of all keys (and, where applicable, human-readable names) where the sha256 of the key matches a prefix
     *
     * @param prefix byte prefix to test keys against
     * @return list of found keys
     */
    public static LinkedList<ChannelKey> lookupChannelsByHash(byte[] prefix) {
        LOG.trace(String.format("Looking up channels by hash: %s", Util.hexFormat.formatHex(prefix)));
        LinkedList<ChannelKey> ret = new LinkedList<>();
        // todo refactor with streams?
        for (ChannelKey k : knownKeys) {
            if (Arrays.equals(prefix, k.getChannelHash(prefix.length))) {
                LOG.trace(String.format("Found key in repository that might match: %s", k));
                ret.add(k);
            }
        }
        return ret;
    }

    /**
     * get a list of all known keys
     */
    public static LinkedList<ChannelKey> getKnownKeys() {
        return knownKeys;
    }
}
