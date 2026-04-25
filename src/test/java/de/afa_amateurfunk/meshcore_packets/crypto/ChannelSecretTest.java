package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ChannelSecret
 *
 * @see ChannelSecret
 */
public class ChannelSecretTest extends AbstractLoggingTest {
    Map<String, byte[]> initialEntries = Map.ofEntries(
            Map.entry("#test", hexFormat.parseHex("9cd8fcf22a47333b591d96a2b848b73f")),
            Map.entry("#hamradio", hexFormat.parseHex("83c8b01997654265938da8765cbc7db9")),
            Map.entry("#freiburg", hexFormat.parseHex("ec1c88f03aad858a6d74ec36ac04c420")),
            Map.entry("#flachwitze", hexFormat.parseHex("ec98cd8821fada7fe21ff9e1df6c619f")),
            Map.entry("#netzstatus", hexFormat.parseHex("e84d5ce15046d5a7f69db3a40f604851")),
            Map.entry("#aurora-alert", hexFormat.parseHex("e823127951fc6a425abe0c65ac07b323")),
            Map.entry("Public", ChannelSecret.publicChannelSecret)
    );

    /**
     * Clean the static list before each test to ensure a pre-defined test suite
     */
    @BeforeEach
    public void cleanSecretsList() {
        ChannelSecret.knownSecrets = new LinkedHashMap<>(initialEntries);
    }

    /**
     * Test that secrets need to have a # prefix
     */
    @Test
    public void testRejectNoHash() {
        assertThrows(InvalidParameterException.class, () -> ChannelSecret.getHashtagChannelSecret("foo"));
    }

    /**
     * Test correct generation of secrets with a list of known channel names
     */
    @Test
    public void testSecretGeneration() {
        //Reset the list to blank slate
        ChannelSecret.knownSecrets = new LinkedHashMap<>(Map.ofEntries(
                Map.entry("Public", ChannelSecret.publicChannelSecret)
        ));
        for (Map.Entry<String, byte[]> entry : initialEntries.entrySet()) {
            //We cannot test that one. No idea which cleartext that derives from
            if (entry.getKey().equals("Public"))
                continue;
            assertArrayEquals(entry.getValue(), ChannelSecret.getHashtagChannelSecret(entry.getKey()));
        }
    }

    /**
     * Test register and lookup of a channel where we only know the secret (say, a private channel)
     * Disabled for now, see <a href="https://github.com/assertj/assertj/issues/2165">assertj bug 2165</a>
     */
    @Test
    @Disabled
    public void testRegisterBySecret() {
        String secret = "3cae16fd067ba9c32a98be22e9b98525"; // #ping
        assertDoesNotThrow(() -> ChannelSecret.registerChannelSecret(hexFormat.parseHex(secret)));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("3c"))).contains(Map.entry("3cae16fd067ba9c32a98be22e9b98525", hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525")));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("3cae"))).containsExactly(Map.entry("3cae16fd067ba9c32a98be22e9b98525", hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525")));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525"))).containsExactly(Map.entry("3cae16fd067ba9c32a98be22e9b98525", hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525")));
    }

    /**
     * Test immediate register (might be used by an observer app filling in a bunch of known channel names at startup)
     * Disabled for now, see <a href="https://github.com/assertj/assertj/issues/2165">assertj bug 2165</a>
     */
    @Test
    @Disabled
    public void testRegisterByName() {
        String channelName = "#ping";
        assertDoesNotThrow(() -> ChannelSecret.registerChannelSecret(channelName));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("3c"))).contains(Map.entry("#ping", hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525")));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("3cae"))).containsExactly(Map.entry("#ping", hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525")));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525"))).containsExactly(Map.entry("#ping", hexFormat.parseHex("3cae16fd067ba9c32a98be22e9b98525")));
    }

    /**
     * Test lookup of multiple elements matching a given prefix
     * Disabled for now, see <a href="https://github.com/assertj/assertj/issues/2165">assertj bug 2165</a>
     */
    @Test
    @Disabled
    public void testLookupMultiple() {
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("ec"))).contains(Map.entry("#freiburg", hexFormat.parseHex("ec1c88f03aad858a6d74ec36ac04c420")));
        assertThat(ChannelSecret.lookupChannelSecret(hexFormat.parseHex("ec"))).contains(Map.entry("#flachwitze", hexFormat.parseHex("ec98cd8821fada7fe21ff9e1df6c619f")));
    }
}
