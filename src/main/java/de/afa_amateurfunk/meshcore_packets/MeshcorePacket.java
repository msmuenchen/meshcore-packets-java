package de.afa_amateurfunk.meshcore_packets;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.payloads.*;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import de.afa_amateurfunk.meshcore_packets.types.RouteType;
import de.afa_amateurfunk.meshcore_packets.types.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Superclass for all Java representations of a Meshcore packet
 */
public abstract class MeshcorePacket {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(MeshcorePacket.class);
    /**
     * class-wide instance of hex formatter
     */
    protected static HexFormat hexFormat = HexFormat.of();
    /**
     * representation of a packet's version bitfield
     */
    protected VersionType packetVersion;
    /**
     * representation of a packet's payload bitfield
     */
    protected PayloadType packetPayloadType;
    /**
     * representation of a packet's routing bitfield
     */
    protected RouteType packetRouting;
    /**
     * a packet's raw transport codes
     * TODO refactor this once we actually are able to math with this?
     */
    protected byte[][] transportCodes;
    /**
     * representation of a packet's path information (hop size, hop count and hops)
     */
    protected PathInformation packetPathInformation;

    /**
     * empty constructor. Need it to silence Javadoc
     * TODO check if we can actually use this from subclass constructors to initialize all the various fields when constructing a packet from scratch?
     */
    public MeshcorePacket() {
        setPacketVersion(VersionType.VER_1);
        setPacketRouting(RouteType.DIRECT);
        PathInformation pi = new PathInformation();
        setPacketPathInformation(pi);
    }

    /**
     * Try to parse and unserialize a raw hexdump into a MeshcorePacket instance
     *
     * @param hexData raw hex data (e.g. "2e008004937254ec")
     * @return a MeshcorePacket subclass representing this packet
     */
    public static MeshcorePacket fromString(String hexData) {
        return fromBytes(hexFormat.parseHex(hexData));
    }

    /**
     * Try to parse and unserialize a MeshCore packet
     *
     * @param buffer the byte buffer of the packet
     * @return a MeshcorePacket subclass representing this packet
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/dev/src/Dispatcher.cpp#L148">upstream parse logic</a>
     */
    public static MeshcorePacket fromBytes(byte[] buffer) {
        LOG.trace("Attempting to parse packet of {} bytes: '{}'", buffer.length, hexFormat.formatHex(buffer));
        MeshcorePacket ret;
        try {
            //First, check if we have at least one byte for the routing/version/payload header
            if (buffer.length == 0) {
                throw new ParseErrorException("Packet is completely empty");
            }
            byte versionByte = buffer[0];
            VersionType packetVersion = VersionType.fromHeader(versionByte);
            LOG.trace("Parsed packet version: {}", packetVersion.getSpecName());

            PayloadType packetPayloadType = PayloadType.fromHeader(versionByte);
            LOG.trace("Parsed packet type: {}", packetPayloadType.getSpecName());

            RouteType packetRouting = RouteType.fromHeader(versionByte);
            LOG.trace("Parsed route type: {}", packetRouting.getSpecName());

            switch (packetPayloadType) {
                case REQUEST -> {
                    ret = new RequestPacket();
                }
                case RESPONSE -> {
                    ret = new ResponsePacket();
                }
                case TEXT_MESSAGE -> {
                    ret = new TextMessagePacket();
                }
                case ACK -> {
                    ret = new AckPacket();
                }
                case ADVERT -> {
                    ret = new AdvertPacket();
                }
                case GROUP_TEXT -> {
                    ret = new GroupTextPacket();
                }
                case GROUP_DATAGRAM -> {
                    ret = new GroupDatagramPacket();
                }
                case ANON_REQUEST -> {
                    ret = new AnonRequestPacket();
                }
                case PATH -> {
                    ret = new PathPacket();
                }
                case TRACE -> {
                    ret = new TracePacket();
                }
                case MULTIPART -> {
                    ret = new MultipartPacket();
                }
                case CONTROL -> {
                    ret = new ControlPacket();
                }
                case RAW_CUSTOM -> {
                    ret = new RawCustomPacket();
                }
                default -> {
                    throw new ParseErrorException("Unable to parse version header");
                }
            }
            ret.packetVersion = packetVersion;
            ret.packetPayloadType = packetPayloadType;
            ret.packetRouting = packetRouting;

            /*
            If we are using transport codes, there are four bytes (2x uint16) prepended before the path, which begins at index 5
            If not, the path follows directly, at index 1
            In both cases we have to supply the entire remainder packet to the PathInformation constructor that actually parses the path packet because we do not know the length beforehand
             */
            int payloadStart;
            if (ret.packetRouting.isUsingTransport()) { // VPR TC1_1 TC1_2 TC2_1 TC2_2 PL [H1..HN]
                // 6 bytes are required at the very least for a packet that has zero hops
                // Further checks are done in the PathInformation parser
                if (buffer.length < 6)
                    throw new ParseErrorException("Packet too short for transport codes plus path information");
                ret.transportCodes = new byte[2][];
                ret.transportCodes[0] = Arrays.copyOfRange(buffer, 1, 3);
                ret.transportCodes[1] = Arrays.copyOfRange(buffer, 3, 5);
                LOG.trace("Packet is using transport codes {} / {}", hexFormat.formatHex(ret.transportCodes[0]), hexFormat.formatHex(ret.transportCodes[1]));
                ret.packetPathInformation = new PathInformation(Arrays.copyOfRange(buffer, 5, buffer.length));
                payloadStart = 1 + 4 + ret.packetPathInformation.toByteArray().length;
            } else { // VPR PL [H1..HN]
                if (buffer.length < 2)
                    throw new ParseErrorException("Packet does not contain a path information");
                LOG.trace("Packet is not using transport codes");
                ret.packetPathInformation = new PathInformation(Arrays.copyOfRange(buffer, 1, buffer.length));
                payloadStart = 1 + ret.packetPathInformation.toByteArray().length;
            }
            LOG.trace("Payload start at {} of {}, payload length expected {}", payloadStart, buffer.length, buffer.length - payloadStart);
            /**
             * Discard packets of more than 184 bytes (current definition of MAX_PACKET_PAYLOAD)
             * @link https://github.com/meshcore-dev/MeshCore/blob/dev/src/MeshCore.h#L19
             */
            if (buffer.length - payloadStart > 184) {
                throw new ParseErrorException("Packet has too long payload");
            } else if (buffer.length - payloadStart > 0) {
                ret.parsePayload(Arrays.copyOfRange(buffer, payloadStart, buffer.length));
            }
            LOG.trace("Finished parsing packet: {}", ret);
            return ret;
        } catch (ParseErrorException e) {
            LOG.warn("Failed to parse packet", e);
            throw e;
        }
    }

