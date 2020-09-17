.. _platform_support:

Platform support
================

Tinlok, being a native library, needs to be ported to each target it runs on. Whilst a lot of
code is shared, libc bindings are specific to each OS and statically linked libraries are
specific to each OS+architecture combination.

Supported platforms
-------------------

These platforms have priority support:

* Linux AMD64 (``linuxX64``)

* Linux ARM64 (``linuxAMD64``)

.. warning::

    Tinlok needs relatively new versions of the Linux kernel and glibc for all features. Ensure
    you are on at least the latest kernel.org LTS (5.4 at time of writing).

Pending support
---------------

These platforms are platforms it would be ideal to support, but can't for various reasons:

* Windows AMD64 (``mingwX64``)

  - K/N does not currently support cross-compiling from Linux to Windows, and cinterop commonizer
    also breaks.

* Linux MIPS(el)32 (``linuxMips32``)

  - I don't have any sort of MIPS development board so there's no way for me to test or develop
    for these architectures.

* macOS (``macosX64()``)

  - I don't have a macOS device, and there are no cross-compilers due to the nature of macOS.


Not gonna happen
----------------

These platforms will very likely never be supported:

* JVM

  - Well, duh. You already have the Java standard library.

* JS

  - See above.

* iOS/tvOS/watchOS

  - Just use Swift or Obj-C. It's practically what K/N has been designed for.

* Android native

* 32-bit platforms
