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

# Requirements

This project requires at least Java 21 support. It is recommended to use IntelliJ, the project supplies run
configurations for the tests.

Use as recent a Maven version as you can, in doubt use `./mvnw` and/or specify your IDE to use Maven Wrapper.

# How to use

Build the library and install it to your local Maven repository with `mvn clean install`.

To generate the Jacoco reports and Javadoc, run `mvn clean package javadoc:javadoc`. The Jacoco report is at
`target/site/jacoco/index.html`, the Javadoc entrypoint is at `target/reports/apidocs/index.html`.

# Project status

## What's working

Decoding of the packet superstructure (i.e. version, routing, payload type, transport codes, path) works and is
reasonably tested. Packet hashes can be calculated, and supported payload types can also be fully manipulated and
created from scratch:

* Raw/Custom packets - fully supported as a structure. Individual applications using these can be implemented if someone
  provides information.
* Control: Request and Response packets are fully supported.
* Advert: Fully supported.

Cryptography support:

* Public / private key generation
* Import of public and private keys from ed25519 seed and ed25519-orlp (also known as `a || RH` / `ref10` /
  `expanded hash` format)
* Conversion of ed25519 seed to ed25519-orlp format
* Derivation of public key from private key
* Creation and verification of digital signatures

## What's missing

* Decoding of individual packet types:
    * AnonRequest/Request/Response
    * Path/Trace
    * TextMessage/Ack
    * GroupText/GroupDatagram
    * Multipart
* decrypting and encrypting of channel message payloads (GRP_TXT, GRP_DATA)
* decrypting and encrypting of peer-to-peer payloads (TEXT_MSG)

Cryptography:

* X25519 derivation of shared secret between two public / private key pairs
* shared-secret encryption and decryption in channels
* shared-secret (based on X25519) encryption and decryption between nodes

# How to help

Feel free to work on whatever you like and file pull requests. Please use JavaDoc and comments heavily if you are
referring to concepts and definitions from the MeshCore code so that there is a bit of a reference trail.

If you wish to create unit tests, the project aims for 100% line and branch coverage of all individual classes. The
helper methods `toString`, `equals`, `hashCode` are excluded, and so are `if` assertions that are safeguards against
legitimately impossible to reach places.

# License / AI

Please refrain from using AI for committing work to avoid legal issues. This project is licensed under the LGPL and it
is therefore too risky for it to incorporate "AI-laundered" GPL, CC-NC, CC-ND or similarly incompatible work by
accident.

You can however freely use the work of this project to feed into an AI of your choice, as long as the output of that
AI - if it is a derivative of this project - is also licensed under the LGPL.
