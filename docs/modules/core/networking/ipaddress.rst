.. _ipaddress:

Networking - IP Addresses
=========================

Tinlok provides high-level immutable wrappers for IPv4 and IPv6 addresses, which are used
everywhere an IP address is used.

Al IP addresses inherit from the ``IPAddress`` class, which provides a ``version`` and a
``family`` field (see :ref:`network-enums`).

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

IPv6 address parsing supports all forms of valid IPv6 address textual representation.

IPv6 parsing and stringification uses the ipv6-parse_ library, licenced under the MIT licence. It
is statically linked into the Tinlok library, meaning neither a developer or an end-user needs it
installed to use.

.. _ipv6-parse: https://github.com/jrepp/ipv6-parse
