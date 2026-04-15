package de.afa_amateurfunk.meshcore_packets;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.payloads.*;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import de.afa_amateurfunk.meshcore_packets.types.RouteType;
import de.afa_amateurfunk.meshcore_packets.types.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HexFormat;


public abstract class MeshcorePacket {
    private static final Logger LOG = LoggerFactory.getLogger(MeshcorePacket.class);
    protected static HexFormat hexFormat = HexFormat.of();
    protected VersionType packetVersion;
    protected PayloadType packetPayloadType;
    protected RouteType packetRouting;
    protected byte[][] transportCodes;
    protected PathInformation packetPathInformation;
    protected byte[] fullData;
    protected byte[] payloadBuffer;

    public MeshcorePacket() {
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
            ret.fullData = buffer;

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
                payloadStart = 5 + 1 + ret.packetPathInformation.getPathBuffer().length;
            } else { // VPR PL [H1..HN]
                if (buffer.length < 2)
                    throw new ParseErrorException("Packet does not contain a path information");
                LOG.trace("Packet is not using transport codes");
                ret.packetPathInformation = new PathInformation(Arrays.copyOfRange(buffer, 1, buffer.length));
                payloadStart = 1 + 1 + ret.packetPathInformation.getPathBuffer().length;

            }
            LOG.trace("Payload start at {} of {}, payload length expected {}", payloadStart, buffer.length, buffer.length - payloadStart);
            /**
             * Discard packets of more than 184 bytes (current definition of MAX_PACKET_PAYLOAD)
             * @link https://github.com/meshcore-dev/MeshCore/blob/dev/src/MeshCore.h#L19
             */
            if (buffer.length - payloadStart > 184) {
                throw new ParseErrorException("Packet has too long payload");
            } else if (buffer.length - payloadStart > 0) {
                ret.payloadBuffer = Arrays.copyOfRange(buffer, payloadStart, buffer.length);
                ret.parsePayload(ret.payloadBuffer);
            }
            LOG.trace("Finished parsing packet: {}", ret);
            return ret;
        } catch (ParseErrorException e) {
            LOG.warn("Failed to parse packet", e);
            throw e;
        }
    }

    public void parsePayload() {
        parsePayload(this.payloadBuffer);
    }

    public abstract void parsePayload(byte[] payloadBuffer);

    public VersionType getPacketVersion() {
        return packetVersion;
    }

    public void setPacketVersion(VersionType packetVersion) {
        this.packetVersion = packetVersion;
    }

    public RouteType getPacketRouting() {
        return packetRouting;
    }

    public void setPacketRouting(RouteType packetRouting) {
        this.packetRouting = packetRouting;
    }

    public PathInformation getPacketPathInformation() {
        return packetPathInformation;
    }

    public void setPacketPathInformation(PathInformation packetPathInformation) {
        this.packetPathInformation = packetPathInformation;
    }

    public PayloadType getPacketPayloadType() {
        return packetPayloadType;
    }

    public void setPacketPayloadType(PayloadType packetPayloadType) {
        this.packetPayloadType = packetPayloadType;
    }

    public byte[][] getTransportCodes() {
        return transportCodes;
    }

    public void setTransportCodes(byte[][] transportCodes) {
        this.transportCodes = transportCodes;
    }

    public byte[] getFullData() {
        return fullData;
    }

    public void setFullData(byte[] fullData) {
        this.fullData = fullData;
    }

    public byte[] getPayloadBuffer() {
        return payloadBuffer;
    }

    public void setPayloadBuffer(byte[] payloadBuffer) {
        this.payloadBuffer = payloadBuffer;
    }

    @Override
    public String toString() {
        return "MeshcorePacket{" +
                "packetVersion=" + packetVersion +
                ", packetPayloadType=" + packetPayloadType +
                ", packetRouting=" + packetRouting +
                (this.packetRouting.isUsingTransport() ? ", transportCodes=" + hexFormat.formatHex(this.transportCodes[0]) + " / " + hexFormat.formatHex(this.transportCodes[1]) : "") +
                ", packetPathInformation=" + packetPathInformation +
                ", fullData=" + (fullData == null ? "null" : hexFormat.formatHex(fullData)) +
                ", payloadBuffer=" + (payloadBuffer == null ? "null" : hexFormat.formatHex(payloadBuffer)) +
                '}';
    }
}
