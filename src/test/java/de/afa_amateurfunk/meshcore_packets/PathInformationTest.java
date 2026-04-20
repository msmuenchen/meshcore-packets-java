package de.afa_amateurfunk.meshcore_packets;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.PathSizeType;
import de.afa_amateurfunk.meshcore_packets.types.PathSizeTypeTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the packet path decoder
 *
 * @see PathInformation
 */
public class PathInformationTest extends AbstractLoggingTest {
    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PathInformationTest.class);
    /**
     * class-wide instance of hex formatter
     */
    protected static HexFormat hexFormat = HexFormat.of();

    /**
     * Test rejection of completely empty packet
     */
    @Test
    void testRejectEmptyPacket() {
        assertThrows(ParseErrorException.class, () -> new PathInformation(new byte[]{}));
    }

    /**
     * Test a zero-hop packet for a 1-byte hop size
     */
    @Test
    void testDecodeZeroHop1Byte() {
        byte[] packetBuffer = hexFormat.parseHex("00");
        PathInformation pi = new PathInformation(packetBuffer);
        assertArrayEquals(new byte[]{}, pi.getPathBuffer());
        assertEquals(0, pi.getHopCount());
        assertEquals(PathSizeType.SIZE_1, pi.getPacketPathSize());
    }

    /**
     * Test a zero-hop packet for a 2-byte hop size
     */
    @Test
    void testDecodeZeroHop2Bytes() {
        byte[] packetBuffer = hexFormat.parseHex("40");
        PathInformation pi = new PathInformation(packetBuffer);
        assertArrayEquals(new byte[]{}, pi.getPathBuffer());
        assertEquals(0, pi.getHopCount());
        assertEquals(PathSizeType.SIZE_2, pi.getPacketPathSize());
    }

    /**
     * Test a zero-hop packet for a 3-byte hop size
     */
    @Test
    void testDecodeZeroHop3Bytes() {
        byte[] packetBuffer = hexFormat.parseHex("80");
        PathInformation pi = new PathInformation(packetBuffer);
        assertArrayEquals(new byte[]{}, pi.getPathBuffer());
        assertEquals(0, pi.getHopCount());
        assertEquals(PathSizeType.SIZE_3, pi.getPacketPathSize());
    }

    /**
     * Test that the reserved size value (0x04) gets rejected
     * <p>The actual reject is already tested in {@link PathSizeTypeTest#testHeaderParseRejectReserved()}, but we also need to test the passthrough</p>
     */
    @Test
    void testRejectReserved() {
        byte[] packetBuffer = hexFormat.parseHex("C0");
        assertThrows(NoSuchElementException.class, () -> new PathInformation(packetBuffer));
    }

    /**
     * Test all possible hop lengths for 1 byte-per-hop
     * <p>Allowed are 0-63 (64 hops). Also automatically tests for buffer overrun issues (read past path length).</p>
     */
    @Test
    void testPathDecoder1Byte() {
        for (int i = 0; i < 64; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x00;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[i + 1];
            byte[] expectedBuffer = new byte[i];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[hop + 1] = (byte) hop;
                expectedBuffer[hop] = (byte) hop;
            }
            PathInformation pi = new PathInformation(buffer);
            assertEquals(i, pi.getHopCount());
            assertEquals(PathSizeType.SIZE_1, pi.getPacketPathSize());
            assertEquals(i, pi.getPathBuffer().length);
            assertArrayEquals(expectedBuffer, pi.getPathBuffer());
            for (int hop = 0; hop < i; hop++) {
                LOG.trace(String.format("Verifying hop %d of %d", hop, i));
                assertArrayEquals(new byte[]{(byte) hop}, pi.getHop(hop));
            }
        }
    }

    /**
     * Test all possible hop lengths for 1 byte-per-hop, guard against too short buffer
     * <p>Allowed are 0-63 (64 hops)</p>
     */
    @Test
    void testRejectPathDecoder1Byte() {
        for (int i = 0; i < 64; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x00;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[i + 1];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[hop + 1] = (byte) hop;
            }
            assertThrows(ParseErrorException.class, () -> new PathInformation(Arrays.copyOfRange(buffer, 0, buffer.length - 1)));
        }
    }

    /**
     * Test all possible hop lengths for 2 bytes-per-hop
     * <p>Allowed are 0-31 (32 hops). Also automatically tests for buffer overrun issues (read past path length).</p>
     */
    @Test
    void testPathDecoder2Bytes() {
        for (int i = 0; i < 32; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x40;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 2) + 1];
            byte[] expectedBuffer = new byte[i * 2];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 2) + 1] = (byte) hop;
                buffer[(hop * 2) + 2] = (byte) hop;
                expectedBuffer[hop * 2] = (byte) hop;
                expectedBuffer[(hop * 2) + 1] = (byte) hop;
            }
            PathInformation pi = new PathInformation(buffer);
            assertEquals(i, pi.getHopCount());
            assertEquals(PathSizeType.SIZE_2, pi.getPacketPathSize());
            assertEquals(i * 2, pi.getPathBuffer().length);
            assertArrayEquals(expectedBuffer, pi.getPathBuffer());
            for (int hop = 0; hop < i; hop++) {
                LOG.trace(String.format("Verifying hop %d of %d", hop, i));
                assertArrayEquals(new byte[]{(byte) hop, (byte) hop}, pi.getHop(hop));
            }
        }
    }

    /**
     * Test all possible hop lengths for 2 bytes-per-hop, guard against too short buffer (too short by 1 byte)
     */
    @Test
    void testRejectPathDecoder2Bytes1Byte() {
        for (int i = 1; i < 32; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x40;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 2) + 1];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 2) + 1] = (byte) hop;
                buffer[(hop * 2) + 2] = (byte) hop;
            }
            assertThrows(ParseErrorException.class, () -> new PathInformation(Arrays.copyOfRange(buffer, 0, buffer.length - 1)));
        }
    }

    /**
     * Test all possible hop lengths for 2 bytes-per-hop, guard against too short buffer (too short by 2 bytes/full hop)
     */
    @Test
    void testRejectPathDecoder2Bytes2Bytes() {
        for (int i = 1; i < 32; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x40;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 2) + 1];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 2) + 1] = (byte) hop;
                buffer[(hop * 2) + 2] = (byte) hop;
            }
            assertThrows(ParseErrorException.class, () -> new PathInformation(Arrays.copyOfRange(buffer, 0, buffer.length - 2)));
        }
    }

    /**
     * Test all possible hop lengths for 3 bytes-per-hop
     * <p>Allowed are 0-21 (22 hops). Also automatically tests for buffer overrun issues (read past path length).</p>
     */
    @Test
    void testPathDecoder3Bytes() {
        for (int i = 0; i < 22; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x80;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 3) + 1];
            byte[] expectedBuffer = new byte[i * 3];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 3) + 1] = (byte) hop;
                buffer[(hop * 3) + 2] = (byte) hop;
                buffer[(hop * 3) + 3] = (byte) hop;
                expectedBuffer[hop * 3] = (byte) hop;
                expectedBuffer[(hop * 3) + 1] = (byte) hop;
                expectedBuffer[(hop * 3) + 2] = (byte) hop;
            }
            PathInformation pi = new PathInformation(buffer);
            assertEquals(i, pi.getHopCount());
            assertEquals(PathSizeType.SIZE_3, pi.getPacketPathSize());
            assertEquals(i * 3, pi.getPathBuffer().length);
            assertArrayEquals(expectedBuffer, pi.getPathBuffer());
            for (int hop = 0; hop < i; hop++) {
                LOG.trace(String.format("Verifying hop %d of %d", hop, i));
                assertArrayEquals(new byte[]{(byte) hop, (byte) hop, (byte) hop}, pi.getHop(hop));
            }
        }
    }

    /**
     * Test all possible hop lengths for 3 bytes-per-hop, guard against too short buffer (too short by 1 byte)
     */
    @Test
    void testRejectPathDecoder3Bytes1Byte() {
        for (int i = 0; i < 22; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x80;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 3) + 1];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 3) + 1] = (byte) hop;
                buffer[(hop * 3) + 2] = (byte) hop;
                buffer[(hop * 3) + 3] = (byte) hop;
            }
            assertThrows(ParseErrorException.class, () -> new PathInformation(Arrays.copyOfRange(buffer, 0, buffer.length - 1)));
        }
    }

    /**
     * Test all possible hop lengths for 3 bytes-per-hop, guard against too short buffer (too short by 2 bytes)
     */
    @Test
    void testRejectPathDecoder3Bytes2Bytes() {
        for (int i = 1; i < 22; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x80;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 3) + 1];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 3) + 1] = (byte) hop;
                buffer[(hop * 3) + 2] = (byte) hop;
                buffer[(hop * 3) + 3] = (byte) hop;
            }
            assertThrows(ParseErrorException.class, () -> new PathInformation(Arrays.copyOfRange(buffer, 0, buffer.length - 2)));
        }
    }

    /**
     * Test all possible hop lengths for 3 bytes-per-hop, guard against too short buffer (too short by 3 bytes/full hop)
     */
    @Test
    void testRejectPathDecoder3Bytes3Bytes() {
        for (int i = 1; i < 22; i++) {
            LOG.trace(String.format("Testing %d hops", i));
            byte basePacket = (byte) 0x80;
            byte finalPacket = (byte) (basePacket | (byte) i);
            byte[] buffer = new byte[(i * 3) + 1];
            buffer[0] = finalPacket;
            for (int hop = 0; hop < i; hop++) {
                buffer[(hop * 3) + 1] = (byte) hop;
                buffer[(hop * 3) + 2] = (byte) hop;
                buffer[(hop * 3) + 3] = (byte) hop;
            }
            assertThrows(ParseErrorException.class, () -> new PathInformation(Arrays.copyOfRange(buffer, 0, buffer.length - 3)));
        }
    }

    /**
     * Test creating a PathInformation from scratch
     */
    @Test
    public void testCreateFromScratch() {
        PathInformation pi = new PathInformation();
        assertEquals(PathSizeType.SIZE_1, pi.getPacketPathSize());
        assertEquals(0, pi.getHopCount());
    }
}
