package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;

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

    }

    /**
     * Construct a packet from scratch
     */
    public RawCustomPacket() {

    }

    /**
     * parse a payload buffer and set all applicable internal fields
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) {

    }
}
