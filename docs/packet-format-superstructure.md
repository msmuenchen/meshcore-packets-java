# Packet superstructure

All MeshCore packets are one self-contained transmission unit from (or to) the radio, with RadioLib taking care of
communication streams to ensure that the MeshCore layer does not have to deal with its own buffer queues and partial
transmission units.

All MeshCore packets carry at least two bytes - one byte describing the superstructure version, routing instructions and
the payload type, and at least one byte detailing the path the packet has taken so far. Most payload types expect at
least one byte of MeshCore payload, except the `RAW_CUSTOM` payload type.

The superstructure is described
upstream [here](https://github.com/meshcore-dev/MeshCore/blob/main/docs/packet_format.md).

# Routing

MeshCore packets can be routed in two ways: `DIRECT` and `FLOOD`.

## DIRECT routing ("zero hop routing")

Packets that have their routing set to `DIRECT` are *not* repeated by any other node. They may, however, cause any
receiving station to react to whatever is inside the packet and also emit some sort of response packet back.

## FLOOD routing

For packets that have their routing set to `FLOOD` are repeated by repeater nodes and by room-server nodes that have
repeat enabled ("roompeater") as store-and-forward. Unlike Meshtastic, companion nodes do not repeat any kind of packets
and cannot be forced to repeat packets on the normal MeshCore frequencies.

For each repeated packet, the repeating node adds the first byte(s) of its public key to the Path List. The Path List
Header carries two pieces of information: the size of each hop's entry in bytes (default: 1 byte per hop, supported are
up to 3 bytes per hop) and the amount of hops already taken. Both together can be used to obtain the total length of the
Path List - anything from 1 byte (the packet just got received and so only has the Path List Header byte) to 64 bytes (1
byte Path Header plus 63/31/21 hops, depending on size-per-hop).

The length of the hop size is set by the originating node of the packet and is not changed during transmission.
Repeating nodes running firmware 1.14 and above will be able to repeat packets of all three hop sizes (until the hop
length reaches the limit), repeating nodes running older firmware will discard packets with more than one byte per hop
or more than 64 hops.

To avoid loops (aka, two or more repeating nodes continuously repeating the same packet) or nodes processing the same
packet multiple times after hearing it from multiple repeaters, MeshCore employs two mechanisms:

1. Packet Hash - each packet seen by a MeshCore node has its payload (but not its header) hashed. Only packets that
   haven't been seen on the air by the MeshCore node get processed at all, others get discarded.
2. Path Header - each packet seen by a MeshCore repeating node has its Path Header inspected to see if the repeater's
   public key is already in the path. This is used especially on low-resource nodes that lack enough RAM to keep hashes
   around for enough time - in a particularly dense mesh, it may very well be the case that a packet can be heard over a
   dozen times. If the repeating node sees its hash in the Path header, it will discard the packet.

The second mechanism has an obvious drawback - a single byte hop size will inevitably lead to collisions, aka a
repeating node *not* repeating a packet despite it not having seen this packet already, as it cannot reliably determine
having seen or not seen that packet. Larger hash sizes (commonly known as "multibyte packets") significantly reduce the
chance of collisions, although "popular" public key prefixes such as "`aaaa`", "`bbbb`", "`cccc`" etc. are known to have
multiple repeaters using them.

# Transport Codes

A consequence of the MeshCore network growing bigger and bigger, particularly with the addition of long-range
mountaintop repeaters, is that the range of packets routed by `FLOOD` (such as channel message and node advert packets)
grows considerably - chains ranging from Northern Italy all through and up to Northern Germany have already been
observed. This
leads to two consequences: people aren't interested in channel messages from someone half a continent away (especially
as return paths aren't guaranteed) and both message and node advert packets congest the available air time for
transmitters, leading to a loss of actually wanted packet transmissions along the chain.

To solve this, transport codes were developed. These codes (known in the MeshCore UI as "regions" and "scopes") are
inspected by repeating nodes (companion nodes do not care about them!) as follows:

1. The packet has no transport codes attached at all (routing `FLOOD` or `DIRECT`) - if "`allowf *`" is set, the packet
   is
   processed normally. If "denyf *" is set, the packet is discarded. (The EVO firmware fork and the official MeshCore
   firmware [differ here](https://github.com/meshcore-dev/MeshCore/pull/1810/changes): EVO only drops GRP_TXT, GRP_DATA
   and ADVERT packets, whereas the official firmware drops *all packets* including those needed for path discovery)
2. The packet has a transport code attached (routing `TRANSPORT_FLOOD` or `TRANSPORT_DIRECT`) - if the transport code is
   in
   the repeater's "allowf" list, it gets repeated, otherwise it gets discarded.

Why `TRANSPORT_DIRECT` even exists is a bit of a weird question as `DIRECT`-routed packets are not supposed to be
repeated anyway.

# Payload types

At the moment, MeshCore supports 13 different payload types:

* [Request (`REQ`) / Anonymous Request (`ANON_REQ`) / Response (
  `RESPONSE`): used to request configuration and telemetry](packet-format-request-response.md)
  from another node
* [Text Message (`TXT_MSG`) / Ack (`ACK`)](packet-format-textmessage.md): a message between two MeshCore nodes that has
  confirmed delivery for the
  sender
* [Advert (`ADVERT`)](packet-format-advert.md): a packet detailing at least a node's type, name and full public key.
  Optionally, the node's
  location (latitude and longitude) can be transmitted as well.
* [Group Text (`GRP_TXT`) / Group Datagram (`GRP_DATA`)](packet-format-group.md): used for the text channels (private
  channels, hashtag
  channels and the Public channel). Unlike Text Message, there is no confirmation of delivery and "silent" losses along
  the path are expected!
* [Path (`PATH`)](packet-format-path.md) / [Trace (`TRACE`)](packet-format-trace.md): link discovery, tracing and
  troubleshooting
* [Multipart (`MULTIPART`)](packet-format-multipart.md): allows for extended packets going beyond the 184 byte payload
  limit
* [Control (`CONTROL`)](packet-format-control.md): zero-hop discovery of neighbor nodes
* [Raw Custom (`RAW_CUSTOM`)](packet-format-rawcustom.md): application-defined payloads. Used for third-party
  applications leveraging MeshCore's
  network and routing.
