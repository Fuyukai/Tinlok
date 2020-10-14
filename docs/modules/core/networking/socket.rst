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

See :ref:`closingscope` for more information about how socket safety is enforced.

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

