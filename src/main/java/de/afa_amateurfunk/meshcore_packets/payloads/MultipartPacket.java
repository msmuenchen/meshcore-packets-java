package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Multipart packet
 */
public class MultipartPacket extends MeshcorePacket {
    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public MultipartPacket(byte[] buffer) {
        super();
        parsePayload(buffer);
    }

    /**
     * Construct a packet from scratch
     */
    public MultipartPacket() {
        super();
    }

    /**
     * parse a payload buffer and set all applicable internal fields
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) {
        throw new NotImplementedException();
    }

    /**
     * return the packet's payload buffer representing the current state of the packet
     *
     * @return full byte buffer
     */
    @Override
    public byte[] getPayloadBuffer() {
        throw new NotImplementedException();
    }
}
