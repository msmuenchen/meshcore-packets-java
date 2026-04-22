package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.ControlPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

public class ControlRequestPacket extends ControlPacket {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControlRequestPacket.class);
    /**
     * is this request asking for 8-byte prefix of the public key or for full 32 byte in responses?
     */
    protected Boolean prefix_only;
    /**
     * type filter
     */
    protected Byte typeFilter;
    /**
     * request tag to be reflected in responses
     */
    protected byte[] tag;
    /**
     * since-filter
     */
    protected Instant since;

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public ControlRequestPacket(byte[] buffer) {
        super();
        parsePayload(buffer);
    }

    /**
     * Construct a packet from scratch
     */
    public ControlRequestPacket() {
        super();
        prefix_only = null;
        typeFilter = null;
        tag = null;
        since = null;
        subtype = ControlPacketType.DISCOVER_REQUEST;
    }

    /**
     * parse a payload buffer and set all applicable internal fields
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) {
        /*
        Length guard. Length is either 6 bytes (1 flags, 1 filter, 4 tag) or 10 bytes (1 flags, 1 filter, 4 tag, 4 since)
         */
        if (payloadBuffer.length < 6)
            throw new ParseErrorException("Payload too short");
        else if (payloadBuffer.length > 10)
            throw new ParseErrorException("Payload too long");
        else if (!(payloadBuffer.length == 6 || payloadBuffer.length == 10))
            throw new ParseErrorException("Payload not exactly 6 or 10 bytes long");
        LOG.trace("Attempting to parse buffer of {} bytes as Control, subtype Discover Request payload: '{}'", payloadBuffer.length, hexFormat.formatHex(payloadBuffer));
        ByteBuffer payloadView = ByteBuffer.wrap(payloadBuffer).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        /*
        Guard against type mismatch
         */
        byte flagsByte = payloadView.get();
        subtype = ControlPacketType.fromHeader(flagsByte);
        if (!subtype.equals(ControlPacketType.DISCOVER_REQUEST))
            throw new ParseErrorException("Tried to construct a ControlDiscoverRequest packet on a different type payload");
        // prefix_only flag
        prefix_only = ((((flagsByte & 0xFF) & 0x0F) & 0x01) == 0x01);
        //type filter byte
        typeFilter = payloadView.get();
        // tag bytes
        tag = new byte[4];
        payloadView.get(tag);
        //optional since field
        if (payloadView.hasRemaining()) {
            int sinceSecs = payloadView.getInt();
            since = Instant.ofEpochSecond(sinceSecs);
        }
        LOG.trace(String.format("Finished parsing packet, result %s", this));
    }

    /**
     * return the packet's payload buffer representing the current state of the packet
     *
     * @return full byte buffer
     */
    @Override
    public byte[] getPayloadBuffer() {
        //Our maximum payload length
        ByteBuffer ret = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
        // First, the flag byte
        byte flagsByte = 0x00;
        flagsByte = (byte) (flagsByte | this.subtype.getBitmask());
        if (prefix_only)
            flagsByte = (byte) (flagsByte | 0x01);
        ret.put(flagsByte);
        LOG.trace(String.format("Reconstituted flag byte %s", flagsByte));
        // type filter
        ret.put(typeFilter);
        LOG.trace(String.format("Reconstituted type filter byte %s", typeFilter));
        //tag
        ret.put(tag);
        LOG.trace(String.format("Reconstituted tag bytes %s", hexFormat.formatHex(tag)));
        //optional: since
        if (since != null) {
            int sinceSecs = (int) since.getEpochSecond();
            ret.putInt(sinceSecs);
            LOG.trace(String.format("Reconstituted since timestamp %d (%s)", sinceSecs, since.toString()));
        }
        // Before returning, slim down the buffer to what we actually need, aka cut trailing zero-bytes
        byte[] finalPayload = new byte[ret.position()];
        ret.position(0).get(finalPayload);
        LOG.trace(String.format("Reconstituted final payload %s", hexFormat.formatHex(finalPayload)));
        return finalPayload;
    }

    public Boolean getPrefixOnly() {
        return prefix_only;
    }

    public void setPrefixOnly(Boolean prefixOnly) {
        this.prefix_only = prefixOnly;
    }

    public Byte getTypeFilter() {
        return typeFilter;
    }

    public void setTypeFilter(Byte typeFilter) {
        this.typeFilter = typeFilter;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public Instant getSince() {
        return since;
    }

    public void setSince(Instant since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return "ControlRequestPacket{" +
                "prefix_only=" + prefix_only +
                ", typeFilter=" + typeFilter +
                ", tag=" + hexFormat.formatHex(tag) +
                ", since=" + since +
                ", subtype=" + subtype +
                ", packetVersion=" + packetVersion +
                ", packetPayloadType=" + packetPayloadType +
                ", packetRouting=" + packetRouting +
                ", transportCodes=" + (this.packetRouting.isUsingTransport() ?
                hexFormat.formatHex(this.transportCodes[0]) + " / " + hexFormat.formatHex(this.transportCodes[1])
                : "null") +
                ", packetPathInformation=" + packetPathInformation +
                '}';
    }
}
