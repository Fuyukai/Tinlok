.. _paths-pure:

Paths - Pure
============

Pure paths (the ``PurePath`` interface and all subclasses) are immutable representations of
filesystem paths that only perform logical operations without performing any I/O. A pure path
does not represent any particular file, directory or filesystem. Pure paths are not platform
specific; you can create a ``PosixPurePath`` on Windows and a ``WindowsPurePath`` on Linux.

.. note::

    All pure paths are implemented using ``ByteString`` objects, because paths are not unicode
    strings. String methods exist for convenience.

Creation
--------

To get a pure path that matches your current platform:

.. code-block:: kotlin

    val path = Path.native("/etc/passwd")

You can create instances of non-platform specific paths directly:

.. code-block:: kotlin

    val posix: PurePath = PosixPurePath("/etc/passwd")
    val windows: PurePath = WindowsPurePath("C:\\\\Windows\\System32\\etc\\hosts")

Usage
-----

Pure paths are comprised of a list of *components* separated by the path separator for the
platform the pure path is for.

A pure path also contains helper properties for:

* ``anchor`` - the / or the drive letter, for absolute paths
* ``parent`` - the parent PurePath instance for this path
* ``name`` - the final component
* ``suffix`` ``suffixes`` - the file extensions, if any.

.. code-block:: kotlin

    val hosts = PosixPurePath("/etc/hosts")
    println(hosts.components) // [/, etc, hosts]
    println(hosts.name) // hosts
    println(hosts.suffix)  // null

    val text = PosixPurePath("file.tar.gz")
    println(text.anchor)  // null
    println(text.suffix)  // gz
    println(text.suffixes)  // [tar, gz]


A pure path can be joined with another pure path using the ``resolveChild`` method:

.. code-block:: kotlin

    val etc = PosixPurePath("/etc")
    val resolv = etc.resolveChild(PosixPurePath("resolv.conf"))  // /etc/resolv.conf
    val passwd = etc.resolveChild(b("passwd"))  // /etc/passwd
    val shadow = etc.resolveChild("shadow")  // /etc/shadow

Joining two absolute paths together will just return the second absolute path:

.. code-block:: kotlin

    val etc = PosixPurePath("/etc")
    val usr = PosixPurePath("/usr")
    assert(etc.resolveChild(usr) == usr)

The name of a pure path can be replaced using ``withName``:

.. code-block:: kotlin

    val shadow = PosixPurePath("/etc/shadow")
    val passwd = shadow.withName("passwd")
    assert(passwd.name == "passwd")
    // doesn't change the original path
    assert(shadow.name == "shadow")

You can check if two pure paths are related with ``isChildOf`` and ``isParentOf``:

.. code-block:: kotlin

    val usr = PosixPurePath("/usr")
    val local = PosixPurePath("/usr/local")
    assert(local.isChildOf(usr))
    assert(usr.isParentOf(local))

You can also change a pure path's parent with ``reparent``:

.. code-block:: kotlin

    val sitePackages = PosixPurePath("/usr/lib/python3.8/site-packages")
    val localSitePackages = sitePackages.reparent(from = usr, to = local)
    assert(localSitePackages == PosixPurePath("/usr/local/lib/python3.8/site-packages"))
