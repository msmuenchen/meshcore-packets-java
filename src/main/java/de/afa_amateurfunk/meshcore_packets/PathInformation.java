package de.afa_amateurfunk.meshcore_packets;

import de.afa_amateurfunk.meshcore_packets.exceptions.ParseErrorException;
import de.afa_amateurfunk.meshcore_packets.types.PathSizeType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HexFormat;

public class PathInformation {
    private static final Logger LOG = LoggerFactory.getLogger(PathInformation.class);
    protected static HexFormat hexFormat = HexFormat.of();
    protected PathSizeType packetPathSize;
    protected int hopCount = 0;
    protected byte[][] packetHops;
    protected byte[] pathBuffer = new byte[0];

    /**
     * Parse a packet's path information
     *
     * @param buffer
     * @link <a href="https://github.com/meshcore-dev/MeshCore/blob/dev/src/Dispatcher.cpp#L164">upstream code</a>
     */
    public PathInformation(byte[] buffer) {
        if (buffer.length == 0)
            throw new ParseErrorException("Attempted to parse path information on empty buffer");
        byte pathHeader = buffer[0];
        LOG.trace(String.format("Determining path size from %02x / %s", pathHeader, StringUtils.leftPad(Integer.toBinaryString(pathHeader & 0xFF), 8, '0')));
        this.packetPathSize = PathSizeType.fromHeader(pathHeader);
        LOG.trace(String.format("Determining hop count from %02x / %s", pathHeader, StringUtils.leftPad(Integer.toBinaryString(pathHeader & 0xFF), 8, '0')));
        /*
         * Mask out only the lower 6 bits, the upper 2 bits are used for the size
         * @link https://github.com/meshcore-dev/MeshCore/blob/main/src/Packet.h#L80
         */
        this.hopCount = pathHeader & 0x3F;
        this.packetHops = new byte[this.hopCount][];
        LOG.trace(String.format("%d hops", this.hopCount));
        if (this.hopCount == 0)
            return;
        if (buffer.length < 1 + (this.packetPathSize.getBytesPerHop() * this.hopCount))
            throw new ParseErrorException("Packet does not contain enough bytes to store all hops, appears to be cut off");
        pathBuffer = Arrays.copyOfRange(buffer, 1, 1 + (this.packetPathSize.getBytesPerHop() * this.hopCount));
        LOG.trace(String.format("Determining hops from %s with %d bytes per hop", hexFormat.formatHex(pathBuffer), this.packetPathSize.getBytesPerHop()));
        for (int i = 0; i < this.hopCount; i++) {
            this.packetHops[i] = Arrays.copyOfRange(buffer, 1 + (i * this.packetPathSize.getBytesPerHop()), 1 + (i * this.packetPathSize.getBytesPerHop()) + this.packetPathSize.getBytesPerHop());
            LOG.trace(String.format("Recorded hop %s", StringUtils.leftPad(hexFormat.formatHex(this.packetHops[i]), this.packetPathSize.getBytesPerHop() * 2, '0')));
        }
    }

    public byte[] getPathBuffer() {
        return pathBuffer;
    }

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

    @Override
    public String toString() {
        return "PathInformation{" +
                "packetPathSize=" + packetPathSize +
                ", packetHops=" + serializePacketHops() +
                '}';
    }
}
