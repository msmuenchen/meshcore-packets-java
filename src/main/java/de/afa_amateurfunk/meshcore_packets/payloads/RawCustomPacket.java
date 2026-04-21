package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;

/**
 * Raw Custom packet
 */
public class RawCustomPacket extends MeshcorePacket {
    /**
     * the packet's payload part in raw byte form
     */
    protected byte[] payloadBuffer;

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


    /**
     * getter for payloadBuffer field
     *
     * @return payloadBuffer field
     */
    public byte[] getPayloadBuffer() {
        return payloadBuffer;
    }

    /**
     * setter for payloadBuffer field
     * TODO check if we can actually do this or if we rather have to go for the subclasses?
     *
     * @param payloadBuffer raw buffer
     */
    protected void setPayloadBuffer(byte[] payloadBuffer) {
        if (payloadBuffer.length > 184)
            throw new ParseErrorException("Payload buffer too long");
        this.payloadBuffer = payloadBuffer;
    }
}
