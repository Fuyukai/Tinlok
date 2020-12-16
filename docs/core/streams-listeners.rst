.. _streams-listeners:

Streams and Listeners
=====================

.. versionchanged:: 1.3.0

    Streams were redesigned significantly.

The high-level API in Tinlok consists of two parts:

- Streams, for objects that read or write incoming or outgoing data.
- Listeners, for objects that produce Streams.


Listeners
---------

.. versionadded:: 1.3.0

Listeners have only one type, the ``Listener`` interface, and one method, ``unsafeAccept``.
Listeners can technically produce any object, not just streams, but it is recommended to at least
only produce ``Closeable`` instances. As with most Unsafe operations, ``Listener`` has some helper
extension functions:

.. code-block:: kotlin

    // where someListener is a Listener<out Closeable>
    val result = someListener.accept {
        ...
    }

    ClosingScope {
        val obb = someListener.accept(it)
    }

Streams
-------

Streams are objects that perform actual I/O, e.g. a file stream or a socket stream. Streams use a
set of base interfaces:

- ``Readable``; an object that can be read from.
- ``Writeable``; an object that can be written to.
- ``ReadWrite``; an object that is both ``Readable`` and ``Writeable``.

A Stream is a combination of either ``Readable``, ``Writeable`` or ``ReadWrite``, and ``Closeable``.
They come in several flavours:

- ``ReadableStream``; a stream that can be read from.
- ``WriteableStream``; a stream that can be written to.
- ``BidirectionalStream``; a stream that is both a ``ReadableStream`` and ``WriteableStream``.
- ``HalfcloseableStream``; a ``BidirectionalStream`` that can have one half closed independently.

Streams have both a very high-level API and a low-level API. Implementors of a ``Stream`` need only
implement the low-level API, as the very high-level API

The very high-level API
-----------------------

The very high-level API for streams operates on :ref:`bytestring`. This is similar to Python's I/O
system, as an example.

- ``Readable.readUpTo(count)`` reads ``count`` bytes from a ``readable``, returning a
  ``ByteString`` object containing the result.
- ``Writeable.writeAll(bs)`` writes all bytes from the passed ``ByteString``.

For end-user applications, this is the preferred API due to its simplicity.

The low-level API
-----------------

The low-level API involves ``ByteArray`` and ``Buffer`` objects. See :ref:`buffer` for more
information on buffers.

- ``Readable.readInto(arr, size, offset)`` - Reads ``size`` bytes into ``arr``, starting at
  ``offset``.
- ``Readable.readInto(buffer, size)`` - Reads ``size`` bytes into ``buffer``.
- ``Writeable.writeAllFrom(ba, size, offset)`` - Writes ``size`` bytes from ``arr``, starting at
  ``offset``, into this writeable.
- ``Writeable.writeAllFrom(buffer, size)`` - Writes ``size`` bytes from ``buffer`` into this
  writeable.

If implementing your own ``Readable`` or ``Writeable``, it is recommended to read :ref:`buffer` for
details of how to efficiently pass data to and from your underlying calls.
