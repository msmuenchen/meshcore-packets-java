package de.afa_amateurfunk.meshcore_packets.types;

import de.afa_amateurfunk.meshcore_packets.AbstractLoggingTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathSizeTypeTest extends AbstractLoggingTest {
    /**
     * Test that the data repository is correctly represented
     */
    @Test
    void testDataMapping() {
        // Test that we have exactly three members
        assertEquals(3, PathSizeType.values().length);
        // SIZE_1: bitmask 0x00, PATH_1_BYTE_PER_HOP, representing 1 byte per hop
        assertEquals(0x00, PathSizeType.SIZE_1.getBitmask());
        assertEquals("PATH_1_BYTE_PER_HOP", PathSizeType.SIZE_1.getSpecName());
        assertEquals("PATH_1_BYTE_PER_HOP", PathSizeType.SIZE_1.toString());
        assertEquals(1, PathSizeType.SIZE_1.getBytesPerHop());
        // SIZE_2: bitmask 0x01, PATH_2_BYTES_PER_HOP, representing 2 bytes per hop
        assertEquals(0x01, PathSizeType.SIZE_2.getBitmask());
        assertEquals("PATH_2_BYTES_PER_HOP", PathSizeType.SIZE_2.getSpecName());
        assertEquals("PATH_2_BYTES_PER_HOP", PathSizeType.SIZE_2.toString());
        assertEquals(2, PathSizeType.SIZE_2.getBytesPerHop());
        // SIZE_3: bitmask 0x02, PATH_3_BYTES_PER_HOP, representing 3 bytes per hop
        assertEquals(0x02, PathSizeType.SIZE_3.getBitmask());
        assertEquals("PATH_3_BYTES_PER_HOP", PathSizeType.SIZE_3.getSpecName());
        assertEquals("PATH_3_BYTES_PER_HOP", PathSizeType.SIZE_3.toString());
        assertEquals(3, PathSizeType.SIZE_3.getBytesPerHop());

    }

    /**
     * Test the actual parser with all possible valid cases
     */
    @Test
    void testHeaderParseValidCases() {
        for (int i = 0; i < 3; i++) {
            byte headerByte = (byte) (0x00 | ((byte) i << 6));
            for (int j = 0; j < 64; j++) {
                byte testByte = (byte) (headerByte | j);
                PathSizeType result = PathSizeType.fromHeader(testByte);
                assertEquals(i + 1, result.getBytesPerHop());
            }
        }
    }

    /**
     * Test that the parser rejects invalid cases (bitmask 0x04 is marked RESERVED upstream)
     */
    @Test
    void testHeaderParseRejectReserved() {
        byte headerByte = (byte) (0x00 | ((byte) 0x03 << 6));
        for (int j = 0; j < 64; j++) {
            byte testByte = (byte) (headerByte | j);
            assertThrows(java.util.NoSuchElementException.class, () -> PathSizeType.fromHeader(testByte));
        }
    }
}