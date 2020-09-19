.. _bytestring:

Types - Bytestrings
===================

A ``ByteString`` is an immutable sequence of singular bytes, or simply an immutable ``ByteArray``.
It is used extensively within Tinlok whenever binary data is involved.

Creation
--------

``ByteString`` instances can be created with the helper methods on the companion object, from
either a regular ``String`` or a regular ``ByteArray``.

To create a new ``ByteString`` from a ``String``:

.. code-block:: kotlin

    // variable strings
    val bs = ByteString.fromString(someString)
    val bs = someString.toByteString()

    // or, for literal strings
    val literal = b("literal string")

.. warning::

    Do not use the ``b()`` method for non-literals. It may cache inputs to avoid string encoding
    penalties.

To create a new ``ByteString`` from a ``ByteArray``:

.. code-block:: kotlin

    val bs = ByteString.fromByteArray(ba)
    val bs = ba.toByteString()

.. note::

    Public ``ByteArray`` conversion methods will always create a copy of the incoming array, to
    ensure immutability.

Usage
-----

``ByteString`` implements ``Collection<Byte>``, so it can be treated as one:

.. code-block:: kotlin

    val bs = byteArrayOf(0, 1, 2, 3, 4).toByteString()
    assert(bs.first() == (0).toByte())
    assert(bs[0] == (0).toByte())
    assert((2).toByte() in bs)
    assert(bs.containsAll(byteArrayOf(2, 4, 3)))

Some convenience methods are also provided:

.. code-block:: kotlin

    // check if a bytestring starts with another one
    val first = b("Hello, world!")
    val second = b("Hello,")
    assert(first.startsWith(second))

    // concatenates two bytestrings
    val first = b("HK")
    val second = b("416")
    assert((first + second) == b("HK416"))

    // splits apart a bytestring
    val ip = b("4.1.20.5")
    val split = ip.split(b("."))
    assert(split[1] == b("1"))

    // slices a bytestring
    val str = b("$$real$$")
    val slice = str.substring(2, str.size - 2)
    assert(slice == b("real"))

``ByteString`` objects can be decoded to a ``String`` using the ``decode`` method:

.. code-block:: kotlin

    val bs = b("string!")
    assert(bs.decode() == "string!")

