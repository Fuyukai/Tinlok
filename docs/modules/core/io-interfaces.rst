.. _io-interfaces:

I/O Interfaces
==============

Tinlok Core provides several interfaces defining how synchronous I/O objects act. These are
implemented by all I/O objects provided by the Tinlok Core library, such as files and sockets.

Core Interfaces
---------------

There are four core interfaces:

* ``Readable``, which defines ``readUpTo`` for consuming up to N bytes from an object.

* ``Closeable``, which defines the idempotent ``close`` to close the resource, and the ``use``
  extension method which automatically closes it.

* ``Writeable``, which defines ``writeAll`` for writing the entire contents of a ``ByteString``
  to the object.

* ``Seekable``, which defines operations to get and change the cursor position for a resource.

Streams
-------

Streams are the combination of the ``Closeable`` interface, and any other core interfaces. Tinlok
defines several:

* ``ReadableStream`` is a closeable readable object.

* ``WriteableStream`` is a closeable writeable object.

* ``BidirectionalStream`` is a closeable readable and writeable object.

* ``HalfCloseableStream`` is a ``BidirectionalStream`` that can have the writing part closed
  independently of the reading part.

Strings
-------

Whilst all of the I/O interfaces produce and consume ``ByteString`` objects, there also exists
interfaces defining objects that produce and consume regular Kotlin ``String`` objects.

* ``StringReadable`` is a ``Readable`` that can **also** produce ``String`` objects.

* ``StringWriteable`` is a ``Writeable`` that can **also** consume ``String`` objects.

String streams similarly exist for each combination of stream objects.

Summary
-------

A table summarising the interfaces:

+-------------------------+-----------------------------------------+------------------------------------------------+
| Interface               | Inherits                                | Provides                                       |
+=========================+=========================================+================================================+
| ``Readable``            | None                                    | ``readUpTo``                                   |
+-------------------------+-----------------------------------------+------------------------------------------------+
| ``Writeable``           | None                                    | ``writeAll``                                   |
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

Credits
-------

The concept of these interfaces were taken from the excellent Trio_ library for asynchronous
programming in Python.

.. _Trio: https://trio.readthedocs.io/en/stable/reference-io.html#the-abstract-stream-api
