package de.afa_amateurfunk.meshcore_packets.payloads;

/**
 * Interface for packets that carry signatures
 */
public interface SignedPacket {
    /**
     * Verify the packet's payload using a public key provided in the packet
     *
     * @return true if signature matches, false if not
     */
    boolean verify();
}
