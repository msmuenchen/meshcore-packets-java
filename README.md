# MeshCore Java Packets Library

This project is a library allowing developers of tooling (both backends of projects like observers/analyzers and
frontends that integrate with MeshCore modems) to encode and decode MeshCore packets.

# Why?

The MeshCore firmware and its documentation can be challenging to follow along, especially as one piece of
functionality (such as parsing a packet) is split over multiple C++ classes, header files and methods.

Other parts such as communication flows or multipart packets are not documented at all and have to be reverse-engineered
first.

In addition to providing a library for Java developers and documentation, the test cases can be adapted by developers of
other MeshCore-related projects.

# Project status

## What's working

Decoding of the packet superstructure (i.e. version, routing, payload type, transport codes, path) works and is
reasonably tested.

## What's missing

* Decoding of individual packet types
* Calculating packet hashes
* Encoding packets that have been created from scratch as Java objects into hex that can be sent off to the MeshCore
  mesh
* Patching packets (e.g. add new hops)
* verifying signatures (e.g. advert payloads)
* signing advert payloads using a key
* decrypting and encrypting of channel message payloads (GRP_TXT, GRP_DATA)
* decrypting and encrypting of peer-to-peer payloads (TEXT_MSG)

# How to help

Feel free to work on whatever you like and file pull requests. Please use JavaDoc and comments heavily if you are
referring to concepts and definitions from the MeshCore code so that there is a bit of a reference trail.

# License / AI

Please refrain from using AI for committing work to avoid legal issues. This project is licensed under the LGPL and it
is therefore too risky for it to incorporate "AI-laundered" GPL, CC-NC, CC-ND or similarly incompatible work by
accident.

You can however freely use the work of this project to feed into an AI of your choice, as long as the output of that
AI - if it is a derivative of this project - is also licensed under the LGPL.
