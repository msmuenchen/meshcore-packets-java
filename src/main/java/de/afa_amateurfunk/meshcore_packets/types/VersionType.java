package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * All version types of MeshCore
 *
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L34">upstream code</a>
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md">upstream doc</a>
 */
public enum VersionType {
    /**
     * Version 1 packet
     */
    VER_1(0x00, "PAYLOAD_VER_1");

    /*
    Note: We intentionally do not carry definitions for VER_2 through VER_4, so that we can throw off a ParseErrorException for not-yet-supported types.

    @link https://github.com/meshcore-dev/MeshCore/blob/dev/src/Dispatcher.cpp#L152
     */
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(VersionType.class);
    /**
     * bitmask representing this enum's value
     */
    private final int bitmask;
    /**
     * upstream specification name for this enum
     */
    private final String specName;

    /**
     * construct a new enum member
     *
     * @param bitmask  input bitmask
     * @param specName input spec-name
     */
    VersionType(int bitmask, String specName) {
        this.bitmask = bitmask;
        this.specName = specName;
    }

    /**
     * Parse the header byte containing the packet version
     *
     * @param rawByte header byte
     * @return VersionType corresponding to the packet version
     * @throws java.util.NoSuchElementException when an invalid header byte is encountered
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L77">upstream code</a>
     */
    public static VersionType fromHeader(byte rawByte) throws NoSuchElementException {
        LOG.trace(String.format("Determining version type from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString(((rawByte & 0xFF) >> 6) & 0xFF),8,'0')));
        return Stream.of(VersionType.values()).filter(el -> (((rawByte & 0xFF) >> 6) & 0xFF) == el.bitmask).limit(1).toList().getFirst();
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
}
