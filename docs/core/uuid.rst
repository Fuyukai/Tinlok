.. _uuids:

UUIDs
=====

Tinlok includes full built-in support for UUID_ objects, via the ``UUID`` class. This includes
parsing both Variant 1 (`RFC 4122`_) and Variant 2 (Legacy Microsoft) UUIDs of all five versions.

Creating UUIDs
--------------

Tinlok has support for generating version 1 (MAC address based) and version 4 (psuedorandom) UUIDs.

.. note::

    UUID generation is implemented via libuuid_ for Linux, and ``CreateUuid`` on Windows. This
    helps ensure global uniqueness whenever possible.

.. code-block:: kotlin

    val v4 = UUID.uuid4()
    assert(v4.version == UUID.Version.VERSION_FOUR)

    val v1 = UUID.uuid1()  // leaks information!
    assert(v1.version == UUID.Version.VERSION_ONE)

Parsing UUIDs
-------------

Existing UUID data in binary form can be used with the primary constructor of the UUID class:

.. code-block:: kotlin

    val data = byteArrayOf(...).toByteString()
    val uuid = UUID(data)

UUIDs can be parsed from a string using ``fromString()``:

.. code-block:: kotlin

    val uuid = UUID.fromString("c55ec9ff-cae0-406e-b0e6-0378f101285f")

Properties
----------

The UUID class contains all the properties defined in `RFC 4122`_, and enumerations for certain
multiplexed fields.

+-------------------------------+----------------------------+---------------------------------------------------------------------+
| RFC Name                      | Property                   | Description                                                         |
+===============================+============================+=====================================================================+
| ``time_low``                  | ``timeLow``                | The lower part of the timestamp.                                    |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| ``time_mid``                  | ``timeMid``                | The middle part of the timestamp.                                   |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| ``time_hi_and_version``       | ``timeHighAndVersion``     | The upper part of the timestamp, multiplexed with the version.      |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| ``clock_seq_hi_and_reserved`` | ``clockSeqHighAndVariant`` | The upper part of the clock sequence, multiplexed with the variant. |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| ``clock_seq_low``             | ``clockSeqLow``            | The lower part of the clock sequence.                               |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| ``node``                      | ``node``                   | The unique node identifier.                                         |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| N/A                           | ``version``                | The version enum, extracted from ``timeHighAndVersion``.            |
+-------------------------------+----------------------------+---------------------------------------------------------------------+
| N/A                           | ``variant``                | The variant enum, extracted from ``clockSeqHighAndVariant``.        |
+-------------------------------+----------------------------+---------------------------------------------------------------------+

Properties
----------

.. _UUID: https://en.wikipedia.org/wiki/Universally_unique_identifier
.. _libuuid: https://linux.die.net/man/3/libuuid
.. _RFC 4122: https://tools.ietf.org/html/rfc4122
