import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.payloads.ControlPacket;
import de.afa_amateurfunk.meshcore_packets.payloads.RawCustomPacket;
import de.afa_amateurfunk.meshcore_packets.types.RouteType;
import de.afa_amateurfunk.meshcore_packets.types.VersionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class HeaderTests {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HeaderTests.class);

    /**
     * Initialize JUL to log everything so we can see logs of failed test cases
     */
    @BeforeAll
    static void setupLogging() {
        // This does not work, probably because JUL already has been initialized somewhere in the invocation
        //System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.ALL);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }

    /**
     * Testcase for a completely empty packet
     */
    @Test
    void testRejectEmptyPacket() {
        String packetBuffer = "";
        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBuffer));
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) without path information
     */
    @Test
    void testRejectNoPathPacket() {
        String packetBuffer = "3D";
        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBuffer));
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) without path information
     */
    @Test
    void testRejectTooShortPathPacket() {
        String packetBuffer = "3D04AABBCC";
        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBuffer));
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) without transport codes and path information
     */
    @Test
    void testRejectNoTransportCodesNoPathPacket() {
        String packetBuffer = "3C00"; // 00 1111 00
        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBuffer));
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) with transport codes but no path information
     */
    @Test
    void testRejectTransportCodesNoPathPacket() {
        String packetBuffer = "3CAABB"; // 00 1111 00
        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBuffer));
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) with transport codes but too short path information
     */
    @Test
    void testRejectTransportTooShortPathPacket() {
        String packetBuffer = "3CEEFF04AABBCC";
        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBuffer));
    }

    /**
     * Testcase that rejects a packet that is syntactically correct, but has too much payload
     * <p>
     * Max MTU of MeshCore is 255 bytes. Theoretically that would allow for 190 bytes, but we need 4 bytes for transport codes
     */
    @Test
    void testRejectTooLongFloodPacket() {
        String packetBufferBuilder = "3D" + //raw_custom with FLOOD routing
                "3F" + //1 bph, 63 hops
                "00112233445566778899" + // hops 0-9
                "00112233445566778899" + // hops 10-19
                "00112233445566778899" + // hops 20-29
                "00112233445566778899" + // hops 30-39
                "00112233445566778899" + // hops 40-49
                "00112233445566778899" + // hops 50-59
                "001122" + // hops 60-62
                "00112233445566778899" + // payload 00-09
                "00112233445566778899" + // payload 10-19
                "00112233445566778899" + // payload 20-29
                "00112233445566778899" + // payload 30-39
                "00112233445566778899" + // payload 40-49
                "00112233445566778899" + // payload 50-59
                "00112233445566778899" + // payload 60-69
                "00112233445566778899" + // payload 70-79
                "00112233445566778899" + // payload 80-89
                "00112233445566778899" + // payload 90-99
                "00112233445566778899" + // payload 100-109
                "00112233445566778899" + // payload 110-119
                "00112233445566778899" + // payload 120-129
                "00112233445566778899" + // payload 130-139
                "00112233445566778899" + // payload 140-149
                "00112233445566778899" + // payload 150-159
                "00112233445566778899" + // payload 160-169
                "00112233445566778899" + // payload 170-179
                "0011223344"; // payload 180-184

        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBufferBuilder));
    }

    /**
     * Testcase that rejects a packet that is syntactically correct, but has too much payload
     * <p>
     * Again, MTU is 255 bytes. The header occupies 6+63=69 bytes, plus 184 bytes payload this leaves 2 bytes spare in the protocol design.
     */
    @Test
    void testRejectTooLongTransportFloodPacket() {
        String packetBufferBuilder = "3C" + //raw_custom with TRANSPORT_FLOOD routing
                "AABBCCDD" + //TC1 AABB, TC2 CCDD
                "3F" + //1 bph, 63 hops
                "00112233445566778899" + // hops 0-9
                "00112233445566778899" + // hops 10-19
                "00112233445566778899" + // hops 20-29
                "00112233445566778899" + // hops 30-39
                "00112233445566778899" + // hops 40-49
                "00112233445566778899" + // hops 50-59
                "001122" + // hops 60-63
                "00112233445566778899" + // payload 00-09
                "00112233445566778899" + // payload 10-19
                "00112233445566778899" + // payload 20-29
                "00112233445566778899" + // payload 30-39
                "00112233445566778899" + // payload 40-49
                "00112233445566778899" + // payload 50-59
                "00112233445566778899" + // payload 60-69
                "00112233445566778899" + // payload 70-79
                "00112233445566778899" + // payload 80-89
                "00112233445566778899" + // payload 90-99
                "00112233445566778899" + // payload 100-109
                "00112233445566778899" + // payload 110-119
                "00112233445566778899" + // payload 120-129
                "00112233445566778899" + // payload 130-139
                "00112233445566778899" + // payload 140-149
                "00112233445566778899" + // payload 150-159
                "00112233445566778899" + // payload 160-169
                "00112233445566778899" + // payload 170-179
                "0011223344"; // payload 180-185

        assertThrows(ParseErrorException.class, () -> MeshcorePacket.fromString(packetBufferBuilder));
    }

    /**
     * Testcase that accepts a TRANSPORT_FLOOD packet that is syntactically correct and has as much payload as possible
     */
    @Test
    void testAcceptMaxLongTransportFloodPacket() {
        String packetBufferBuilder = "3C" + //raw_custom with TRANSPORT_FLOOD routing
                "AABBCCDD" + //TC1 AABB, TC2 CCDD
                "3F" + //1 bph, 63 hops
                "00112233445566778899" + // hops 0-9
                "00112233445566778899" + // hops 10-19
                "00112233445566778899" + // hops 20-29
                "00112233445566778899" + // hops 30-39
                "00112233445566778899" + // hops 40-49
                "00112233445566778899" + // hops 50-59
                "001122" + // hops 60-63
                "00112233445566778899" + // payload 00-09
                "00112233445566778899" + // payload 10-19
                "00112233445566778899" + // payload 20-29
                "00112233445566778899" + // payload 30-39
                "00112233445566778899" + // payload 40-49
                "00112233445566778899" + // payload 50-59
                "00112233445566778899" + // payload 60-69
                "00112233445566778899" + // payload 70-79
                "00112233445566778899" + // payload 80-89
                "00112233445566778899" + // payload 90-99
                "00112233445566778899" + // payload 100-109
                "00112233445566778899" + // payload 110-119
                "00112233445566778899" + // payload 120-129
                "00112233445566778899" + // payload 130-139
                "00112233445566778899" + // payload 140-149
                "00112233445566778899" + // payload 150-159
                "00112233445566778899" + // payload 160-169
                "00112233445566778899" + // payload 170-179
                "00112233"; // payload 180-183

        assertDoesNotThrow(() -> MeshcorePacket.fromString(packetBufferBuilder));
    }

    /**
     * Testcase that accepts a TRANSPORT_FLOOD packet that is syntactically correct and has as much payload as possible
     */
    @Test
    void testAcceptMaxLongFloodPacket() {
        String packetBufferBuilder = "3D" + //raw_custom with FLOOD routing
                "3F" + //1 bph, 63 hops
                "00112233445566778899" + // hops 0-9
                "00112233445566778899" + // hops 10-19
                "00112233445566778899" + // hops 20-29
                "00112233445566778899" + // hops 30-39
                "00112233445566778899" + // hops 40-49
                "00112233445566778899" + // hops 50-59
                "001122" + // hops 60-63
                "00112233445566778899" + // payload 00-09
                "00112233445566778899" + // payload 10-19
                "00112233445566778899" + // payload 20-29
                "00112233445566778899" + // payload 30-39
                "00112233445566778899" + // payload 40-49
                "00112233445566778899" + // payload 50-59
                "00112233445566778899" + // payload 60-69
                "00112233445566778899" + // payload 70-79
                "00112233445566778899" + // payload 80-89
                "00112233445566778899" + // payload 90-99
                "00112233445566778899" + // payload 100-109
                "00112233445566778899" + // payload 110-119
                "00112233445566778899" + // payload 120-129
                "00112233445566778899" + // payload 130-139
                "00112233445566778899" + // payload 140-149
                "00112233445566778899" + // payload 150-159
                "00112233445566778899" + // payload 160-169
                "00112233445566778899" + // payload 170-179
                "00112233"; // payload 180-183

        assertDoesNotThrow(() -> MeshcorePacket.fromString(packetBufferBuilder));
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) with TRANSPORT_FLOOD routing
     */
    @Test
    void testRouteTransportFlood() {
        String packetBuffer = "3CAABBCCDD00"; // 00 1111 00
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        assertEquals(RawCustomPacket.class, packet.getClass());
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.TRANSPORT_FLOOD, packet.getPacketRouting());
        byte[][] expectedTransportCodes = {{(byte) 0xAA, (byte) 0xBB}, {(byte) 0xCC, (byte) 0xDD}};
        assertArrayEquals(expectedTransportCodes, packet.getTransportCodes());
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) with FLOOD routing
     */
    @Test
    void testRouteFlood() {
        String packetBuffer = "3D00"; // 00 1111 01
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        assertEquals(RawCustomPacket.class, packet.getClass());
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.FLOOD, packet.getPacketRouting());
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) with TRANSPORT_FLOOD routing
     */
    @Test
    void testRouteTransportDirect() {
        String packetBuffer = "3FAABBCCDD00"; // 00 1111 11
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        assertEquals(RawCustomPacket.class, packet.getClass());
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.TRANSPORT_DIRECT, packet.getPacketRouting());
        byte[][] expectedTransportCodes = {{(byte) 0xAA, (byte) 0xBB}, {(byte) 0xCC, (byte) 0xDD}};
        assertArrayEquals(expectedTransportCodes, packet.getTransportCodes());
    }

    /**
     * Testcase for a raw_custom packet (avoid payload parsing attempts) with FLOOD routing
     */
    @Test
    void testRouteDirect() {
        String packetBuffer = "3E00"; // 00 1111 10
        MeshcorePacket packet = MeshcorePacket.fromString(packetBuffer);
        assertEquals(RawCustomPacket.class, packet.getClass());
        assertEquals(VersionType.VER_1, packet.getPacketVersion());
        assertEquals(RouteType.DIRECT, packet.getPacketRouting());
    }

    @Test
    void testControlPacket() {
        // This is a control packet with direct routing asking for repeaters
        String controlRequestRepeaterBuffer = "2e008004937254ec";
        MeshcorePacket controlRequestRepeaterPacket = MeshcorePacket.fromString(controlRequestRepeaterBuffer);
        LOG.trace(controlRequestRepeaterPacket.toString());
        assertEquals(ControlPacket.class, controlRequestRepeaterPacket.getClass());
        assertEquals(VersionType.VER_1, controlRequestRepeaterPacket.getPacketVersion());
    }

    @Test
    void testPathDecode() {
        String size1PacketBuffer = "11046C2D4AE3A954F2735BCC9F530604BFD4BAFE4BAA24963FC42F804BCB87F6FDD340AA620062E75366FD76F67B279B7BB1A41EFD3FCE8A3D4C9002F36B03B69F7237DC005982F9BA9B590CC69470905097BBEE2CCB3CC8DAD1FFD69E5201CA6F6DC1C19182D5490400921E59E502BCD8CE00444230504153204D617868C3B66865";
        MeshcorePacket size1Packet = MeshcorePacket.fromString(size1PacketBuffer);
        LOG.trace(size1Packet.toString());
        String size2PacketBuffer = "094E667516F9E36A39FF2AB0F68B971E851183308BC0CAA44091971EF68B4E956C87CB0A47AA16F36BC7BDB98BCE9F2A1C0B";
        MeshcorePacket size2Packet = MeshcorePacket.fromString(size2PacketBuffer);
        LOG.trace(size2Packet.toString());
    }
}
