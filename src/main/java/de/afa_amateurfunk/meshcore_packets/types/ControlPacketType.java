package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * The two currently known Control subtypes of MeshCore
 *
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/examples/simple_repeater/MyMesh.cpp#L769">upstream code</a>
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/payloads.md#control-data">upstream doc</a>
 */
public enum ControlPacketType {
    /**
     * This packet represents a discovery request
     */
    DISCOVER_REQUEST(0x80, "CTL_TYPE_NODE_DISCOVER_REQ"),
    /**
     * This packet represents a discovery response
     */
    DISCOVER_RESPONSE(0x90, "CTL_TYPE_NODE_DISCOVER_RESP");
    /*
    Other values are currently not assigned, theoretically there would be up to 16 distinct subtypes possible
     */
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControlPacketType.class);
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
    ControlPacketType(int bitmask, String specName) {
        this.bitmask = bitmask;
        this.specName = specName;
    }

    /**
     * Parse the header byte containing the subtype
     *
     * @param rawByte header byte
     * @return ControlPacketType corresponding to the packet version
     */
    public static ControlPacketType fromHeader(byte rawByte) {
        LOG.trace(String.format("Determining control packet subtype from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString((rawByte & 0xF0) & 0xFF),8,'0')));
        ControlPacketType ret = Stream.of(ControlPacketType.values()).filter(el -> ((rawByte & 0xF0) & 0xFF) == el.bitmask).limit(1).toList().getFirst();
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
}
