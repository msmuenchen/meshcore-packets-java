package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * Path size information
 *
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L79">upstream code</a>
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md">upstream doc</a>
 */
public enum PathSizeType {

    SIZE_1(0x00, "PATH_1_BYTE_PER_HOP", 1),
    SIZE_2(0x01, "PATH_2_BYTES_PER_HOP", 2),
    SIZE_3(0x02, "PATH_3_BYTES_PER_HOP", 3);

    /*
    Note: We intentionally do not carry definition for 0x03, so that we can throw off a ParseErrorException for not-yet-supported packets.

    @link https://github.com/meshcore-dev/MeshCore/blob/dev/src/Dispatcher.cpp#L167
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathSizeType.class);
    private final int bitmask;
    private final String specName;
    private final int bytesPerHop;

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
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L79">upstream code</a>
     */
    public static PathSizeType fromHeader(byte rawByte) {
        LOG.trace(String.format("Determining path size from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString(((rawByte & 0xFF) >> 6) & 0xFF),8,'0')));
        PathSizeType ret = Stream.of(PathSizeType.values()).filter(el -> (((rawByte & 0xFF) >> 6) & 0xFF) == el.bitmask).limit(1).toList().getFirst();
        LOG.trace("Result: {}", ret.getSpecName());
        return ret;
    }

    @Override
    public String toString() {
        return specName;
    }

    public int getBitmask() {
        return bitmask;
    }

    public String getSpecName() {
        return specName;
    }

    public int getBytesPerHop() {
        return bytesPerHop;
    }
}
