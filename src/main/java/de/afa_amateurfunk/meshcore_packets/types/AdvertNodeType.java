package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * Advert packet's node type field in app_data's first byte lower bits
 *
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/payloads.md#node-advertisement">upstream doc</a>
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/dev/src/helpers/AdvertDataHelpers.h">upstream code</a>
 */
public enum AdvertNodeType {
    /**
     * no type specified
     */
    NONE(0, "ADV_TYPE_NONE"),
    /**
     * companion/"chat node"
     * <p>name deviation from spec is intentional, as userland (firmare type) uses "companion"</p>
     */
    COMPANION(1, "ADV_TYPE_CHAT"),
    /**
     * repeater
     */
    REPEATER(2, "ADV_TYPE_REPEATER"),
    /**
     * room server
     */
    ROOMSERVER(3, "ADV_TYPE_ROOM"),
    /**
     * sensor
     */
    SENSOR(4, "ADV_TYPE_SENSOR");
    /*
    5-15 are reserved
     */
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AdvertNodeType.class);
    /**
     * index representing this enum's value - unlike the other types, the type field is not a bitmask but a 4-bit number
     */
    private final int index;
    /**
     * upstream specification name for this enum
     */
    private final String specName;

    /**
     * construct a new enum member
     *
     * @param index    input index
     * @param specName input spec-name
     */
    AdvertNodeType(int index, String specName) {
        this.index = index;
        this.specName = specName;
    }

    /**
     * Parse the app_data flag byte containing the node type information
     *
     * @param rawByte header byte
     * @return AdvertNodeType corresponding to the packet's specification
     * @throws java.util.NoSuchElementException when an invalid header byte is encountered
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/dev/src/helpers/AdvertDataHelpers.h#L7">upstream code</a>
     */
    public static AdvertNodeType fromHeader(byte rawByte) throws NoSuchElementException {
        LOG.trace(String.format("Determining advert node type from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString(((rawByte & 0x0F)) & 0xFF),8,'0')));
        AdvertNodeType ret = Stream.of(AdvertNodeType.values()).filter(el -> (((rawByte & 0x0F)) & 0xFF) == el.index).limit(1).toList().getFirst();
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
     * get numeric node type
     *
     * @return index
     */
    public int getIndex() {
        return index;
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
