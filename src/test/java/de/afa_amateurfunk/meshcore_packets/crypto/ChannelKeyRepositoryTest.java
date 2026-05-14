package de.afa_amateurfunk.meshcore_packets.crypto;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import de.afa_amateurfunk.meshcore_packets.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ChannelKey
 *
 * @see ChannelKey
 */
public class ChannelKeyRepositoryTest extends AbstractLoggingTest {
    /**
     * A list of random channel names based on cities, activities and regions. These may or may not exist in real life.
     */
    public static List<ChannelKey> testEntries = List.of(
            new ChannelKey("#test"),
            new ChannelKey("#hamradio"),
            new ChannelKey("#freiburg"),
            new ChannelKey("#flachwitze"),
            new ChannelKey("#netzstatus"),
            new ChannelKey("#aurora-alert"),
            new ChannelKey("#ergoldsbach"),
            new ChannelKey("#ergolding"),
            new ChannelKey("#muenchen"),
            new ChannelKey("#munich"),
            new ChannelKey("#region-muc"),
            new ChannelKey("#bot"),
            new ChannelKey("#bayern"),
            new ChannelKey("#landshut"),
            new ChannelKey("#warnings"),
            new ChannelKey("#qrv"),
            new ChannelKey("#chat"),
            new ChannelKey("#isarmesh"),
            new ChannelKey("#sos"),
            new ChannelKey("#blackout"),
            new ChannelKey("#koeln"),
            new ChannelKey("#cologne"),
            new ChannelKey("#berlin"),
            new ChannelKey("#dortmund"),
            new ChannelKey("#hamburg"),
            new ChannelKey("#stuttgart"),
            new ChannelKey("#zuerich"),
            new ChannelKey("#nuernberg"),
            new ChannelKey("#hof"),
            new ChannelKey("#frankfurt"),
            new ChannelKey("#mainz"),
            new ChannelKey("#amsterdam"),
            new ChannelKey("#paris"),
            new ChannelKey("#london"),
            new ChannelKey("#nyc"),
            new ChannelKey("#newyork"),
            new ChannelKey("#boston"),
            new ChannelKey("#washington"),
            new ChannelKey("#zagreb"),
            new ChannelKey("#rijeka"),
            new ChannelKey("#bordeaux"),
            new ChannelKey("#nrw"),
            new ChannelKey("#bremen"),
            new ChannelKey("#ingolstadt"),
            new ChannelKey("#regensburg"),
            new ChannelKey("#rosenheim"),
            new ChannelKey("#freilassing"),
            new ChannelKey("#passau"),
            new ChannelKey("#prague"),
            new ChannelKey("#wien"),
            new ChannelKey("#vienna"),
            new ChannelKey("#lyon"),
            new ChannelKey("#badtoelz"),
            new ChannelKey("#dingolfing"),
            new ChannelKey("#deggendorf"),
            new ChannelKey("#eggenfelden"),
            new ChannelKey("#neumarkt"),
            new ChannelKey("#niederbayern"),
            new ChannelKey("#oberbayern"),
            new ChannelKey("#franken"),
            new ChannelKey("#oberpfalz"),
            new ChannelKey("#saarland"),
            new ChannelKey("#saarbruecken"),
            new ChannelKey("#minden"),
            new ChannelKey("#heidelberg"),
            new ChannelKey("#hildesheim"),
            new ChannelKey("#wacken"),
            new ChannelKey("#brandenburg"),
            new ChannelKey("#sachsen"),
            new ChannelKey("#sachsenanhalt"),
            new ChannelKey("#redcross"),
            new ChannelKey("#madrid"),
            new ChannelKey("#barcelona"),
            new ChannelKey("#spain"),
            new ChannelKey("#split"),
            new ChannelKey("#ramersdorf"),
            new ChannelKey("#bremerhaven"),
            new ChannelKey("#bawue"),
            new ChannelKey("#schleswigholstein"),
            new ChannelKey("#sports"),
            new ChannelKey("#soccer"),
            new ChannelKey("#golf"),
            new ChannelKey("#tennis"),
            new ChannelKey("#cricket"),
            new ChannelKey("#nfl"),
            new ChannelKey("#bowling"),
            new ChannelKey("#cats"),
            new ChannelKey("#dogs"),
            new ChannelKey("#beer"),
            new ChannelKey("#wine"),
            new ChannelKey("#aachen"),
            new ChannelKey("#linz"),
            new ChannelKey("#isar"),
            new ChannelKey("#innsbruck"),
            new ChannelKey("#bratislava"),
            new ChannelKey("#ljubljana"),
            new ChannelKey("#laibach"),
            new ChannelKey("#freising"),
            new ChannelKey("#heiligendamm"),
            new ChannelKey("#koblenz"),
            new ChannelKey("#farming"),
            new ChannelKey("#bahn"),
            new ChannelKey("#elmau"),
            new ChannelKey("#stpauli"),
            new ChannelKey("#fcbayern"),
            new ChannelKey("#wolfsburg"),
            new ChannelKey("#kaiserslautern"),
            new ChannelKey("#bvb"),
            new ChannelKey("#tsv1860"),
            new ChannelKey("#premierleague"),
            new ChannelKey("#bundesliga"),
            new ChannelKey("#spongebob"),
            new ChannelKey("#bosnia"),
            new ChannelKey("#croatia"),
            new ChannelKey("#slovenia"),
            new ChannelKey("#geretsried"),
            new ChannelKey("#germering"),
            new ChannelKey("#zorneding"),
            new ChannelKey("#erding"),
            new ChannelKey("#dorfen"),
            new ChannelKey("#augsburg"),
            new ChannelKey("#wuerzburg"),
            new ChannelKey("#wiesbaden"),
            new ChannelKey("#luxembourg"),
            new ChannelKey("#maastricht"),
            new ChannelKey("#antwerp"),
            new ChannelKey("#magdeburg"),
            new ChannelKey("#luebeck"),
            new ChannelKey("#cottbus"),
            new ChannelKey("#dresden"),
            new ChannelKey("#leipzig"),
            new ChannelKey("#coburg"),
            new ChannelKey("#fuerth"),
            ChannelKeyRepository.publicChannelKey
    );

