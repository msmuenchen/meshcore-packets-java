package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * All packet types of MeshCore
 *
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L19">upstream code</a>
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md">upstream doc</a>
 */
public enum PayloadType {
    /**
     * Payload will be Request
     */
    REQUEST(0x00, "PAYLOAD_TYPE_REQ"),
    /**
     * Payload will be Response
     */
    RESPONSE(0x01, "PAYLOAD_TYPE_RESPONSE"),
    /**
     * Payload will be Text Message
     */
    TEXT_MESSAGE(0x02, "PAYLOAD_TYPE_TXT_MSG"),
    /**
     * Payload will be Acknowledge
     */
    ACK(0x03, "PAYLOAD_TYPE_ACK"),
    /**
     * Payload will be Advert
     */
    ADVERT(0x04, "PAYLOAD_TYPE_ADVERT"),
    /**
     * Payload will be Group Text Message
     */
    GROUP_TEXT(0x05, "PAYLOAD_TYPE_GRP_TXT"),
    /**
     * Payload will be Group Datagram
     */
    GROUP_DATAGRAM(0x06, "PAYLOAD_TYPE_GRP_DATA"),
    /**
     * Payload will be Anonymous Request
     */
    ANON_REQUEST(0x07, "PAYLOAD_TYPE_ANON_REQ"),
    /**
     * Payload will be Path Discovery
     */
    PATH(0x08, "PAYLOAD_TYPE_PATH"),
    /**
     * Payload will be Trace
     */
    TRACE(0x09, "PAYLOAD_TYPE_TRACE"),
    /**
     * Payload will be Multipart
     */
    MULTIPART(0x0A, "PAYLOAD_TYPE_MULTIPART"),
    /**
     * Payload will be Control
     */
    CONTROL(0x0B, "PAYLOAD_TYPE_CONTROL"),
    /*
    0x0C, 0x0D, 0x0E omitted, these are RESERVED
     */
    /**
     * Payload will be Custom
     */
    RAW_CUSTOM(0x0F, "PAYLOAD_TYPE_RAW_CUSTOM");
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PayloadType.class);
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
    PayloadType(int bitmask, String specName) {
        this.bitmask = bitmask;
        this.specName = specName;
    }

    /**
     * Parse the header byte containing the packet version
     *
     * @param rawByte header byte
     * @return VersionType corresponding to the packet version
     * @throws java.util.NoSuchElementException when an invalid header byte is encountered
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L77">upstream code</a>
     */
    public static PayloadType fromHeader(byte rawByte) throws NoSuchElementException {
        LOG.trace(String.format("Determining payload type from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        LOG.trace(String.format("%s", StringUtils.leftPad(Integer.toBinaryString(((rawByte & 0x3C) >> 2) & 0xFF), 8, '0')));
        PayloadType ret = Stream.of(PayloadType.values()).filter(el -> (((rawByte & 0x3C) >> 2) & 0xFF) == el.bitmask).limit(1).toList().getFirst();
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
