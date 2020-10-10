.. _tcp-sockets:

Networking - TCP Sockets
========================

TCP (or Transmission Control Protocol) is the primary reliable stream-based internet protocol.
Tinlok provides first-class support for networking over TCP.

Addresses
---------

The Tinlok TCP support provides ``TcpSocketAddress`` and ``TcpSocketInfo``.

A resolved ``TcpSocketAddress`` can be obtained with ``TcpSocketAddress.resolve``:

.. code-block:: kotlin

    val addr = TcpSocketAddress.resolve("example.com", 80, resolver = GlobalResolver)

A ``TcpSocketInfo`` can be obtained by directly creating it with the IP address and port to bind
on, or by using ``TcpSocketInfo.wildcard(port)`` for the dual stack wildcard and
``TcpSocketInfo.localhost()`` for the dual stack localhost address.

Socket Options
--------------

The ``TcpSocket`` base interface provides overloads for getting and setting ``TcpSocketOption``
socket options. Both the client and server sockets implement this interface.

The available options are:

* I lied, there's none yet. Stay tuned!

Client Sockets
--------------

The ``TcpClientSocket`` interface is a ``StreamingClientSocket`` that operates over the TCP
protocol.

.. code-block:: kotlin

    // resolve an address, connect a socket to it, and read some data off of it
    val address = TcpSocketAddress.resolve("time-d-b.nist.gov", 13)
    val data = TcpClientSocket.connect(address) {
        it.readUpTo(4096)
    }

Tinlok adds support for allowing ``connect`` calls to timeout, by passing an optional ``timeout``
parameter to the call. The timeout is specified in milliseconds.

.. code-block:: kotlin

    val millis = 30_000
    TcpClientSocket.connect(address, timeout = millis) {
        ...
    }

Server Sockets
--------------

The ``TcpServerSocket`` interface is a ``AcceptingServerSocket`` that operates over the TCP
protocol.

A server socket can be opened with a ``ConnectionInfo`` but not immediately connected, if you
need to set socket options before calling ``bind``:

.. code-block:: kotlin

    val info = TcpConnectionInfo.wildcard(80)
    TcpServerSocket.open(info) { sock ->
        sock.setSocketOption(StandardSocketOptions.SO_REUSEADDR, true)
        sock.bind(backlog = 128)

        sock.accept {
            // ...
        }
    }

A server socket can also be directly opened and bound to a ``ConnectionInfo``:

.. code-block:: kotlin

    val info = TcpConnectionInfo.wildcard(80)
    TcpServerSocket.bind(info) { sock ->
        sock.accept {
            // ...
        }
    }

