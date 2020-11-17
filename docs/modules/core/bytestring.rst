.. _bytestring:

Bytestrings
===========

.. versionchanged:: 1.2.0

    Moved from Tinlok-Core to Copperchain.

A ``ByteString`` is an immutable sequence of singular bytes, or simply an immutable ``ByteArray``.
It is used extensively whenever binary data is involved.

Unlike ``ByteArray`` objects, ``ByteString`` objects provide sane equality and hashcode attributes
(no ``contentEquals`` or ``contentHashCode``).

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

``ByteString`` implements the ``+`` operator for immutable concatenation:

.. code-block:: kotlin

    // concatenates two bytestrings
    val first = b("HK")
    val second = b("416")
    assert((first + second) == b("HK416"))

Additionally, ``ByteString`` provides extensions similar to a standard ``String``. A short set of
examples for these:

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

If a ``String`` extension equivalent is missing for ``ByteString``, please raise an issue.

Conversion to Strings
---------------------

``ByteString`` objects can be decoded to a ``String`` using the ``decode`` method:

.. code-block:: kotlin

    val bs = b("string!")
    assert(bs.decode() == "string!")

``ByteString`` objects can also be turned into an escaped string, for invalid unicode values.

.. code-block:: kotlin

    val bs = byteArrayOf(1, 2).toByteString()
    val s1 = bs.decode()  // fails!
    val s2 = bs.escapedString()  // succeeds, "\\x01\\x02"

Unwrapping
----------

A ``ByteString`` can be turned into a regular ``ByteArray`` in one of several ways:

- With ``Collection<Byte>.toByteArray()`` which iterates over each ``Byte`` and copes them into a
  ``ByteArray``.
- With ``unwrapCopy``, which makes a direct copy of the backing ``ByteArray``. This is the preferred
  method.
- With ``unwrap``, which is ``@Unsafe`` and returns the backing ``ByteArray`` directly. This should
  only be used for performance concerns inside low-level code when you need to pass a ``ByteArray``
  to a C funcrtion, for example.

