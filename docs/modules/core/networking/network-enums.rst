.. _network-enums:

Networking - Enums
==================

Tinlok provides several enumerations that map to networking constants. This page provides a brief
description and the available constants for all platforms.

.. note::

    Specific platforms may have more than these constants exposed, but these are guaranteed to
    exist on all supported platforms.

AddressFamily
-----------------

The ``AddressFamily`` enum corresponds to ``AF_`` constants on the BSD socket API.

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

The ``SocketType`` enum corresponds to SOCK\_ constants on the BSD socket API.

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

The ``IPProtocol`` enum corresponds to ``IPROTO_`` constants on the BSD socket API.

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
