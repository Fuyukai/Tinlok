.. _network-enums:

Networking - Enums
==================

Tinlok provides several interfaces defining the socket creation constant, and enumerations
containing a set of possible values that are known to be supported by all platforms.

AddressFamily
-----------------

The ``AddressFamily`` enumeration corresponds to ``AF_`` constants on the BSD socket API, with the
specified members:

+---------------+-----------------------------------------------------------------------+
| Name          | Description                                                           |
+===============+=======================================================================+
| ``AF_UNSPEC`` | Unspecified. Used as a hint for getaddrinfo(), not in the socket API. |
+---------------+-----------------------------------------------------------------------+
| ``AF_INET``   | IPv4.                                                                 |
+---------------+-----------------------------------------------------------------------+
| ``AF_INET6``  | IPv6.                                                                 |
+---------------+-----------------------------------------------------------------------+
| ``AF_UNIX``   | Unix domain sockets.                                                  |
+---------------+-----------------------------------------------------------------------+

SocketType
--------------

The ``SocketType`` enumeration corresponds to SOCK\_ constants on the BSD socket API, with the
specified members:

+-----------------+--------------------------------------------+
| Name            | Description                                |
+=================+============================================+
| ``SOCK_STREAM`` | Stream-based connections, such as TCP.     |
+-----------------+--------------------------------------------+
| ``SOCK_DGRAM``  | Datagram-based connections, such as UDP.   |
+-----------------+--------------------------------------------+
| ``SOCK_RAW``    | Raw sockets, for running over IP directly. |
+-----------------+--------------------------------------------+

IPProtocol
--------------

The ``IPProtocol`` enumeration corresponds to ``IPROTO_`` constants on the BSD socket API, with the
specified members:

+------------------+--------------------------------------------------+
| Name             | Description                                      |
+==================+==================================================+
| ``IPPROTO_IP``   | Usually signifies "kernel chooses the protocol". |
+------------------+--------------------------------------------------+
| ``IPPROTO_TCP``  | Transport Control Protocol.                      |
+------------------+--------------------------------------------------+
| ``IPPROTO_UDP``  | Unreliable/User Datagram Protocol.               |
+------------------+--------------------------------------------------+
| ``IPPROTO_ICMP`` | Internet Control Message Protocol.               |
+------------------+--------------------------------------------------+
