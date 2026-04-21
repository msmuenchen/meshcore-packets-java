package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Group Datagram packet
 */
public class GroupDatagramPacket extends MeshcorePacket {
    /**
     * Construct an instance with a pre-supplied payload buffer
     *
     * @param buffer byte buffer (payload only, no header!)
     */
    public GroupDatagramPacket(byte[] buffer) {
        super();
        parsePayload(buffer);
    }

    /**
     * Construct a packet from scratch
     */
    public GroupDatagramPacket() {
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
}