    /**
     * parse a payload buffer and set all applicable internal fields, to be implemented by subclasses
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     */
    public abstract void parsePayload(byte[] payloadBuffer);

    /**
     * return the packet's payload buffer representing the current state of the packet
     *
     * @return full byte buffer
     */
    public abstract byte[] getPayloadBuffer();

    /**
     * getter for packetVersion field
     *
     * @return packetVersion field
     */
    public VersionType getPacketVersion() {
        return packetVersion;
    }

    /**
     * setter for packetVersion field
     *
     * @param packetVersion new value
     */
    public void setPacketVersion(VersionType packetVersion) {
        this.packetVersion = packetVersion;
    }

    /**
     * getter for packetRouting field
     *
     * @return packetRouting field
     */
    public RouteType getPacketRouting() {
        return packetRouting;
    }

    /**
     * setter for packetRouting field
     *
     * @param packetRouting new value
     */
    public void setPacketRouting(RouteType packetRouting) {
        this.packetRouting = packetRouting;
    }

    /**
     * getter for packetPathInformation field
     *
     * @return packetPathInformation field
     */
    public PathInformation getPacketPathInformation() {
        return packetPathInformation;
    }

    /**
     * setter for packetPathInformation field
     *
     * @param packetPathInformation new value
     */
    public void setPacketPathInformation(PathInformation packetPathInformation) {
        this.packetPathInformation = packetPathInformation;
    }

    /**
     * getter for packetPayloadType field
     *
     * @return packetPayloadType field
     */
    public PayloadType getPacketPayloadType() {
        return packetPayloadType;
    }

    /**
     * getter for transportCodes field
     *
     * @return transportCodes field
     */
    public byte[][] getTransportCodes() {
        return transportCodes;
    }

    /**
     * setter for transportCodes field
     *
     * @param transportCodes new value
     */
    public void setTransportCodes(byte[][] transportCodes) {
        this.transportCodes = transportCodes;
    }

    /**
     * for debug output
     *
     * @return string representation of packet's information
     */
    @Override
    public String toString() {
        return "MeshcorePacket{" +
                "packetVersion=" + packetVersion +
                ", packetPayloadType=" + packetPayloadType +
                ", packetRouting=" + packetRouting +
                (this.packetRouting.isUsingTransport() ? ", transportCodes=" + hexFormat.formatHex(this.transportCodes[0]) + " / " + hexFormat.formatHex(this.transportCodes[1]) : "") +
                ", packetPathInformation=" + packetPathInformation +
                '}';
    }

    /**
     * turn the packet into a byte array representation
     *
     * @return byte array ready to transmit on the air
     */
    public byte[] toByteArray() {
        // Allocate 255 bytes aka LoRa MTU to have a safe gate against emitting too large packets
        ByteBuffer ret = ByteBuffer.allocate(255).order(ByteOrder.LITTLE_ENDIAN);
        // First, the version/payload/route byte
        byte vprByte = 0x00;
        vprByte = (byte) (vprByte | (packetVersion.getBitmask() << 6));
        vprByte = (byte) (vprByte | (packetPayloadType.getBitmask() << 2));
        vprByte = (byte) (vprByte | packetRouting.getBitmask());
        ret.put(vprByte);
        LOG.trace(String.format("Reconstituted header byte %02x", vprByte));
        // If transport codes are used, these follow next
        if (packetRouting.isUsingTransport()) {
            ret.put(transportCodes[0]);
            ret.put(transportCodes[1]);
            LOG.trace(String.format("Reconstituted transport codes %s / %s", hexFormat.formatHex(transportCodes[0]), hexFormat.formatHex(transportCodes[1])));
        }
        // Path information
        byte[] pathBuffer = packetPathInformation.toByteArray();
        ret.put(pathBuffer);
        LOG.trace(String.format("Reconstituted path buffer %s", hexFormat.formatHex(pathBuffer)));
        // Payload
        byte[] payloadBuffer = getPayloadBuffer();
        ret.put(payloadBuffer);
        LOG.trace(String.format("Reconstituted payload buffer %s", hexFormat.formatHex(payloadBuffer)));

        // Before returning, slim down the buffer to what we actually need, aka cut trailing zero-bytes
        byte[] finalPacket = new byte[ret.position()];
        ret.position(0).get(finalPacket);
        LOG.trace(String.format("Reconstituted final packet %s", hexFormat.formatHex(finalPacket)));
        return finalPacket;
    }
}
