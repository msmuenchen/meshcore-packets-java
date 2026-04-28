package de.afa_amateurfunk.meshcore_packets.transport_codes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.HexFormat;

/**
 * This class is used to represent a transport code from an arbitrary packet (i.e. we do not at that point know the plaintext region)
 */
public class RawTransportCode {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(RawTransportCode.class);
    /**
     * class-wide instance of hex formatter
     */
    protected static HexFormat hexFormat = HexFormat.of();
    /**
     * backing buffer
     */
    protected byte[] rawBytes;

    /**
     * Construct a RawTransportCode wrapper object
     *
     * @param rawBytes the raw bytes containing the transport code
     */
    public RawTransportCode(byte[] rawBytes) {
        if (rawBytes.length != 2)
            throw new InvalidParameterException("Transport code must be exactly 2 bytes long");
        this.rawBytes = rawBytes;
    }

    /**
     * Return the bytes
     *
     * @return bytes representing the region code
     */
    public byte[] toByteArray() {
        return rawBytes;
    }

    @Override
    public String toString() {
        return "RawTransportCode{" +
                "rawBytes=" + hexFormat.formatHex(rawBytes) +
                '}';
    }
}
