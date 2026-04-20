package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;

/**
 * Raw Custom packet
 */
public class RawCustomPacket extends MeshcorePacket {

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public RawCustomPacket(byte[] buffer) {
        super();
        packetPayloadType = PayloadType.RAW_CUSTOM;
        parsePayload(buffer);
    }

    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer as hex string (payload only, no header!)
     */
    public RawCustomPacket(String buffer) {
        super();
        packetPayloadType = PayloadType.RAW_CUSTOM;
        parsePayload(hexFormat.parseHex(buffer));
    }

    /**
     * Construct a packet from scratch
     */
    public RawCustomPacket() {
        super();
        packetPayloadType = PayloadType.RAW_CUSTOM;
        setPayloadBuffer(new byte[0]);
    }

    /**
     * parse a payload buffer and set all applicable internal fields
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) {
        setPayloadBuffer(payloadBuffer);
    }
}
