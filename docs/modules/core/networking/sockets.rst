.. _sockets:

Sockets
=======

BSD sockets are the standard way to do socket networking on nearly all modern platforms. Tinlok
provides a cross-platform wrapper over BSD sockets (the POSIX socket implementation on Linux, or
WinSock2 on Windows), for both blocking and non-blocking sockets, via the ``Socket`` interface.

.. warning::

    Sockets are low-level and Unsafe as they hold a reference to a managed resource
    (the actual socket).

Creation
--------

Sockets can be created easily with the helper functions on the ``Socket.Companion`` object:

.. code-block:: kotlin

    val tcp = Socket.tcp(StandardAddressFamilies.AF_INET6)
    val udp = Socket.udp(StandardAddressFamilies.AF_INET6)

Socket Options
--------------

Socket options can be set on a socket with the ``setsockopt`` method and retrieved with the
``getsockopt`` method.

.. code-block:: kotlin

    sock.setsockopt(StandardSocketOptions.SO_REUSEADDR, true)
    val opt = sock.getsockopt(StandardSocketOptions.SO_KEEPALIVE)

Non-blocking
------------

Sockets can operate in both *blocking* and *non-blocking* mode, controlled by the ``blocking``
property on the socket object.

.. warning::

    Do NOT set the socket to non-blocking with an ``fcntl`` or ``ioctlsocket`` call on the
    underlying handle. Internally, implementations track the non-blocking status with a boolean
    value.

Due to this, sockets return a ``BlockingResult`` inline class in various locations, which wraps
either the result of a call, or ``-1`` to signify that the socket needs to be polled on until
the operation can complete. In blocking mode, this result will never be -1.

Connecting
----------

Client sockets are connected using ``connect`` with a ``ConnectionInfo`` object of the right type.

.. note::

    Despite the naming, ``SocketAddress`` is not the object to use here.

.. code-block:: kotlin

    sock.connect(TcpConnectionInfo(...))

For blocking sockets, this will either throw an exception or return true to indicate successful
connection. For non-blocking connection, this will return a boolean for if the socket has connected
immediately, or if it needs to be polled upon.

Binding
-------

Server sockets are bound using ``bind`` with a ``ConnectionInfo``, and then set into listen mode.

.. code-block:: kotlin

    sock.bind(TcpConnectionInfo(...))
    sock.listen(backlog = 16)

Accepting
---------

Server sockets accept with the ``Unsafe`` method ``accept``.

.. warning::

    This method is unsafe because failing to close the socket will leak a file descriptor.

.. code-block:: kotlin

    val client: Socket<I>? = sock.accept()

For blocking sockets, this will return the accepted client socket. For non-blocking sockets, this
will either return the accepted client socket, or null if no client is available yet and the socket
needs to be polled on.

Receiving data
--------------

The ``recv`` method is used to receive data on connection-oriented sockets. This works for both
``ByteArray`` objects and ``Buffer`` objects.

.. code-block:: kotlin

    val ba = ByteArray(1024)
    val count = sock.recv(ba, ba.size, 0, 0).ensureNonBlocking()

The ``recvfrom`` method is used to receive data on datagram-oriented sockets. It is similar to
``recv``, but instead of returning the count alone, it returns a ``RecvFrom`` object which wraps
both the ``BlockingResult`` and the ``ConnectionInfo`` remote address data was received from.

Sending data
------------

Sending data has three forms:

- The ``send`` call, which does **ONE** attempt at sending data.
- The ``sendall`` call, which will **RETRY** until all data is sent, or the socket blocks.
- The ``sendto`` call, which does **ONE** attempt at sending data to the specified address.
