.. _changelog:

Changelog
=========

1.2.0
-----

 - Add ``WindowsPurePath``.

 - I/O interfaces now work similar to ``Readable|WriteableByteChannel``, reading into a buffer.

 - ``LinuxPath`` is now a ``PosixPurePath``.

 - Add compiled versions of the two static libraries for mingwX64.

 - Refactor out various APIs into a new ``Copperchain`` project.

   - This may eventually be part of the basis for a truly multiplatform standard library, with
     both JVM and Native implementations. May.

 - Remove errno/winerror from OSException, and make them part of CC.

   - winerror is too broad for the design to work properly.

   - Also, I don't see really any use for errno properties that wouldn't be better served with
     more specific subclasses.

 - Add UUID support.

1.1.0 (Released 2020-10-14)
---------------------------

 - Add a ``SecureRandom`` API.

 - Added a cryptographic API to the core module.

    - This exposes several well tested functions for general purpose cryptographic usage.

    - Add the ``Blake2b`` integrity hashing algorithm.

    - Add the ``argon2i`` password hashing algorithm.

 - Remove the ``String``-based streams. These need a design rework.

 - Add hex-encoding for ``ByteString`` objects.

 - Add Base64 encoding for ``ByteString`` objects.

 - Add ``poll()`` based timeout for TCP sockets when connecting.

 - Add ``libtls`` based TLS support.

1.0.0
------

 - Initial release.
