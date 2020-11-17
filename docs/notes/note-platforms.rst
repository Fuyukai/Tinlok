.. _platform_support:

A note on platform support
==========================

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

* Windows AMD64 (``mingwX64``)

  - My dev box is Linux, so the default branch may have outdated or broken Windows support; but
    official releases will have support.

Pending support
---------------

These platforms are platforms it would be ideal to support, but can't for various reasons:

* Linux MIPS(el)32 (``linuxMips32``)

  - I don't have any sort of MIPS development board so there's no way for me to test or develop
    for these architectures.

* macOS (``macosX64``, ARM)

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
