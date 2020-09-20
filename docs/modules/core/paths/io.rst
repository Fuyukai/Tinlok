.. _paths-io:

Paths - I/O
============

I/O Paths (the ``Path`` interface) are immutable representations of a real file or directory on a
filesystem. These paths can be used to open a file for reading or writing, or perform various
filesystem interactions.

All ``Path`` instances are also ``PurePath`` instances and can be passed around like so. Unlike
``PurePath``, only the platform-specific implementation if a ``Path`` is available (and is an
``internal class``), as well as any path implementations for various virtual filesystems.

Creation
--------

A platform-specific ``Path`` object can be created using the methods on the ``Path`` companion
object:

.. code-block:: kotlin

    val path = Path.of("/etc/hosts")
    val path = Path.of(b("/etc/passwd"))
    val path = Path.of(PurePath.native("/etc/shadow"))

.. warning::

    You can only create a ``Path`` from a platform ``PurePath``. This is enforced with typing.

File I/O
--------

The contents of a file can be read using the ``Path.readAllBytes`` and ``Path.readAllString``
convenience methods:

.. code-block:: kotlin

    val path = Path.of("/etc/hostname")
    assert(path.readAllBytes() == b("MEMBER"))
    assert(path.readAllString() == b("BLOCK"))

Files can also be written to using ``Path.writeBytes`` and ``Path.writeString``. By default,
these methods are atomic (either ALL content will be written to the file, or NO content will be
written to the file), but this can be disabled by passing ``atomic = false`` to the functions.

.. code-block:: kotlin

    val path = Path.of("/etc/hostname")
    path.writeBytes(b("ITEM"))
    path.writeString("SCHOOL")

If you don't wish to read whole files into memory, you can open the path using ``Path.open``:

.. code-block:: kotlin

    val path = Path.of("/dev/urandom")
    path.open(StandardOpenModes.READ) { it: FilesystemFile ->
        val bytes = it.readUpTo(16)
    }


``open`` provides a ``FilesystemFile`` to a lambda which is a union of
``BidirectionalStringStream | Seekable``. The file will always be closed at the end of the
lambda.

Filesystem interaction
----------------------

``Path`` instances have various methods to interact with their filesystem.

There are several high level functions and extensions for the most common actions:

.. code-block:: kotlin

    val path = Path.of("/some/file")
    // high-level move, works across different filesystems
    path.move(Path.of("/some/other/file"))
    // high-level copy, copies files efficiently and recursively copies directories
    path.copy(Path.of("/some/other/file2"))
    // high-level delete, will unlink files/symlinks and recursively delete directories
    path.delete()
    // high-level symlink
    path.symlinkTo(Path.of("/real/file"))

To see the underlying lower-level functions that power these extensions, check their source code.

The status of a file can be queried with various methods:

.. code-block:: kotlin

    val path = Path.home().resolveChild(".config/alacritty/alacritty.yml")
    // check if the file exists
    assert(path.exists())
    // get the size of the file
    println("File size: ${path.size()}")
    // probe its type
    assert(path.isRegularFile(followSymlinks = true))
    assert(!path.isDirectory(followSymlinks = false))
    assert(!path.isLink())

For directories, there are two methods for listing the underlying files:

* ``Path.scandir`` which is provided a lambda to be called for every entry (faster)

* ``Path.listdir`` which returns a list of ``DirEntry`` instead.

The ``DirEntry`` data class contains a ``Path`` of the child directory and the ``FileType`` of
the file listed (only supported on certain filesystems). It also contains functions similar to
the query operations which operate on the ``FileType`` to avoid excessive stat() calls.

A ``Path`` can be fully resolved into an absolute path using ``toAbsolutePath``:

.. code-block:: kotlin

    val path = Path.of("./abc/def")
    val absolute = path.toAbsolutePath()
    assert(path.isAbsolute)
    assert(path == Path.of("/home/cs/abc/def"))

Temporary files
---------------

Temporary folders and files are tricky security-wise (as people can intercept your creation and
do evil things). The ``Path.createTempDirectory`` extension is provided that calls an underlying,
more secure, platform call to create a temporary directory with the correct permissions for
security.

.. code-block:: kotlin

    Path.createTempDirectory("some-prefix") { tmp ->
        val file = tmp.resolveChild("some-file.txt")
        file.writeAllString("...")
    }

The path will be automatically recursively deleted at the end of operations.

.. warning::

    Not to be confused with the unsafe method that only takes a prefix and returns the created
    Path instead of passing it to a lambda.
