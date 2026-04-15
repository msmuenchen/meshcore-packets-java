package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * Path size information
 *
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L79">upstream code</a>
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md">upstream doc</a>
 */
public enum PathSizeType {
    /**
     * 1 byte per hop
     */
    SIZE_1(0x00, "PATH_1_BYTE_PER_HOP", 1),
    /**
     * 2 bytes per hop
     */
    SIZE_2(0x01, "PATH_2_BYTES_PER_HOP", 2),
    /**
     * 3 bytes per hop
     */
    SIZE_3(0x02, "PATH_3_BYTES_PER_HOP", 3);

    /*
    Note: We intentionally do not carry definition for 0x03, so that we can throw off a ParseErrorException for not-yet-supported packets.

    @link https://github.com/meshcore-dev/MeshCore/blob/dev/src/Dispatcher.cpp#L167
     */
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathSizeType.class);
    /**
     * bitmask representing this enum's value
     */
    private final int bitmask;
    /**
     * upstream specification name for this enum
     */
    private final String specName;
    /**
     * creature comfort to access the calculation-relevant value (so that we don't have to add +1 to the bitmask like upstream does)
     * <p>note: do not assume that, should upstream implement 0x03, it will represent 4 bytes per hop</p>
     */
    private final int bytesPerHop;

    /**
     * construct a new enum member
     *
     * @param bitmask     input bitmask
     * @param specName    input spec-name
     * @param bytesPerHop input bytes per hop
     */
    PathSizeType(int bitmask, String specName, int bytesPerHop) {
        this.bitmask = bitmask;
        this.specName = specName;
        this.bytesPerHop = bytesPerHop;
    }

    /**
     * Parse the path size byte containing the byte-per-hop information
     *
     * @param rawByte header byte
     * @return PathSizeType corresponding to the packet's specification
     * @throws java.util.NoSuchElementException when an invalid header byte is encountered
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L79">upstream code</a>
     */
    public static PathSizeType fromHeader(byte rawByte) throws NoSuchElementException {
        LOG.trace(String.format("Determining path size from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString(((rawByte & 0xFF) >> 6) & 0xFF),8,'0')));
        PathSizeType ret = Stream.of(PathSizeType.values()).filter(el -> (((rawByte & 0xFF) >> 6) & 0xFF) == el.bitmask).limit(1).toList().getFirst();
        LOG.trace("Result: {}", ret.getSpecName());
        return ret;
    }

    /**
     * pretty-print when someone tries to dump an instance into a string, we fall back to the specification name for recognizability
     *
     * @return spec name
     */
    @Override
    public String toString() {
        return specName;
    }

    /**
     * get bitmask field
     *
     * @return bitmask
     */
    public int getBitmask() {
        return bitmask;
    }

    /**
     * get specName field
     *
     * @return upstream name
     */
    public String getSpecName() {
        return specName;
    }

    /**
     * get bytesPerHop field
     *
     * @return bytes per hop
     */
    public int getBytesPerHop() {
        return bytesPerHop;
    }
}
