.. _changelog:

Changelog
=========

1.1.0
-----

 - Add a ``SecureRandom`` API.

 - Added a cryptographic API to the core module.

    - This exposes several well tested functions for general purpose cryptographic usage.

    - Add the ``Blake2b`` integrity hashing algorithm.

    - Add the ``argon2i`` password hashing algorithm.

 - Remove the ``String``-based streams. These need a design rework.

 - Add hex-encoding for ``ByteString`` objects.

 - Add Base64 encoding for ``ByteString`` objects.

1.0.0
------

 - Initial release.
