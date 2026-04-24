package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.AdvertNodeType;
import de.afa_amateurfunk.meshcore_packets.types.ControlPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

/**
 * Control packet of subtype DISCOVER_RESPONSE
 * <p>This packet contains the repeater's public key and the SNR with which the DISCOVER_REQUEST was received.</p>
 */
public class ControlResponsePacket extends ControlPacket {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControlResponsePacket.class);

    /**
     * node type of this node
     */
    protected AdvertNodeType nodeType;
    /**
     * SNR with which this node received the request
     */
    protected Byte snr;
    /**
     * response tag that got reflected from request
     */
    protected byte[] tag;
    /**
     * public key
     */
    protected byte[] publicKey;

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public ControlResponsePacket(byte[] buffer) {
        super();
        parsePayload(buffer);
    }

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public ControlResponsePacket(String buffer) {
        this(hexFormat.parseHex(buffer));
    }

    /**
     * Construct a packet from scratch
     */
    public ControlResponsePacket() {
        super();
        nodeType = null;
        snr = null;
        tag = null;
        publicKey = null;
        subtype = ControlPacketType.DISCOVER_RESPONSE;
    }

    /**
     * parse a payload buffer and set all applicable internal fields
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) {
        /*
        Length guard. Length is either 14 bytes (1 flags, 1 snr, 4 tag, 8 bytes short public key) or 38 bytes (1 flags, 1 snr, 4 tag, 32 bytes full public key)
         */
        if (payloadBuffer.length < 14)
            throw new ParseErrorException("Payload too short");
        else if (payloadBuffer.length > 38)
            throw new ParseErrorException("Payload too long");
        else if (!(payloadBuffer.length == 14 || payloadBuffer.length == 38))
            throw new ParseErrorException("Payload not exactly 14 or 38 bytes long");
        LOG.trace("Attempting to parse buffer of {} bytes as Control, subtype Discover Response payload: '{}'", payloadBuffer.length, hexFormat.formatHex(payloadBuffer));
        ByteBuffer payloadView = ByteBuffer.wrap(payloadBuffer).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        /*
        Guard against type mismatch
         */
        byte flagsByte = payloadView.get();
        subtype = ControlPacketType.fromHeader(flagsByte);
        if (!subtype.equals(ControlPacketType.DISCOVER_RESPONSE))
            throw new ParseErrorException("Tried to construct a ControlDiscoverResponse packet on a different type payload");
        //type is identical to advert
        nodeType = AdvertNodeType.fromHeader(flagsByte);
        //snr byte
        snr = payloadView.get();
        // tag bytes
        tag = new byte[4];
        payloadView.get(tag);
        // public key
        publicKey = new byte[payloadView.remaining()];
        payloadView.get(publicKey);
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
        ByteBuffer ret = ByteBuffer.allocate(38).order(ByteOrder.LITTLE_ENDIAN);
        // First, the flag byte
        byte flagsByte = 0x00;
        flagsByte = (byte) (flagsByte | this.subtype.getBitmask());
        flagsByte = (byte) (flagsByte | nodeType.getIndex());
        ret.put(flagsByte);
        LOG.trace(String.format("Reconstituted flag byte %s", flagsByte));
        // snr
        ret.put(snr);
        LOG.trace(String.format("Reconstituted SNR byte %s", snr));
        //tag
        ret.put(tag);
        LOG.trace(String.format("Reconstituted tag bytes %s", hexFormat.formatHex(tag)));
        // public key
        ret.put(publicKey);
        LOG.trace(String.format("Reconstituted public key %s", hexFormat.formatHex(publicKey)));

        // Before returning, slim down the buffer to what we actually need, aka cut trailing zero-bytes
        byte[] finalPayload = new byte[ret.position()];
        ret.position(0).get(finalPayload);
        LOG.trace(String.format("Reconstituted final payload %s", hexFormat.formatHex(finalPayload)));
        return finalPayload;
    }

    public AdvertNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(AdvertNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Byte getSnr() {
        return snr;
    }

    public void setSnr(Byte snr) {
        this.snr = snr;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        if (publicKey.length != 32 && publicKey.length != 8)
            throw new InvalidParameterException(String.format("input length mismatch: is %d, should be %d", publicKey.length, 32));
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "ControlResponsePacket{" +
                "nodeType=" + nodeType +
                ", snr=" + (snr / 4) + " (" + hexFormat.formatHex(new byte[]{snr}) + ")" +
                ", tag=" + (tag == null ? "null" : hexFormat.formatHex(tag)) +
                ", publicKey=" + (publicKey == null ? "null" : hexFormat.formatHex(publicKey)) +
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
