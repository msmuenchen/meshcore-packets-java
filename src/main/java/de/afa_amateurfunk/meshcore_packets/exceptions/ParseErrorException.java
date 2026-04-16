package de.afa_amateurfunk.meshcore_packets.exceptions;

/**
 * Exception to be thrown by all code paths that indicate a malformed packet or a packet using features that are not yet represented in the upstream spec or in this implementation
 */
public class ParseErrorException extends RuntimeException {
    /**
     * constructor
     *
     * @param message detailed error message
     */
    public ParseErrorException(String message) {
        super(message);
    }
}
