.. _changelog:

Changelog
=========

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
