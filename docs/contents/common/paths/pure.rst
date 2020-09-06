.. _paths-pure:

Pure Paths
==========

Pure paths (the ``PurePath`` interface and all subclasses) are representations of filesystem
paths that only perform logical operations without performing any I/O. A pure path does not
represent any particular file, directory or filesystem. Pure paths are not platform specific; you
can create a ``PosixPurePath`` on Windows and a ``WindowsPurePath`` on Linux.

Creation
--------

To get a pure path that matches your current platform, use the ``Paths`` object:

.. code-block:: kotlin

    val path = Paths.purePath("/etc/passwd")

You can create instances of non-platform specific paths directly:

.. code-block:: kotlin

    val posix: PurePath = PosixPurePath("/etc/passwd")
    val windows: PurePath = WindowsPurePath("C:\\\\Windows\\System32\\etc\\hosts")

Core attributes
---------------

Pure paths are comprised of a list of **components** separated by the path separator for the
platform the pure path is for. A pure path also contains helper properties for:

* ``anchor`` - the / or the drive letter
* ``name`` - the final component
* ``suffix`` ``suffixes`` - the file extensions, if any.

.. code-block:: kotlin

    val hosts = PosixPurePath("/etc/hosts")
    println(hosts.components) // [/, etc, hosts]
    println(hosts.name) // hosts
    println(hosts.suffix)  // null

    val text = PosixPurePath("file.tar.gz")
    println(text.suffix)  // gz
    println(text.suffixes)  // [tar, gz]


