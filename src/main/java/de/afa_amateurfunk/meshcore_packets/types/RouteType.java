package de.afa_amateurfunk.meshcore_packets.types;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * All four routing types of MeshCore
 *
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L14">upstream code</a>
 * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md">upstream doc</a>
 */
public enum RouteType {
    /**
     * This packet gets repeated by all repeaters that have its transport code set to "allow flood" in their configuration
     */
    TRANSPORT_FLOOD(0x00, "ROUTE_TYPE_TRANSPORT_FLOOD"),
    /**
     * This packet gets repeated by all repeaters that have "allow flood *" set in their configuration
     */
    FLOOD(0x01, "ROUTE_TYPE_FLOOD"),
    /**
     * This packet gets repeated only by repeaters in the path chain
     */
    DIRECT(0x02, "ROUTE_TYPE_DIRECT"),
    /**
     * This packet gets repeated by repeaters in the path chain that have its transport code in their configuration => TODO verify this...
     *
     */
    TRANSPORT_DIRECT(0x03, "ROUTE_TYPE_TRANSPORT_DIRECT");
    private static final Logger LOG = LoggerFactory.getLogger(RouteType.class);
    private final int bitmask;
    private final String specName;

    RouteType(int bitmask, String specName) {
        this.bitmask = bitmask;
        this.specName = specName;
    }

    /**
     * Parse the header byte containing the packet version
     *
     * @param rawByte header byte
     * @return VersionType corresponding to the packet version
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L62">upstream code</a>
     */
    public static RouteType fromHeader(byte rawByte) {
        LOG.trace(String.format("Determining route type from %02x / %s", rawByte, StringUtils.leftPad(Integer.toBinaryString(rawByte & 0xFF), 8, '0')));
        //LOG.trace(String.format("%s",StringUtils.leftPad(Integer.toBinaryString((rawByte & 0xFF) & 0x03),8,'0')));
        return Stream.of(RouteType.values()).filter(el -> ((rawByte & 0xFF) & 0x03) == el.bitmask).limit(1).toList().getFirst();
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

    /**
     * Is this routing done using flood?
     *
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L64">upstream code</a>
     */
    public boolean isFlood() {
        return (this == RouteType.FLOOD || this == RouteType.TRANSPORT_FLOOD);
    }

    /**
     * Is this routing direct (i.e. repeaters shall answer if they are targeted, otherwise discard)?
     *
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L65">upstream code</a>
     */
    public boolean isDirect() {
        return (this == RouteType.DIRECT || this == RouteType.TRANSPORT_DIRECT);
    }

    /**
     * Is the routing/handling affected by transport codes?
     *
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L67">upstream code</a>
     */
    public boolean isUsingTransport() {
        return (this == RouteType.TRANSPORT_FLOOD || this == RouteType.TRANSPORT_DIRECT);
    }
}
