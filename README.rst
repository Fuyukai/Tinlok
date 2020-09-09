KNSTE
-----

**The KNSTE GitHub page is a temporary location.**

KNSTE (Kotlin Native STdlib Extensions) is a library that fills in the "missing pieces" from the
Kotlin standard library on native platforms as well as adding useful optional extra libraries.

KNSTE is entirely re-implementations of common standard library features for Native desktop only. It
is not designed for the JVM, iOS or Android and support will not be added.

What?
=====

KNSTE adds or is planning to contain the following modules and features:

- ``knste-core``
 - Path helpers (inspired by Python's ``pathlib``)
 - Filesystem I/O
 - Network I/O
 - (Planned) Subprocessing
 - (Planned) Time/Date based on java.time
 - (Planned) Hashes (Blake2)
 - (Planned) Compression/decompression (zlib, gzip, lzma)

- ``knste-async`` (Planned)
 - Suspending I/O extensions using ``kotlinx.coroutines``

- ``knste-tls`` (Planned)
 - TLS extensions to networking I/O (sync and async)

- ``knste-crypto`` (Planned)
 - Libsodium-based cryptography library

Why?
====

The Kotlin stdlib is very barebones. Kotlin is typically a guest language on somebody else's
virtual machine, so most Kotlin code is designed around integrating with that platform's core
functionality, sometimes with helper glue code to make it more natural to use in a Kotlin context.

Kotlin/Native on Desktop, however, is a host language and has no platform to work with aside from
the platform libraries (libc or Win32) and these libraries are not ergonomic to use from a Kotlin
context.

Where?
======

KNSTE is not currently available anywhere. Stay tuned.

Stability
=========

For all I care, use KNSTE in production. I make zero stability guarantees or remarks on being
"production ready".

Versioning
==========

KNSTE using a versioning scheme called Less Afraid Versioning, which prioritises creating sound
designs over paranoid obsession over backwards compatibility. This means that APIs can, and will,
break between minor or patch versions if they are not the best possible design.
