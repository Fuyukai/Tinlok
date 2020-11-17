.. _ipaddress:

Networking - IP Addresses
=========================

Copperchain provides high-level immutable wrappers for IPv4 and IPv6 addresses, which can be used
everywhere an IP address is used.

Al IP addresses inherit from the ``IPAddress`` class, which provides a ``version`` and a
``family`` field (see :ref:`network-enums`), as well as the ``representation`` field which contains
the raw ``ByteString`` representation.

IPv4
----

To create a new ``IPv4Address``, you can use the helper companion object methods:

.. code-block:: kotlin

    val ip = IPv4Address.of("192.81.134.36")
    // esoteric!
    val decimalIp = IPv4Address.of(3226568228U)
    assert(decimalIp.toString() == "192.81.134.36")

IPv6
----

To create a new ``IPv6Address``, you can use the helper companion object methods:

.. code-block:: kotlin

    val ip = IPv6Address.of("2600:3c01::f03c:91ff:fedb:76b6")
    assert(ip.toString() == "2600:3c01::f03c:91ff:fedb:76b6")

.. note::

    IPv6 stringification uses the output format as specified by RFC 5952, not the canonical output.

IPv6 address parsing supports all forms of valid IPv6 address textual representation.

.. versionchanged:: 1.3.0

    1.3.0 onwards uses a pure-Kotlin IPv6 parser, rather than a dependency provided one.

