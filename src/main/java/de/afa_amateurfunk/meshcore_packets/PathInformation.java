package de.afa_amateurfunk.meshcore_packets;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.PathSizeType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Class representing a packet's full path info (header + hops)
 */
public class PathInformation {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathInformation.class);
    /**
     * class-wide instance of hex formatter
     */
    protected static HexFormat hexFormat = HexFormat.of();
    /**
     * information about the hash length and number of hops
     */
    protected PathSizeType packetPathSize;
    /**
     * Individual hops
     */
    protected byte[][] packetHops;


    /**
     * Empty/from-scratch constructor
     */
    public PathInformation() {
        LOG.trace("Creating new PathInformation");
        packetPathSize = PathSizeType.SIZE_1;
        packetHops = new byte[0][];
    }

    /**
     * Parse a packet's path information
     *
     * @param buffer entire packet starting with the first byte of path info (=path header byte)
     * @throws ParseErrorException in case of an invalid packet
     * @see <a href="https://github.com/meshcore-dev/MeshCore/blob/dev/src/Dispatcher.cpp#L164">upstream code</a>
     */
    public PathInformation(byte[] buffer) throws ParseErrorException {
        if (buffer.length == 0)
            throw new ParseErrorException("Attempted to parse path information on empty buffer");
        LOG.trace("Attempting to parse buffer of {} bytes for path information: '{}'", buffer.length, hexFormat.formatHex(buffer));
        byte pathHeader = buffer[0];
        LOG.trace(String.format("Determining path size from %02x / %s", pathHeader, StringUtils.leftPad(Integer.toBinaryString(pathHeader & 0xFF), 8, '0')));
        this.packetPathSize = PathSizeType.fromHeader(pathHeader);
        LOG.trace(String.format("Determining hop count from %02x / %s", pathHeader, StringUtils.leftPad(Integer.toBinaryString(pathHeader & 0xFF), 8, '0')));
        /*
         * Mask out only the lower 6 bits, the upper 2 bits are used for the size
         * @link https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L80
         */
        int hopCount = pathHeader & 0x3F;
        this.packetHops = new byte[hopCount][];
        LOG.trace(String.format("%d hops", hopCount));
        if (hopCount == 0)
            return;
        if (buffer.length < 1 + (this.packetPathSize.getBytesPerHop() * hopCount))
            throw new ParseErrorException("Packet does not contain enough bytes to store all hops, appears to be cut off");
        byte[] pathBuffer = Arrays.copyOfRange(buffer, 1, 1 + (this.packetPathSize.getBytesPerHop() * hopCount));
        LOG.trace(String.format("Determining hops from %s with %d bytes per hop", hexFormat.formatHex(pathBuffer), this.packetPathSize.getBytesPerHop()));
        for (int i = 0; i < hopCount; i++) {
            this.packetHops[i] = Arrays.copyOfRange(buffer, 1 + (i * this.packetPathSize.getBytesPerHop()), 1 + (i * this.packetPathSize.getBytesPerHop()) + this.packetPathSize.getBytesPerHop());
            LOG.trace(String.format("Recorded hop %s", StringUtils.leftPad(hexFormat.formatHex(this.packetHops[i]), this.packetPathSize.getBytesPerHop() * 2, '0')));
        }
    }

    /**
     * get hop count
     *
     * @return hop count
     */
    public int getHopCount() {
        return packetHops.length;
    }

    /**
     * return path size
     *
     * @return PathSizeType corresponding to bytes-per-hop
     */
    public PathSizeType getPacketPathSize() {
        return this.packetPathSize;
    }

    /**
     * get a single hop
     *
     * @param index index of hop
     * @return hop ID
     */
    public byte[] getHop(int index) {
        LOG.trace(String.format("Getting hop %d", index));
        return packetHops[index];
    }

    /**
     * get all hops
     *
     * @return all hops
     */
    public byte[][] getHops() {
        return packetHops;
    }

    /**
     * internal helper to serialize the packet hops array to something readable by humans
     *
     * @return string representation of hops
     */
    private String serializePacketHops() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d[", this.packetHops.length));
        for (int i = 0; i < this.packetHops.length; i++) {
            sb.append(String.format("'%s'", hexFormat.formatHex(this.packetHops[i])));
            if (i < this.packetHops.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * for debug output
     *
     * @return string representation of packet's path information
     */
    @Override
    public String toString() {
        return "PathInformation{" +
                "packetPathSize=" + packetPathSize +
                ", packetHops=" + serializePacketHops() +
                '}';
    }

    /**
     * reconstitute the path information byte array
     */
    public byte[] toByteArray() {
        ByteBuffer ret = ByteBuffer.allocate(1 + (packetPathSize.getBytesPerHop() * packetHops.length)).order(ByteOrder.LITTLE_ENDIAN);
        byte headerByte = 0x00;
        headerByte = (byte) (headerByte | (packetPathSize.getBitmask() << 6));
        headerByte = (byte) (headerByte | packetHops.length);
        ret.put(headerByte);
        for (byte[] packetHop : this.packetHops) {
            ret.put(packetHop);
        }
        return ret.array();
    }
}
