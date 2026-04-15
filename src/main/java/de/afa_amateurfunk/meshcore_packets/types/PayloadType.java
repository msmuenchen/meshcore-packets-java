package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * All packet types of MeshCore
 *
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L19">upstream code</a>
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md">upstream doc</a>
 */
public enum PayloadType {

    REQUEST(0x00, "PAYLOAD_TYPE_REQ"),
    RESPONSE(0x01, "PAYLOAD_TYPE_RESPONSE"),
    TEXT_MESSAGE(0x02, "PAYLOAD_TYPE_TXT_MSG"),
    ACK(0x03, "PAYLOAD_TYPE_ACK"),
    ADVERT(0x04, "PAYLOAD_TYPE_ADVERT"),
    GROUP_TEXT(0x05, "PAYLOAD_TYPE_GRP_TXT"),
    GROUP_DATAGRAM(0x06, "PAYLOAD_TYPE_GRP_DATA"),
    ANON_REQUEST(0x07, "PAYLOAD_TYPE_ANON_REQ"),
    PATH(0x08, "PAYLOAD_TYPE_PATH"),
    TRACE(0x09, "PAYLOAD_TYPE_TRACE"),
    MULTIPART(0x0A, "PAYLOAD_TYPE_MULTIPART"),
    CONTROL(0x0B, "PAYLOAD_TYPE_CONTROL"),
    RAW_CUSTOM(0x0F, "PAYLOAD_TYPE_RAW_CUSTOM");
    private static final Logger LOG = LoggerFactory.getLogger(PayloadType.class);
    private final int bitmask;
    private final String specName;

    PayloadType(int bitmask, String specName) {
        this.bitmask = bitmask;
        this.specName = specName;
    }

    /**
     * Parse the header byte containing the packet version
     *
     * @param rawByte header byte
     * @return VersionType corresponding to the packet version
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L77">upstream code</a>
     */
    public static PayloadType fromHeader(byte rawByte) {
        LOG.trace(String.format("Determining payload type from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString(((rawByte & 0xFF) >> 2) & 0xFF),8,'0')));
        return Stream.of(PayloadType.values()).filter(el -> (((rawByte & 0xFF) >> 2) & 0xFF) == el.bitmask).limit(1).toList().getFirst();
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
}
