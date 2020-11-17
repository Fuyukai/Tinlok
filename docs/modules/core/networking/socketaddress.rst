.. _socketaddress:

Networking - Socket Addresses
=============================

.. versionchanged:: 1.3.0

    Added the ability to create addresses directly through the constructor.

Under the BSD socket model, sockets are bound or connected to a specific single address, which
is of a single address family, protocol and socket type. In the real world, this is not
sufficient; with dual stack networks being common but not too common, you want to be able to
connect to IPv6 if possible then fall back to IPv4.

Tinlok builds socket abstractions around this model by separating out an address
a socket will bind to with the actual networking address using two classes: ``SocketAddress`` and
``ConnectionInfo``.

ConnectionInfo
--------------

A ``ConnectionInfo`` instance contains the raw information a socket can use to open and connect
or bind to. It contains the socket constants for a BSD socket, and is subclassed for things such as
internet sockets or unix sockets.

For internet sockets, the ``InetConnectionInfo`` abstract class exists. It wraps an ``IPAddress``
and an integer port.

Usually, a ``ConnectionInfo`` subclass instance can be created by passing the IP and port:

.. code-block:: kotlin

    val ip = IPv6Address.of("::1")
    val info = TcpConnectionInfo(ip, 80)
    // info can now be used to bind a server socket

The wildcard address and the localhost address can be created using helper methods:

.. code-block:: kotlin

    val wildcard = TcpConnectionInfo.wildcard(22)
    val localhost = TcpConnectionInfo.localhost(21)

A ``ConnectionInfo`` exposes the ``family``, ``type``, and ``protocol``. See :ref:`network-enums`.

SocketAddress
-------------

A ``SocketAddress`` is a ``Set`` of multiple ``ConnectionInfo`` instances as returned by the
underlying system DNS resolver, representing a single remote endpoint. It is passed to a socket to
connect to the remote server over all available address families.

A ``SocketAddress`` is usually created from a helper method on the companion object, rather than
being passed directly:

.. code-block:: kotlin

    val tcp = TcpSocketAddress.resolve("toaru-project.com")
    val udp = UdpSocketAddress.resolve("some-udp-service-i-couldnt-think-of-any.arpa")

.. note::

    These methods come from Tinlok-Core. DNS resolution is not provided in Copperchain.

.. note::

    In library design, anywhere you want to do DNS resolution you should allow a user to pass a
    ``resolver`` parameter to customise the DNS resolver used.

These addresses can then be passed to a client socket to connect with.
