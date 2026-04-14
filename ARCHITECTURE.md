# Decoding a MeshCore packet

The entry point to decoding is the class `de.afa_amateurfunk.meshcore_packets.MeshcorePacket` and its methods
`fromString`, which accepts a hex string (which you can get from observers, debug logs or the RX log of the MeshCore
app) or a byte buffer (if your usecase is a full-featured MeshCore client).

It will return a fully instantiated subclass of `MeshcorePacket`, assuming the packet is syntactically correct,
otherwise it will throw an exception.

Payloads that require cryptographic material to decrypt or verify will require a followup call to their `decrypt` method
supplying the key material. The fields of the Packet class will be updated by that call, but it might be that decryption
errors or MAC verification failures leave the packet in an undefined state after throwing an exception.

# Encoding a MeshCore packet

The entry point here are the individual packet type classes in `de.afa_amateurfunk.meshcore_packets.payloads`. Construct
the packet, call the appropriate setters, and once finished, use getFullData() to get the resulting packet.

# Separation of concerns

As far as possible, dealing with individual parts of a MeshCore packet is left to dedicated classes.

## Enums

Decoding of anything looking like an enumeration of bitmasks (for the superstructure, the version/route/payload byte and
the path segment) is done in the appropriate Enum classes in `de.afa_amateurfunk.meshcore_packets.types`. These all
follow the convention that the Enum members are named in plain English, with each Enum member having their bitmask and
the original name of the definition in the MeshCore firmware as properties.

The `fromHeader` function is the entry point returning the Enum member corresponding to the byte from the raw buffer.

## Path decoding

Decoding hops (# of bytes per hop and the individual hops) is a bit of an exception to the above rule - wrestling with
hops is split off into the `PathInformation` class, which uses the `PathSizeType` enum to parse the related bitfield.

## Payload subclasses

Each individual payload type has its own corresponding subclass with all fields that might be present in such a packet.
