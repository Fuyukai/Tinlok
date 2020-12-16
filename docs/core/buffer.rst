.. _buffer:

Buffers
=======

.. versionadded:: 1.3.0

    This class was added.

A ``Buffer`` is a mutable block of memory that can be read or written to in a structured manner, and
passed to streams to efficiently read or write directly to memory without needing to go through an
intermediate ``ByteArray``. Buffers may be Closeable if they hold a managed resource (e.g. mmap()'d
buffers); see :ref:`closingscope`.

Cursors and Capacity
--------------------

A ``Buffer`` has the concept of a ``cursor`` and a ``capacity``, both exposed as properties.

The ``cursor`` is the current position of the buffer; all read and write operations to the buffer
will start at this position in the buffer's backing storage. The cursor is mutable, and can be
changed at will.

The ``capacity`` is the maximum position of the cursor, and thus the maximum amount of data that can
be written to or read from the buffer. The ``capacity`` is *also* mutable, but cannot be changed at
will i.e. it can be read from and the read value may be changed but it cannot be written to.

Creation
--------

The ``ByteArrayBuffer`` class exists as an in-memory implemention of ``Buffer``.

Reading
-------

A ``Buffer`` has several functions for reading data out of it. Each read method increments the
``cursor`` value by the size of the type being read.

- ``readByte`` - reads a singular byte at the ``cursor`` position.
- ``readShort`` - reads a singular short at the ``cursor`` position.
- ``readInt`` - reads a singular int at the ``cursor`` position.
- ``readLong`` - reads a singular long at the ``cursor`` position.
- ``readArray`` - reads a sequence of bytes at the ``cursor`` positions.

All methods imply big-endian reads. The ``readShortLE``, ``readIntLE``, and ``readLongLE`` methods
are provided for little endian reads.

Writing
-------

Similarly, a ``Buffer`` has several functions for writing data into it. Each write method increments
the ``cursor`` value by the size of the type being written.

- ``writeByte`` - writes a singular byte at the ``cursor`` position.
- ``writeShort`` - reads a singular short at the ``cursor`` position.
- ``writeInt`` - writes a singular int at the ``cursor`` position.
- ``writeLong`` - writes a singular long at the ``cursor`` position.
- ``writeArray`` -  writes a sequence of bytes at the ``cursor`` positions.

All methods imply big-endian writes. The ``writeShortLE``, ``writeIntLE``, and ``writeLongLe``
methods are provided for little endian reads.

Implementation Details
----------------------

.. warning::

    The ``Buffer`` interface should **not** be implemented in common modules.

``Buffer`` implementations are designed to allow efficient writing or reading directly from the
backing storage, via the ``address`` method. However, some buffers may not be backed by a
memory-mapped object. The ``supportsAddress`` function should be called by anything asking for a
``Buffer`` object's internal address.

- If ``supportsAddress`` returns true, use the ``address`` method with a callback that will take the
  direct ``CPointer<ByteVar>`` of the backing memory for the buffer.
- If ``supportsAddress`` returns false, use the ``readArray`` and ``writeArray`` methods for
  fallback support.
