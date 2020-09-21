.. _sockets:

Networking - Generic Sockets
============================

Tinlok defines several interfaces for sockets.

* ``Socket`` - the base interface, defines functions for getting and setting ``SocketOption``.

* ``ClientSocket<I>`` - defines a client socket, with a ``remoteAddress`` property, where ``I`` is
  the ``ConnectionInfo`` type the remote address is defined as.

* ``ServerSocket`` - defines a server socket that can ``bind`` to its address with a backlog.

  - A server socket may be opened without being bound to a specific ``ConnectionInfo``.

Furthermore, some base interfaces are provided for specific behaviour of certain sockets:

* ``AcceptingServerSocket<I, T>`` - defines a server socket that accepts new client connections,
  producing new ``ClientSocket<I>`` instances of type ``T``

* ``StreamingClientSocket<I>`` - defines a client-side socket that is also a
  ``HalfCloseableStream``.

Socket Options
--------------

Sockets can have their parameters tweaked using the socket option API. Socket options are exposed
with the type-safe ``setSocketOption`` and ``getSocketOption`` API, which is overloaded for every
specific socket type.

The base ``Socket`` interface defines the ``StandardSocketOption`` class, which contains several
socket options that are defined for **all** sockets. More specific subinterfaces and subclasses
may overload the ``set|getSocketOption`` functions with other option types that that specific
socket will accept.

Safety
------

For safety purposes, several functions are provided a lambda which are then passed an open
socket, instead of directly returning a socket. Usually, this is because failure to close a
socket will lead to the socket's fd living forever and the easiest way to design APIs to enforce
closure is to run at the end of a passed-in lambda's execution.

Usually, these functions are safe inline extension functions that call the unsafe function
within, thus having near-zero performance impact.

Functions that do this include:

* ``AcceptingServerSocket.accept``
* ``TcpClientSocket.connect``
* ``TcpServerSocket.open``

This is a non-exhaustive list.

TCP
---

See :ref:`tcp-sockets` for more information on TCP sockets.

UDP
---

Currently unsupported! I don't know how to design their API yet. Look out for them in the next
release.

Unix sockets
------------

Currently unsupported! I don't know how to design their API yet.

