package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.MeshcorePacket;
import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.ControlPacketType;
import de.afa_amateurfunk.meshcore_packets.types.PayloadType;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control packet
 * <p>Control packets are used to discover neighboring nodes</p>
 * <p>Implementation notice: this is a pseudo-abstract class, see {@link MeshcorePacket#fromBytes(byte[])} for the reasoning</p>
 *
 * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/main/examples/simple_repeater/MyMesh.cpp#L772">upstream code</a>
 * @see ControlRequestPacket
 * @see ControlResponsePacket
 */
public class ControlPacket extends MeshcorePacket {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControlPacket.class);

    /**
     * subtype
     */
    protected ControlPacketType subtype;

    /**
     * Construct a packet from scratch
     */
    public ControlPacket() {
        super();
        this.packetPayloadType = PayloadType.CONTROL;
    }

    /**
     * Try to parse and unserialize the actual ControlPacket subclass from its payload buffer
     *
     * @param payloadBuffer raw payload buffer
     * @return ControlPacket subclass
     */
    public ControlPacket subclassFromBytes(byte[] payloadBuffer) {
        ControlPacket ret;
        //Guard at least enough to not run into a buffer overrun at the start
        //Proper check of payload length should happen in the subclasses
        if (payloadBuffer.length == 0)
            throw new ParseErrorException("Payload too short");
        subtype = ControlPacketType.fromHeader(payloadBuffer[0]);
        switch (subtype) {
            case DISCOVER_REQUEST -> ret = new ControlRequestPacket(payloadBuffer);
            case DISCOVER_RESPONSE -> ret = new ControlResponsePacket(payloadBuffer);
            /*
            Ignore coverage report lacking for this line - it cannot be reached.
            We keep the default branch however in case we do implement a new packet type in ControlPacketType
            but forget to implement it here
             */
            default -> throw new ParseErrorException("Unable to parse control payload header");
        }
        ret.copySuperstructure(this);
        return ret;
    }

    /**
     * get subtype
     *
     * @return subtype
     */
    public ControlPacketType getSubtype() {
        return subtype;
    }

    /**
     * Do not call this directly, it will not work, see {@link MeshcorePacket#fromBytes(byte[])}
     *
     * @param payloadBuffer byte buffer (payload only, no header!)
     * @throws NotImplementedException always
     */
    @Override
    public void parsePayload(byte[] payloadBuffer) throws NotImplementedException {
        throw new NotImplementedException();
    }

    /**
     * Do not call this directly, it will not work, see {@link MeshcorePacket#fromBytes(byte[])}
     *
     * @return nothing
     * @throws NotImplementedException always
     */
    @Override
    public byte[] getPayloadBuffer() throws NotImplementedException {
        throw new NotImplementedException();
    }

}
