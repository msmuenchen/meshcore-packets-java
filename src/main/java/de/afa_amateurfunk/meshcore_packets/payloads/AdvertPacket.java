package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.AdvertNodeType;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Instant;

/**
 * Advert packet
 *
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/docs/payloads.md#node-advertisement">upstream doc</a>
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/dev/src/helpers/AdvertDataHelpers.h">upstream code</a>
 */
public class AdvertPacket extends MeshcorePacket {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AdvertPacket.class);
    /**
     * public key of node sending the advert, 32 bytes
     */
    protected byte[] publicKey;
    /**
     * timestamp (unix time), 4 bytes
     */
    protected Instant timestamp;
    /**
     * signature, 64 bytes
     * <p>ed25519(pk + ts + appdata)</p>
     */
    protected byte[] signature;
    /**
     * node type
     */
    protected AdvertNodeType nodeType;
    /**
     * latitude, 4 bytes
     */
    protected Integer latitude;
    /**
     * longitude, 4 bytes
     */
    protected Integer longitude;
    /**
     * feat_1 (reserved), 2 bytes
     */
    protected Short feat1;
    /**
     * feat_1 (reserved), 2 bytes
     */
    protected Short feat2;
    /**
     * node name (remainder of PDU)
     */
    protected byte[] nodeName;

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public AdvertPacket(byte[] buffer) {
        super();
        packetPayloadType = PayloadType.ADVERT;
        parsePayload(buffer);
    }

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public AdvertPacket(String buffer) {
        this(hexFormat.parseHex(buffer));
    }

    /**
     * Construct a packet from scratch
     */
    public AdvertPacket() {
        super();
        packetPayloadType = PayloadType.ADVERT;
        publicKey = new byte[]{};
        timestamp = null;
        signature = new byte[]{};
        latitude = null;
        longitude = null;
        feat1 = null;
        feat2 = null;
        nodeName = null;
    }

    /**
     * parse a payload buffer and set all applicable internal fields
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     * @throws ParseErrorException      in case of malformed packet
     * @throws BufferUnderflowException in case of too short packet
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) throws ParseErrorException, BufferUnderflowException {
        /*
        Minimum length is at least 101 bytes (pk + ts + sig + app_data flag byte
         */
        if (payloadBuffer.length < 32 + 4 + 64 + 1)
            throw new ParseErrorException("Payload buffer too short");
        /* Global maximum limit */
        if (payloadBuffer.length > 32 + 4 + 64 + 1 + 8 + 2 + 2 + 32)
            throw new ParseErrorException("Payload buffer too long");
        LOG.trace("Attempting to parse buffer of {} bytes as Advert payload: '{}'", payloadBuffer.length, hexFormat.formatHex(payloadBuffer));
        ByteBuffer payloadView = ByteBuffer.wrap(payloadBuffer).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        publicKey = new byte[32];
        payloadView.get(publicKey, 0, 32);
        int timestampSecs = payloadView.getInt();
        timestamp = Instant.ofEpochSecond(timestampSecs);
        signature = new byte[64];
        payloadView.get(signature, 0, 64);
        byte flagsByte = payloadView.get();
        nodeType = AdvertNodeType.fromHeader(flagsByte);
        /*
         * Unpack all the potential app_data fields. In case there isn't enough buffer, Java itself will barf
         * @see https://github.com/meshcore-dev/MeshCore/blob/dev/src/helpers/AdvertDataHelpers.h#L14
         */
        boolean hasLatLong = ((flagsByte & 0xF0) & 0x10) == 0x10;
        boolean hasFeat1 = ((flagsByte & 0xF0) & 0x20) == 0x20;
        boolean hasFeat2 = ((flagsByte & 0xF0) & 0x40) == 0x40;
        boolean hasName = ((flagsByte & 0xF0) & 0x80) == 0x80;
        LOG.trace(String.format("Flags byte: type %s, hasLatLong: %b, hasFeat1: %b, hasFeat2: %b, hasName: %b", nodeType, hasLatLong, hasFeat1, hasFeat2, hasName));
        if (hasLatLong) {
            LOG.trace("Unpacking latitude / longitude");
            latitude = payloadView.getInt();
            longitude = payloadView.getInt();
        } else {
            LOG.trace("No latitude / longitude supplied");
            latitude = null;
            longitude = null;
        }
        if (hasFeat1) {
            LOG.trace("Unpacking feat1");
            feat1 = payloadView.getShort();
        } else {
            LOG.trace("No feat1 supplied");
            feat1 = null;
        }
        if (hasFeat2) {
            LOG.trace("Unpacking feat2");
            feat2 = payloadView.getShort();
        } else {
            LOG.trace("No feat2 supplied");
            feat2 = null;
        }
        if (hasName) {
            if (payloadView.position() == payloadView.capacity())
                throw new ParseErrorException("Payload buffer too short for name");
            LOG.trace("Unpacking name");
            // Name is defined as "remainder of packet", so slurp up everything until end of packet
            // However, there is a cap of 32 bytes in upstream code (MAX_ADVERT_DATA_SIZE)
            int nameLength = payloadView.capacity() - payloadView.position();
            if (nameLength > 32)
                throw new ParseErrorException("Name too long");
            nodeName = new byte[nameLength];
            payloadView.get(nodeName);
        } else {
            LOG.trace("No name supplied");
            nodeName = null;
        }
    }

    /**
     * get public key
     *
     * @return public key
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * set public key
     *
     * @param publicKey public key (exactly 32 bytes)
     */
    public void setPublicKey(byte[] publicKey) throws InvalidParameterException {
        if (publicKey.length != 32)
            throw new InvalidParameterException(String.format("input length mismatch: is %d, should be %d", publicKey.length, 32));
        this.publicKey = publicKey;
    }

    /**
     * get timestamp at which the packet was signed
     *
     * @return timestamp (Unix epoch based)
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * set timestamp at which the packet was signed
     *
     * @param timestamp timestamp (Unix epoch based)
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * get ed25519 signature
     *
     * @return signature, raw bytes
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * set ed25519 signature
     *
     * @param signature signature, raw bytes (exactly 64 bytes)
     */
    public void setSignature(byte[] signature) {
        if (signature.length != 64)
            throw new InvalidParameterException(String.format("input length mismatch: is %d, should be %d", signature.length, 64));
        this.signature = signature;
    }

    /**
     * get node type of advert
     *
     * @return node type
     */
    public AdvertNodeType getNodeType() {
        return nodeType;
    }

    /**
     * set node type of advert
     *
     * @param nodeType node type
     */
    public void setNodeType(AdvertNodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * get GPS latitude
     *
     * @return latitude (as whole int - float has to be done by userland due to potential for rounding issues)
     */
    public Integer getLatitude() {
        return latitude;
    }

    /**
     * set GPS latitude
     *
     * @param latitude as whole int - float has to be done by userland due to potential for rounding issues
     */
    public void setLatitude(Integer latitude) {
        this.latitude = latitude;
    }

    /**
     * get GPS longitude
     *
     * @return longitude (as whole int - float has to be done by userland due to potential for rounding issues)
     */
    public Integer getLongitude() {
        return longitude;
    }

    /**
     * set GPS longitude
     *
     * @param longitude as whole int - float has to be done by userland due to potential for rounding issues
     */
    public void setLongitude(Integer longitude) {
        this.longitude = longitude;
    }

    /**
     * get reserved feat1 value
     *
     * @return feat1 value
     */
    public Short getFeat1() {
        return feat1;
    }

    /**
     * set reserved feat1 value
     *
     * @param feat1 feat1 value
     */
    public void setFeat1(Short feat1) {
        this.feat1 = feat1;
    }

    /**
     * get reserved feat2 value
     *
     * @return feat2 value
     */
    public Short getFeat2() {
        return feat2;
    }

    /**
     * set reserved feat2 value
     *
     * @param feat2 feat2 value
     */
    public void setFeat2(Short feat2) {
        this.feat2 = feat2;
    }

    /**
     * get node name
     *
     * @return node name in bytes (conversion by userland)
     */
    public byte[] getNodeName() {
        return nodeName;
    }

    /**
     * set node name
     *
     * @param nodeName node name as byte buffer
     */
    public void setNodeName(byte[] nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * return the packet's payload buffer representing the current state of the packet
     *
     * @return full byte buffer
     */
    @Override
    public byte[] getPayloadBuffer() {
        // Allocate 149 bytes (currently maximum possible payload) to have a safe gate against emitting too large packets
        ByteBuffer ret = ByteBuffer.allocate(149).order(ByteOrder.LITTLE_ENDIAN);
        // Field 1: public key (32 bytes)
        ret.put(publicKey);
        // Field 2: timestamp (4 bytes)
        ret.putInt((int) timestamp.getEpochSecond());
        // Field 3: signature (64 bytes)
        ret.put(signature);
        // Field 4: appdata_flags
        byte flagByte = 0x00;

        if ((latitude != null && longitude == null) || (latitude == null && longitude != null))
            throw new InvalidParameterException("Either latitude or longitude member is not set");

        if (latitude != null) { //If latitude is not null, longitude must always also be non-null (gated above)
            flagByte = (byte) (flagByte | 0x10);
            LOG.trace("Have latitude / longitude");
        }
        if (feat1 != null) {
            flagByte = (byte) (flagByte | 0x20);
            LOG.trace("Have feat1");
        }
        if (feat2 != null) {
            flagByte = (byte) (flagByte | 0x40);
            LOG.trace("Have feat2");
        }
        if (nodeName != null) {
            flagByte = (byte) (flagByte | 0x80);
            LOG.trace("Have name");
        }
        flagByte = (byte) (flagByte | nodeType.getIndex());
        ret.put(flagByte);
        LOG.trace(String.format("Reconstituted flag byte %02x", flagByte));
        // Now, sequentially all data which we have
        if (latitude != null) {
            ret.putInt(latitude);
            ret.putInt(longitude);
            LOG.trace("Reconstituted latitude / longitude");
        }
        if (feat1 != null) {
            ret.putShort(feat1);
            LOG.trace("Reconstituted feat1");
        }
        if (feat2 != null) {
            ret.putShort(feat2);
            LOG.trace("Reconstituted feat2");
        }
        if (nodeName != null) {
            ret.put(nodeName);
            LOG.trace("Reconstituted name");
        }

        // Before returning, slim down the buffer to what we actually need, aka cut trailing zero-bytes
        byte[] finalPayload = new byte[ret.position()];
        ret.position(0).get(finalPayload);
        LOG.trace(String.format("Reconstituted final payload %s", hexFormat.formatHex(finalPayload)));
        return finalPayload;
    }

    @Override
    public String toString() {
        return "AdvertPacket{" +
                "packetPathInformation=" + packetPathInformation +
                ", transportCodes=" + (this.packetRouting.isUsingTransport() ?
                hexFormat.formatHex(this.transportCodes[0]) + " / " + hexFormat.formatHex(this.transportCodes[1])
                : "null") +
                ", packetRouting=" + packetRouting +
                ", packetPayloadType=" + packetPayloadType +
                ", packetVersion=" + packetVersion +
                ", nodeName=" + new String(nodeName, StandardCharsets.UTF_8) +
                ", feat2=" + feat2 +
                ", feat1=" + feat1 +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", nodeType=" + nodeType +
                ", signature=" + hexFormat.formatHex(signature) +
                ", timestamp=" + timestamp +
                ", publicKey=" + hexFormat.formatHex(publicKey) +
                '}';
    }
}
