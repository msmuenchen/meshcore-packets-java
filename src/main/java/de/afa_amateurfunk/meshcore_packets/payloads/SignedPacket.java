package de.afa_amateurfunk.meshcore_packets.payloads;

import de.afa_amateurfunk.meshcore_packets.crypto.PrivateKey;

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

    /**
     * (Re)-Sign this packet using a specified private key, updating its publicKey field if present
     *
     * @param privateKey Private key to use
     */
    void updateSignature(PrivateKey privateKey);
}