    /**
     * Clean the static list before each test to ensure a pre-defined test suite
     */
    @BeforeEach
    public void cleanKeysList() {
        ChannelKeyRepository.knownKeys = new LinkedList<>();
    }

    /**
     * Test register and lookup of a channel where we only know the key (say, a private channel)
     */
    @Test
    public void testRegisterPrivateChannel() {
        String key = "000102030405060708090a0b0c0d0e0f";
        assertDoesNotThrow(() -> ChannelKeyRepository.registerPrivateChannel("private channel", hexFormat.parseHex(key)));
        ChannelKey k = new ChannelKey("private channel", Util.hexFormat.parseHex(key));
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("be"))).containsExactly(k);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("be45"))).containsExactly(k);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("be45cb2605bf36bebde684841a28f0fd43c69850a3dce5fedba69928ee3a8991"))).containsExactly(k);
    }

    /**
     * Test immediate register (might be used by an observer app filling in a bunch of known channel names at startup)
     */
    @Test
    public void testRegisterByName() {
        String channelName = "#ping";
        assertDoesNotThrow(() -> ChannelKeyRepository.registerHashtagChannel(channelName));
        ChannelKey k = new ChannelKey("#ping");
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("28"))).containsExactly(k);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("2885"))).containsExactly(k);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("2885d962dbd452e6403c88a7d02c93a7e4dadc5d1733bafe84a298197cfb27c9"))).containsExactly(k);
    }

    /**
     * Test immediate register not adding a channel key it already knows
     */
    @Test
    public void testRegisterByNameDeduplicate() {
        String channelName = "#ping";

        assertDoesNotThrow(() -> ChannelKeyRepository.registerChannelKey(new ChannelKey(channelName)));
        assertDoesNotThrow(() -> ChannelKeyRepository.registerChannelKey(new ChannelKey(channelName)));

        assertEquals(1, ChannelKeyRepository.getKnownKeys().size());

        ChannelKey k = new ChannelKey(channelName);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("28"))).containsExactly(k);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("2885"))).containsExactly(k);
        assertThat(ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("2885d962dbd452e6403c88a7d02c93a7e4dadc5d1733bafe84a298197cfb27c9"))).containsExactly(k);
    }

    /**
     * test registering of multiple keys
     */
    @Test
    public void testRegisterMultipleChannels() {
        // First, load our test dataset
        for (ChannelKey k : testEntries) {
            ChannelKeyRepository.registerChannelKey(k);
        }
        assertEquals(testEntries.size(), ChannelKeyRepository.getKnownKeys().size());

        // Add a private channel
        String key = "000102030405060708090a0b0c0d0e0f";
        assertDoesNotThrow(() -> ChannelKeyRepository.registerPrivateChannel("private channel", hexFormat.parseHex(key)));
        assertEquals(testEntries.size() + 1, ChannelKeyRepository.getKnownKeys().size());
        // Adding the private channel again should not change the size of the list (=prevent duplication)
        assertDoesNotThrow(() -> ChannelKeyRepository.registerPrivateChannel("private channel", hexFormat.parseHex(key)));
        assertEquals(testEntries.size() + 1, ChannelKeyRepository.getKnownKeys().size());
        // Adding the private channel with a new key should change the name
        assertDoesNotThrow(() -> ChannelKeyRepository.registerPrivateChannel("private channel 2", hexFormat.parseHex(key)));
        assertEquals(testEntries.size() + 1, ChannelKeyRepository.getKnownKeys().size());
        List<ChannelKey> privateChannelKeys = ChannelKeyRepository.lookupChannelsByHash(hexFormat.parseHex("be"));
        assertEquals(1, privateChannelKeys.size());
        assertEquals("private channel 2", privateChannelKeys.getFirst().getName());
        assertArrayEquals(hexFormat.parseHex(key), privateChannelKeys.getFirst().getKey());

        // Add a hashtag channel that is not present in the test dataset
        String channelName = "#foobar";
        assertDoesNotThrow(() -> ChannelKeyRepository.registerHashtagChannel(channelName));
        assertEquals(testEntries.size() + 2, ChannelKeyRepository.getKnownKeys().size());
        // Adding the hashtag channel again should not change the size of the list (=prevent duplication)
        assertDoesNotThrow(() -> ChannelKeyRepository.registerHashtagChannel(channelName));
        assertEquals(testEntries.size() + 2, ChannelKeyRepository.getKnownKeys().size());
    }

    /**
     * Test lookup of multiple elements matching a given prefix
     */
    @Test
    public void testLookupMultiple() {
        for (int i = 0; i < testEntries.size(); i++) {
            ChannelKey k = testEntries.get(i);
            ChannelKeyRepository.registerChannelKey(k);
            assertEquals(i + 1, ChannelKeyRepository.getKnownKeys().size());
        }
        Map<String, List<ChannelKey>> testCases = Map.ofEntries(
                Map.entry("DD", List.of(
                        new ChannelKey("#vienna"),
                        new ChannelKey("#sports"),
                        new ChannelKey("#dresden")
                )),
                Map.entry("11", List.of(
                        new ChannelKey("#oberpfalz"),
                        ChannelKeyRepository.publicChannelKey
                )),
                Map.entry("CA", List.of(
                        new ChannelKey("#bot"),
                        new ChannelKey("#qrv")
                )),
                Map.entry("D0", List.of(
                        new ChannelKey("#blackout"),
                        new ChannelKey("#nrw")
                ))
        );

        for (Map.Entry<String, List<ChannelKey>> testCase : testCases.entrySet()) {
            assertArrayEquals(
                    testCase.getValue().toArray(),
                    ChannelKeyRepository.lookupChannelsByHash(Util.hexFormat.parseHex(testCase.getKey())).toArray()
            );
        }
    }
}
