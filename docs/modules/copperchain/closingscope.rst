.. _closingscope:

Closeable and ClosingScope
==========================

.. versionchanged:: 1.2.0

    Moved from Tinlok-Core to Copperchain.

.. note::

    Parts of this applies to the version of Kotlin/Native with primitive ARC memory management
    (as of writing, 1.4 and earlier). With a tracing GC, some form of finalizer may be added which
    may make this less unsafe.

In the native world, many things need to be opened, used, then closed. The opening part is easy
but the closing part is hard; it can be forgotten or exceptions can occur, and then you have a
memory leak or a file descriptor leak or other similarly vile issues.

Copperchain solves this with a combination of the ``Unsafe`` annotation, and the
``Closeable`` interface.

Closing safely
--------------

In any case where a native object needs to hold onto an external resource for a specified amount
of time, such as a heap allocated C structure or a file descriptor, there are two design rules:

1. The class holding the resource must be a ``Closeable``.
2. Any functions or constructors that create a new instance of the class must be marked ``Unsafe``.

Normally, this would mean any user code that wants to use an object that holds an external
resource would also need to be marked ``Unsafe``, thus not really providing any extra security.
This is solved by creating an extension function that passes the newly created object to a
provided lambda. For example, given the following object:

.. code-block:: kotlin

    public class MyObject : Closeable {
        // heap allocator
        private val arena = Arena()
        // some external C struct
        private val struct = arena.alloc<some_struct>()

        public fun doSomething() = ...

        override fun close() {
            arena.free()
        }
    }

Using it normally would be like so:

.. code-block:: kotlin

    fun someFun() {
        val obb = MyObject()
        return obb.doSomething()  // OOPS! ``struct`` is never freed!
    }

This creates a memory leak that cannot be resolved by Kotlin. (Whilst the example is trivial, it is
very easy to do this accidentally).

Instead, the object should be designed like this:

.. code-block:: kotlin

    public class MyObject @Unsafe private constructor() : Closeable {
        public companion object {
            @OptIn(Unsafe::class)
            public inline fun <R> create(block: (MyObject) -> R): R {
                val obb = MyObject()
                return obb.use(block)
            }
        }
        ...
    }

The ``use`` extension function is equivalent to the following:

.. code-block:: kotlin

    try {
        block(this)
    } finally {
        close()
    }

ClosingScope
------------

The above method has advantages (it prevents resource leaks) but it can also end up with your
code looking like 2009 JavaScript.

.. code-block:: kotlin

    SomeObject.create { a ->
        OtherObject.create { b ->
            EvenMoreObject.create { c ->
                // actual logic, three indentations deep
            }
        }
    }

To solve this, the ``ClosingScope`` interface exists. In it's simplest form, it can be used as a
function:

.. code-block:: kotlin

    // example extension, assumes constructor is internal/unsafe
    @OptIn(Unsafe::class)
    public fun SomeObject.Companion.create(scope: ClosingScope) {
        val obb = SomeObject()
        scope.add(obb)
        return obb
    }

    val result = ClosingScope { scope ->
        val a = SomeObject.create(scope)
        val b = OtherObject.create(scope)
        val c = EvenMoreObject.create(scope)

        // nice linear logic goes here
    }

.. note::

    If/when multiple receivers land in Kotlin, the APIs will be changed so that ``scope`` does
    not need to be explicitly passed.

When the block returns, all objects will be automatically closed safely.

.. warning::

    Objects will be closed in no specific order. Do not rely on it.

.. warning::

    When a ``ClosingScope`` returns, only the LAST exception will be re-thrown.

All closeable objects provide extension functions for both the callback and the
``ClosingScope`` forms.

