# Data flow model

This is the high level flow, explained in this and the following documents:

    MeshCore --(raw packets)--> RadioLib --(transmission units on radio bus, usually SPI)--> Radio chip --(UHF radio waves, impedance 50 ohms)--> antenna

Inside most fully integrated radio chip, the radio:

1. converts the incoming transmission unit to symbols suitable for transmission
2. adds forward error correction information (FEC) according to the Coding Rate settings
3. prepends symbols for the preamble, header and header CRC
4. calculates and appends symbols for the payload CRC
5. generates a RF modulation from the symbol stream according to the Spreading Factor settings
6. up-mixes the RF modulation with the carrier frequency
7. filters and amplifies the resulting final modulated RF signal
8. reports back the used air-time to the higher layers

Radio boards may add further filtering and power amplification stages, this however is way outside the scope.

Reception uses the inverted flowchart from above.

The reception flow inside most fully integrated radio chips is:

1. the received signal is filtered and amplified by a LNA
2. a mixing stage is used to down-mix from the carrier frequency to a lower frequency
3. the lower frequency is digitized and turned into a symbol stream according to the Spreading Factor setting
4. the symbol stream is parsed to look for the synchronization and preamble symbols to demodulate the signal, using FEC
   information configured by the Coding Rate setting to aid in recovery
5. The CRCs in the header and after the payload are used to verify successful decoding. Packets that fail the
   verification get discarded.
6. The symbols are converted into data bytes
7. The full frame's data bytes, together with information about the reception (RSSI and SNR), is handed over as a single
   transmission unit to the higher layers.

# OSI layer 1 (physical layer)

In Europe, MeshCore uses the 868 MHz ISM band, in US/NZ/AU the 915 MHz ISM band for the carrier frequency. All have in
common that they can be
used by everyone without requiring a commercial or amateur radio license and without requiring certification of the
hardware (unlike PMR446, for example). Higher bandwidths allow faster data rates, however RF bandwidth is a scarce
resource and so most meshes have shifted from 250 kHz ("wideband") to 62.5 kHz ("narrowband").

* In Europe/UK, the general recommendation is to use 869.619 MHz with 62.5 kHz bandwidth.
* The Czech Republic uses 869.525 MHz with 62.5 kHz bandwidth
* In the USA/Canada, the general recommendation is to use 910.525 MHz with 62.5 kHz bandwidth.
* In New Zealand, the general recommendation is to use 917.375 MHz with 250 or 62.5 kHz bandwidth.
* In Australia, there are three frequencies being used - 915.800 MHz, 916.575 MHz, 923.125 MHz. Bandwidth is 250 kHz (
  915.800 MHz) and 62.5 kHz otherwise.
* In Vietnam, the general recommendation is to use 920.250 MHz with 62.5 kHz bandwidth.

Power, bandwidth and duty cycle (i.e. how long is any single device allowed to transmit in a given timeframe) limits
vary
significantly between countries. Please consult local regulatory authorities and legislation, and remember that while
federal/union law specifies a broad framework, national/state legislation may add unexpected restrictions.

# OSI layer 2 (data link layer)

MeshCore is based on the [LoRa](https://en.wikipedia.org/wiki/LoRa) radio standard that
provides [synchronization, frame limiting and error detection/correction](https://www.disk91.com/2024/technology/lora/the-hidden-side-of-lora/).

MeshCore sticks to the default RadioLib configuration of
using [explicit headers](https://github.com/jgromes/RadioLib/blob/master/src/modules/SX126x/SX126x.cpp#L37), which means
it uses the features provided by LoRa's link layer.

RadioLib therefore can be assumed to only provide complete packets free of transmission errors towards the upper layer
of MeshCore.

Radios can "hear" each other as long as they are on the same frequency, bandwidth and
spreading factor settings. Frequency and bandwidth are a responsibility of the PHY layer (see above)
.The [Spreading Factor](https://www.thethingsnetwork.org/docs/lorawan/spreading-factors/) (commonly abbreviated to SF)
essentially describes the "bitrate", aka how long each individual data symbol's "beep" is on the air. Radios with
different SF settings can operate at the same time on the same frequency and bandwidth without impacting each other (
thanks to RF black magic), but they cannot hear each other.

The second configuration parameter is
the [Coding Rate](https://www.thethingsnetwork.org/docs/lorawan/fec-and-code-rate/) (commonly abbreviated to CR). It
details the amount of "extra" symbols transmitted to allow receivers to recover from transmission or reception errors
caused by all sorts of interference. Radios can "hear" each other with different coding rates.

This can be used to set highly exposed nodes (such as mountain-top repeaters or long distance links with high-gain Yagi
antennas) to higher coding rates so the signal has a better chance of getting received at the other ends, at the expense
of longer transmission times. Companion nodes will be able to hear such nodes just fine, although the chances of such a
repeater successfully understanding a node using lower CR are markedly lower on longer ranges.

The currently recommended settings for companion nodes are:

* Europe, UK: SF 8
* Czech Republic: SF 7
* Portugal: SF 7
* Australia: SF 10 (915.800 MHz), SF 7 (916.575 MHz), SF 8 (923.125 MHz)
* USA/Canada: SF 7
* NZ: SF 11 (wideband), SF 7 (narrow)
* Vietnam: SF 8

Coding Rate recommendation is usually CR 5 through 8.
