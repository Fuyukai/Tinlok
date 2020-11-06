.. _io-interfaces:

I/O Interfaces
==============

.. versionchanged:: 1.2.0

    Moved from Tinlok-Core to Copperchain.

.. versionadded:: 1.2.0

    Added asynchronous variants.

Copperchain provides several interfaces defining how I/O objects act. Both synchronous and
asynchronous variants exist.

Core Interfaces
---------------

There are four primary interfaces:

* ``Readable`` and ``AsyncReadable``, which define ``readInto`` for consuming up to N bytes from an
  object.

* ``Closeable`` and ``AsyncCloseable``, which define the idempotent ``close`` to close the
  resource, and the ``use`` and ``ause`` extension methods which automatically closes them.

* ``Writeable`` and ``AsyncWriteable``, which defines ``writeAll`` for writing the entire contents
  of a ``ByteArray`` to the object.

* ``Seekable``, which defines operations to get and change the cursor position for a resource.

Streams
-------

Streams are the combination of the ``Closeable`` interface, and any other core interfaces. Tinlok
defines several:

* ``(Async)ReadableStream`` is a closeable readable object.

* ``(Async)WriteableStream`` is a closeable writeable object.

* ``(Async)BidirectionalStream`` is a closeable readable and writeable object.

* ``(Async)HalfCloseableStream`` is a ``BidirectionalStream`` that can have the writing part closed
  independently of the reading part.

Summary
-------

A table summarising the interfaces:

+-------------------------+-----------------------------------------+------------------------------------------------+
| Interface               | Inherits                                | Provides                                       |
+=========================+=========================================+================================================+
| ``Readable``            | None                                    | ``readInto``                                   |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``Writeable``           | None                                    | ``writeFrom``                                  |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``Closeable``           | None                                    | ``close``                                      |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``Seekable``            | None                                    | ``cursor``, ``seekAbsolute``, ``seekRelative`` |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``ReadableStream``      | ``Readable``, ``Closeable``             | None                                           |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``WriteableStream``     | ``Writeable``, ``Closeable``            | None                                           |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``BidirectionalStream`` | ``ReadableStream``, ``WriteableStream`` | None                                           |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``HalfCloseableStream`` | ``BidirectionalStream``                 | ``sendEof``                                    |
+-------------------------+-----------------------------------------+------------------------------------------------+

All interfaces in this table have an ``Async``-prefixed counterpart which works in the same manner.

Credits
-------

The concept of these interfaces were taken from the excellent Trio_ library for asynchronous
programming in Python.

.. _Trio: https://trio.readthedocs.io/en/stable/reference-io.html#the-abstract-stream-api
